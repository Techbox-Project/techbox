package io.github.techbox.core.modules.commands

import io.github.techbox.core.modules.ModuleRegistry
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed


interface ICommand {
    var module: ModuleRegistry.ModuleProxy
    val aliases: Array<String>
    val autoRegister: Boolean
    val category: Category?
    val discordPermissions: List<Permission>
    suspend fun execute(ctx: CommandContext)
    fun onHelp(): MessageEmbed
}