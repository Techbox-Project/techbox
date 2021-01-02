package io.github.techbox.core.modules.commands

import io.github.techbox.TechboxLauncher
import io.github.techbox.data.entities.TechboxGuild
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent


class CommandProcessor {

    suspend fun run(event: GuildMessageReceivedEvent): Boolean {
        var rawCmd = event.message.contentRaw

        val prefixes = TechboxLauncher.config.prefix
        val techboxGuild = TechboxGuild.findById(event.guild.idLong) ?: TechboxGuild.new(event.guild.idLong) {}
        val customPrefix = techboxGuild.commandPrefix

        var usedPrefix = prefixes.firstOrNull { rawCmd.toLowerCase().startsWith(it) }

        val selfUserMention = event.jda.selfUser.asMention
        val selfMemberMention = event.jda.selfUser.asMention.replace("@", "@!")
        when {
            usedPrefix != null -> {
                rawCmd = rawCmd.substring(usedPrefix.length)
            }
            customPrefix != null && rawCmd.startsWith(customPrefix) -> {
                rawCmd = rawCmd.substring(customPrefix.length)
                usedPrefix = customPrefix
            }
            rawCmd.startsWith("$selfUserMention ") -> {
                rawCmd = rawCmd.substring("$selfUserMention ".length)
                usedPrefix = selfUserMention
            }
            rawCmd.startsWith("$selfMemberMention ") -> {
                rawCmd = rawCmd.substring("$selfMemberMention ".length)
                usedPrefix = selfUserMention
            }
            else -> {
                return false
            }
        }

        var args = rawCmd.split(" ")
        val cmdName = args[0]
        args = args.drop(1)

        TechboxLauncher.core.commandRegistry.execute(CommandContext(usedPrefix, event.message, cmdName, args, techboxGuild))
        return true
    }

}