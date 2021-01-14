package io.github.techbox.core.modules.commands

import net.dv8tion.jda.api.entities.MessageEmbed

class HelpReceiver(val commandContext: CommandContext?) {
    var title: String? = null
    var aliases: Array<String> = emptyArray()
    var fields: MutableList<MessageEmbed.Field> = mutableListOf()
        private set

    var description: String? = null
        set(value) {
            fields.add(MessageEmbed.Field("Description:", value, false))
        }
    var seeAlso: String? = null
        set(value) {
            fields.add(MessageEmbed.Field("See Also:", value, false))
        }
    var note: String? = null
        set(value) {
            fields.add(MessageEmbed.Field("Note:", value, false))
        }


    inner class UsageReceiver {
        var usageNodes: MutableList<String> = mutableListOf()
        fun usage(command: String, description: String) {
            usageNodes.add("`${commandContext!!.core.config.prefix}$command` - $description")
        }

        fun usage(command: String, extra: String, description: String) {
            usageNodes.add("`${commandContext!!.core.config.prefix}$command` $extra - $description")
        }

        fun textUsage(value: String) {
            usageNodes.add(value)
        }

        fun usageSeparator() {
            usageNodes.add("")
        }
    }

    fun usages(body: UsageReceiver.() -> Unit) {
        fields.add(MessageEmbed.Field("Usage:", UsageReceiver().apply(body).usageNodes.joinToString("\n"), false))
    }

    fun seeAlso(body: UsageReceiver.() -> Unit) {
        fields.add(MessageEmbed.Field("See Also:", UsageReceiver().apply(body).usageNodes.joinToString("\n"), false))
    }

    fun field(name: String, value: String) {
        fields.add(MessageEmbed.Field(name, value, false))
    }

    fun example(vararg values: String, withPrefix: Boolean = true) {
        val stringValues = values.toList()
        val value = (if (withPrefix) stringValues.map { "${commandContext!!.core.config.prefix[0]}$it" } else stringValues)
            .joinToString(prefix = "```\n", separator = "\n", postfix = "\n```")

        fields.add(MessageEmbed.Field("Example:", value, false))
    }
}
