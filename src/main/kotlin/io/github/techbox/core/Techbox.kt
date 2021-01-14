package io.github.techbox.core

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.github.techbox.TechboxConfig
import io.github.techbox.core.listeners.CommandListener
import io.github.techbox.core.listeners.ReactionListener
import io.github.techbox.core.modules.Module
import io.github.techbox.core.modules.ModuleRegistry
import io.github.techbox.core.modules.commands.CommandProcessor
import io.github.techbox.core.modules.commands.CommandRegistry
import io.github.techbox.core.shard.Shard
import io.github.techbox.database.DatabaseProvider
import io.github.techbox.database.entities.TechboxGuild
import io.github.techbox.utils.logger
import io.github.techbox.utils.lookForAnnotatedClassesOn
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.function.IntFunction
import java.util.stream.Collectors
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

class Techbox(val config: TechboxConfig) : CoroutineScope by CoroutineScope(Job() + CoroutineName(TECHBOX)) {

    companion object {
        const val TECHBOX = "Techbox"
        const val TECHBOX_VERSION = "1.0.0"

        private val log: Logger = logger<Techbox>()

        lateinit var INSTANCE: Techbox
            private set
    }

    private var loadState: LoadState = LoadState.PRELOAD
    private val shards: ConcurrentHashMap<Int, Shard> = ConcurrentHashMap()
    private val commandProcessor = CommandProcessor(this)
    lateinit var shardManager: ShardManager
    val moduleRegistry = ModuleRegistry()
    val commandRegistry = CommandRegistry()
    lateinit var databaseProvider: DatabaseProvider

    init {
        INSTANCE = this

        /*
            Some coroutine was not handled properly, this will serve
            as a fatal global exception handler to fix the error.

            This can occur when the execution of a child Job
            without an explicit CoroutineScope is launched on that CoroutineScope and fails.

            This can be easily resolved using SupervisorJob, but it is important
            that the code is presented correctly so it is better to correct a wrong code
            that could possibly result in a fatal error than simply ignoring it.
         */
        coroutineContext[Job]!!.invokeOnCompletion {
            var exitCode = 0
            if (it == null) {
                // abnormal termination since we do not know
                // why the scope was canceled, since we have no exception.
                log.error("abnormal CoroutineScope cancellation")
                exitCode = 1
            }

            log.error(it.toString())
            log.trace(null, it)
            exitProcess(exitCode)
        }
    }

    suspend fun start() {
        loadState = LoadState.LOADING
        log.info("Connecting to the database...")
        databaseProvider = DatabaseProvider(this)
        databaseProvider.connect()
        databaseProvider.runMigrations()

        log.info("Loading modules...")
        for (module in lookForAnnotatedClassesOn("io.github.techbox.modules", Module::class.java)) {
            runCatching {
                moduleRegistry.register(module)
            }.onFailure { err ->
                log.error("Caught error while registering module", err)
            }
        }

        log.info("Loaded " + moduleRegistry.modules.size + " modules and " + commandRegistry.commands.size + " successfully.")
        startShards()
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun startShards() {
        val defaultDeniedMentions =
            EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE)
        MessageAction.setDefaultMentions(EnumSet.complementOf(defaultDeniedMentions))
        MessageAction.setDefaultMentionRepliedUser(false)

        val enabledGatewayIntents = listOf(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES
        )

        log.info("Using intents {}", enabledGatewayIntents
            .stream()
            .map { it.name }
            .collect(Collectors.joining(", "))
        )

        val latchCount = config.shardCount
        val shardCounter = atomic(0)
        val shardReadyState = CompletableDeferred<Unit>()
        val shardListener = object: EventListener {
            override fun onEvent(event: GenericEvent) {
                if (event !is ReadyEvent)
                    return

                val value = shardCounter.incrementAndGet()
                log.info("Shard $value of $latchCount loaded.")

                if (value == latchCount)
                    shardReadyState.complete(Unit)
            }
        }

        val shardManagerBuilder = DefaultShardManagerBuilder.create(config.token, enabledGatewayIntents)
            .setChunkingFilter(ChunkingFilter.NONE)
            .addEventListeners(ReactionListener(), shardListener)
            .addEventListenerProviders(
                // event listener providers (commands, members, etc) go here
                listOf(
                    IntFunction { CommandListener(commandProcessor) },
                    IntFunction { shardId -> getShard(shardId).eventListener }
                ))
            .setEventManagerProvider {
                    shardId -> getShard(shardId).eventManager
            }
            .setBulkDeleteSplittingEnabled(false)
            .disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS))
            .setActivity(Activity.playing("$TECHBOX is loading..."))

        shardManagerBuilder.setShardsTotal(latchCount)
            .setGatewayPool(Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder()
                .setNameFormat("GatewayThread-%d")
                .setDaemon(true)
                .setPriority(Thread.MAX_PRIORITY)
                .build()), true)
            .setRateLimitPool(Executors.newScheduledThreadPool(2, ThreadFactoryBuilder()
                .setNameFormat("RequesterThread-%d")
                .setDaemon(true)
                .build()), true)
        loadState = LoadState.LOADING_SHARDS

        log.info("Spawning $latchCount shards...")
        loadState = LoadState.LOADED
        shardManager = shardManagerBuilder.build()

        val startedAt = Instant.now()
        shardReadyState.invokeOnCompletion {
            loadState = LoadState.READY
            shardManager.removeEventListener(shardListener)
            val duration = Duration.between(startedAt, Instant.now()).toSeconds()
            log.info("Loaded all shards successfully! Took $duration seconds.")
        }
    }

    suspend fun close() {
        for ((_, shard) in shards)
            shard.jda.shutdown()
    }

    fun getShard(id: Int): Shard {
        return shards.computeIfAbsent(id) { Shard(id) }
    }

    suspend fun getGuild(id: Long): TechboxGuild {
        return newSuspendedTransaction(db = databaseProvider.database) {
            TechboxGuild.findById(id) ?: TechboxGuild.new(id) {}
        }
    }

}