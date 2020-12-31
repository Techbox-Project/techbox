package io.github.techbox.core.modules.commands

import io.github.techbox.TechboxLauncher
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent


class CommandProcessor {

    suspend fun run(event: GuildMessageReceivedEvent): Boolean {
        var rawCmd = event.message.contentRaw

        val prefixes = TechboxLauncher.config.prefix

        val usedPrefix = prefixes.firstOrNull { rawCmd.toLowerCase().startsWith(it) }

        val selfMention = event.jda.selfUser.asMention
        if (usedPrefix != null) {
            rawCmd = rawCmd.substring(usedPrefix.length)
        } else if (rawCmd.startsWith("$selfMention ")) {
            rawCmd = rawCmd.substring("$selfMention ".length)
        } else {
            return false
        }

        var args = rawCmd.split(" ")
        val cmdName = args[0]
        args = args.drop(1)

        TechboxLauncher.core.commandRegistry.execute(CommandContext(event.message, cmdName, args))
        return true
    }

}