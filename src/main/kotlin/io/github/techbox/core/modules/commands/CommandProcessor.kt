package io.github.techbox.core.modules.commands

import io.github.techbox.core.Techbox
import io.github.techbox.database.entities.TechboxGuild
import io.github.techbox.utils.logger
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.Logger

class CommandProcessor(val techbox: Techbox) {

    companion object {

        private val log: Logger = logger<CommandProcessor>()
        const val MENTION_EXALAMATION_INDEX = 2

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun handle(event: GuildMessageReceivedEvent) {
        // TODO: cache guilds previously searched
        val job = techbox.async(NonCancellable + Dispatchers.IO + CoroutineName("Command Processor Guild Search")) {
            techbox.getGuild(event.guild.idLong)
        }

        job.invokeOnCompletion { error ->
            if (error != null) {
                log.error("Failed to load guild ${event.guild.idLong}: $error")
                log.trace(null, error)
                return@invokeOnCompletion
            }

            process(job.getCompleted(), event)
        }
    }

    private fun execute(guild: TechboxGuild, prefix: String, message: Message, command: String, args: List<String>) {
        log.debug("Executing $command...")
        techbox.launch(Dispatchers.IO + CoroutineName("Command Processor: $command")) {
            techbox.commandRegistry.execute(CommandContext(prefix, message, command, args, guild, techbox))
        }
    }

    private fun process(guild: TechboxGuild, event: GuildMessageReceivedEvent) {
        val rawCmd = event.message.contentRaw.toLowerCase()
        val (givenCommand, commandArgs) = rawCmd.split(" ").run {
            first() to lazy { drop(1) }
        }

        // removes equal prefixes if defined and equal.
        val prefixes = listOfNotNull(techbox.config.prefix, guild.commandPrefix).distinct().map { it.toLowerCase() }

        // check for prefixes first, fast-path
        for (prefix in prefixes) {
            val check = givenCommand.substring(prefix.indices)
            if (check == prefix) {
                execute(guild, prefix, event.message, givenCommand.substring(prefix.length), commandArgs.value)
                return
            }
        }

        // check for mentions, slow-path.
        val selfUserMention = event.jda.selfUser.asMention
        if (givenCommand.length < selfUserMention.length)
            return

        // mention, only
        val args = commandArgs.value
        if (args.isEmpty())
            return

        val mentionWithoutExcalamation = givenCommand.removeRange(2, 3) // 2 = exclamation index
        if (mentionWithoutExcalamation == selfUserMention)
            execute(guild, selfUserMention, event.message, args.first(), args.drop(1))
    }

}