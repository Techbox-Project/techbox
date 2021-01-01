package io.github.techbox.core.modules.commands

import io.github.techbox.core.modules.ModuleRegistry
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color


class Command(
    override val aliases: Array<String>,
    override val autoRegister: Boolean,
    override val category: Category?,
    override val discordPermissions: List<Permission>,
    val helpHandler: (HelpReceiver.() -> Unit)?,
    private val executor: CommandExecutor
) : ICommand {
    override lateinit var module: ModuleRegistry.ModuleProxy

    override suspend fun execute(ctx: CommandContext) {
        if (discordPermissions.isNotEmpty() && ctx.member == null) {
            return ctx.reply("❌Is everything alright? Something weird happened and I couldn't check your permissions for this command.")
        }
        discordPermissions.forEach {
            if (!ctx.member!!.hasPermission(it)) {
                return ctx.reply("❌You need the permission `${it.name}` to use this command.")
            }
        }
        executor.invoke(ctx)
    }

    override fun onHelp(): MessageEmbed {
        val builder = EmbedBuilder()
        val help = if (helpHandler != null) HelpReceiver().apply(helpHandler) else HelpReceiver()
        builder.setColor(Color.GREEN)
        builder.setAuthor(help.title ?: "${aliases[0].capitalize()} Command")
        val effectiveAliases = if (help.aliases.isEmpty()) aliases else help.aliases
        if (effectiveAliases.size > 1) {
            builder.addField(
                "Aliases: ",
                effectiveAliases.asSequence().drop(1).joinToString("` `", "`", "`"),
                false)
        }
        if (help.fields.isEmpty()) {
            builder.setDescription("No information has been provided for this command.")
        } else {
            help.fields.forEach { builder.addField(it) }
        }
        return builder.build()
    }
}