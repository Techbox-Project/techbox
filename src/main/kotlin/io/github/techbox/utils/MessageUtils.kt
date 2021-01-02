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

    var author: String? = null
    var authorURL: String? = null
    var authorAvatar: String? = null

    var footer: String? = null
    var footerIcon: String? = null

    var color: Color? = null
    var image: String? = null
    var thumbnail: String? = null
    var description: String? = null
    var timestamp: TemporalAccessor? = null
    var fields: Fields = Fields()

    fun fields(body: Fields.() -> Unit) = Fields().apply(body)

    fun field(body: TechboxFieldBuilder.() -> Unit) =
        fields.add(body)

    fun build(): MessageEmbed {
        return EmbedBuilder()
            .setThumbnail(thumbnail)
            .setImage(image)
            .setTitle(title, titleURL)
            .setDescription(description)
            .setAuthor(author, authorURL, authorAvatar)
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

class Fields {
    private val list: MutableList<Field> = ArrayList()

    fun blank(inline: Boolean = false) =
        add(Field(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, inline))

    fun add(field: Field) = list.add(field)

    fun add(body: TechboxFieldBuilder.() -> Unit) =
        add(TechboxFieldBuilder().build(body))

    operator fun iterator() = list.iterator()
}

class TechboxFieldBuilder {
    lateinit var name: String
    lateinit var value: String
    var inline = false

    fun build(body: TechboxFieldBuilder.() -> Unit): Field {
        body()
        return Field(name, value, inline)
    }

}