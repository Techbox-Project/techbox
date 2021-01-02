package io.github.techbox.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.*
import java.awt.Color
import java.time.temporal.TemporalAccessor

const val ZERO_WIDTH_SPACE = EmbedBuilder.ZERO_WIDTH_SPACE

class TechboxEmbedBuilder {
    var title: String? = null
    var titleURL: String? = null

    var authorName: String? = null
    var authorURL: String? = null
    var authorAvatar: String? = null

    var footer: String? = null
    var footerIcon: String? = null

    var color: Color? = null
    var image: String? = null
    var thumbnail: String? = null
    var description: String? = null
    var timestamp: TemporalAccessor? = null
    var fields: MutableList<Field> = ArrayList()

    fun field(name: String, value: String, inline: Boolean = false) =
        fields.add(Field(name, value, inline))

    fun build(): MessageEmbed {
        return EmbedBuilder()
            .setThumbnail(thumbnail)
            .setImage(image)
            .setTitle(title, titleURL)
            .setDescription(description)
            .setAuthor(authorName, authorURL, authorAvatar)
            .setFooter(footer, footerIcon)
            .setTimestamp(timestamp)
            .setColor(color)
            .also {
                for (field in fields)
                    it.addField(field)
            }
            .build()
    }
}
