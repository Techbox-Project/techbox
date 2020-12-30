package io.github.techbox.core

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.github.techbox.core.shard.Shard
import io.github.techbox.data.Config
import io.github.techbox.utils.formatDuration
import io.github.techbox.utils.logger
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
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.IntFunction
import java.util.stream.Collectors
import javax.annotation.Nonnull
import javax.security.auth.login.LoginException


class Techbox(private val config: Config) {
    private val log: Logger = logger<Techbox>()
    private var loadState: LoadState = LoadState.PRELOAD
    private val shards: ConcurrentHashMap<Int, Shard> = ConcurrentHashMap()
    private val threadPool = Executors.newCachedThreadPool(
        ThreadFactoryBuilder().setNameFormat("Techbox Thread-%d").build()
    )
    private lateinit var shardManager: ShardManager

    fun start() {
        startShards()
    }

    private fun startShards() {
        loadState = LoadState.LOADING
        val gatewayThreadFactory = ThreadFactoryBuilder()
            .setNameFormat("GatewayThread-%d")
            .setDaemon(true)
            .setPriority(Thread.MAX_PRIORITY)
            .build()
        val requesterThreadFactory = ThreadFactoryBuilder()
            .setNameFormat("RequesterThread-%d")
            .setDaemon(true)
            .build()

        try {
            val defaultDeniedMentions =
                EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE)
            MessageAction.setDefaultMentions(EnumSet.complementOf(defaultDeniedMentions))

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

            val shardStartListener = ShardStartListener()

            val shardManager: DefaultShardManagerBuilder = DefaultShardManagerBuilder.create(
                config.token,
                enabledGatewayIntents
            )
                .setChunkingFilter(ChunkingFilter.NONE)
                .addEventListeners(
                    // TODO Event listeners go here
                    shardStartListener
                )
                .addEventListenerProviders(
                    // TODO Event listener providers (commands, members, etc) go here
                    listOf(
                        IntFunction { id -> getShard(id).listener }
                    ))
                .setEventManagerProvider { id -> getShard(id).manager }
                .setBulkDeleteSplittingEnabled(false)
                .disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS))
                .setActivity(Activity.playing("Techbox is loading..."))

            val shardIds = 0 until config.shardCount
            val latchCount = config.shardCount

            shardManager.setShardsTotal(config.shardCount)
                .setGatewayPool(Executors.newSingleThreadScheduledExecutor(gatewayThreadFactory), true)
                .setRateLimitPool(Executors.newScheduledThreadPool(2, requesterThreadFactory), true)
            log.info("Using ${config.shardCount} shards")

            if (shardIds.count() != latchCount) {
                throw IllegalStateException("Shard ids list must have the same size as latch count")
            }

            loadState = LoadState.LOADING_SHARDS

            log.info("Spawning $latchCount shards...")
            val start = System.currentTimeMillis()
            shardStartListener.latch = CountDownLatch(latchCount)
            this.shardManager = shardManager.build()

            threadPool.submit {
                log.info("CountdownLatch started: Awaiting for $latchCount shards to be counted down to start PostLoad.")
                try {
                    shardStartListener.latch.await()
                    val elapsed = System.currentTimeMillis() - start
                    log.info("All shards logged in! Took ${TimeUnit.MILLISECONDS.toSeconds(elapsed)} seconds")
                    shardManager.removeEventListeners(shardStartListener)
                    startPostLoadProcedure(elapsed)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        } catch (e: LoginException) {
            throw IllegalStateException(e)
        }

        loadState = LoadState.LOADED
    }

    private fun startPostLoadProcedure(elapsed: Long) {
        loadState = LoadState.READY

        log.info("Loaded all shards successfully! Took ${formatDuration(elapsed)}. Current status: $loadState")
    }

    fun getShard(id: Int): Shard {
        return shards.computeIfAbsent(id) { Shard(id) }
    }

    private class ShardStartListener : EventListener {
        lateinit var latch: CountDownLatch

        override fun onEvent(@Nonnull event: GenericEvent) {
            if (event is ReadyEvent) {
                val sm = event.getJDA().shardManager
                    ?: throw AssertionError()
                latch.countDown()
            }
        }
    }

}