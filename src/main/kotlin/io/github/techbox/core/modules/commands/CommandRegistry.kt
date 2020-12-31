package io.github.techbox.core.modules.commands

import io.github.techbox.core.modules.ModuleRegistry.ModuleProxy
import io.github.techbox.utils.logger
import org.slf4j.Logger
import java.util.*


class CommandRegistry {
    val log: Logger = logger<CommandRegistry>()
    val commands: HashMap<String, ICommand> = HashMap()
    val aliases: HashMap<String, String> = HashMap()

    fun register(module: ModuleProxy, command: ICommand) {
        command.module = module
        require(commands.putIfAbsent(command.aliases[0], command) == null) { "Duplicate command ${command.aliases[0]}" }

        for (alias in command.aliases.drop(1)) {
            require(aliases.putIfAbsent(alias, command.aliases[0]) == null) { "Duplicate alias $alias" }
        }
    }

    suspend fun execute(ctx: CommandContext): Boolean {
        val commandName = ctx.command.toLowerCase()
        val command = commands[commandName] ?: commands[aliases.getOrDefault(commandName, "")]
        if (command != null) {
            command.execute(ctx)
            return true
        } else if (ctx.usedPrefix == ctx.message.jda.selfUser.asMention) { // https://github.com/meew0/discord-bot-best-practices
            ctx.message.reply("❓Unknown command").queue()
        }
        return false
    }


}