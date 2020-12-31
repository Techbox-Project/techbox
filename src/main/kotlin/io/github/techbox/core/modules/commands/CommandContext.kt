package io.github.techbox.core.modules.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed


class CommandContext(
    val message: Message,
    val command: String,
    val args: List<String>
) {
    val author get() = message.author

    fun reply(content: String) {
        message.reply(content).queue()
    }

    fun reply(embed: MessageEmbed) {
        message.reply(embed)
    }

    fun replyEmbed(builder: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) =
        message.reply(builder.also(init).build())

}