package io.github.techbox.core.modules.commands

import io.github.techbox.utils.TechboxEmbedBuilder
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
    val member get() = message.member

    fun reply(content: String) {
        message.reply(content).queue()
    }

    fun replyBlocking(content: String): Message {
        return message.reply(content).complete()
    }

    fun reply(embed: MessageEmbed) {
        message.reply(embed).queue()
    }

    fun reply(reply: Message) {
        message.reply(reply).queue()
    }

    fun replyEmbed(builder: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) =
        message.reply(builder.also(init).build()).queue()

    fun replyEmbedDSL(body: TechboxEmbedBuilder.() -> Unit) =
        message.reply(TechboxEmbedBuilder().apply(body).build()).queue()

}