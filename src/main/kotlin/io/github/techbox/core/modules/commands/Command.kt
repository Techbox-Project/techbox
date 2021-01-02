package io.github.techbox.core.modules.commands

import io.github.techbox.core.modules.ModuleRegistry
import io.github.techbox.utils.TechboxEmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color


class Command(
    override val aliases: Array<String>,
    override val autoRegister: Boolean,
    override val category: Category?,
    override val selfPermissions: List<Permission>,
    override val discordPermissions: List<Permission>,
    val helpHandler: (HelpReceiver.() -> Unit)?,
    private val executor: CommandExecutor
) : ICommand {
    override lateinit var module: ModuleRegistry.ModuleProxy

    override suspend fun execute(ctx: CommandContext) {
        selfPermissions.forEach {
            if (!ctx.message.guild.selfMember.hasPermission(it)) {
                return ctx.reply("❌Hey, you need to give me the permission **${it.name}** for me to be able to do that.")
            }
        }
        if (discordPermissions.isNotEmpty() && ctx.member == null) {
            return ctx.reply("❌Is everything alright? Something weird happened and I couldn't check your permissions for this command.")
        }
        discordPermissions.forEach {
            if (!ctx.member!!.hasPermission(it)) {
                return ctx.reply("❌You need the permission **${it.name}** to use this command.")
            }
        }
        executor.invoke(ctx)
    }

    override fun onHelp(context: CommandContext?): MessageEmbed {
        val builder = TechboxEmbedBuilder().apply {
            val help = if (helpHandler != null) HelpReceiver(context).apply(helpHandler) else HelpReceiver(context)
            color = Color.GREEN
            authorName = help.title ?: "${aliases[0].capitalize()} Command"
            val effectiveAliases = if (help.aliases.isEmpty()) aliases else help.aliases
            if (effectiveAliases.size > 1) {
                field(
                    "Aliases: ",
                    effectiveAliases.asSequence().drop(1).joinToString("` `", "`", "`")
                )
            }
            if (help.fields.isEmpty()) {
                description = "No information has been provided for this command."
            } else {
                help.fields.forEach { fields.add(it) }
            }
        }

        return builder.build()
    }
}