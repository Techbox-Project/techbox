package io.github.techbox.core.modules.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed


class CommandContext(
    val usedPrefix: String,
    val message: Message,
    val command: String,
    val args: List<String>
) {
    val author get() = message.author

    fun reply(content: String) {
        message.reply(content).queue()
    }

    fun reply(embed: MessageEmbed) {
        message.reply(embed).queue()
    }

    fun reply(reply: Message) {
        message.reply(reply).queue()
    }

    fun replyEmbed(builder: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) =
        message.reply(builder.also(init).build()).queue()

}