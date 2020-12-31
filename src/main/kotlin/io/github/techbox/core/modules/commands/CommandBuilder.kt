package io.github.techbox.core.modules.commands

import io.github.techbox.TechboxLauncher


typealias CommandExecutor = suspend CommandContext.() -> Unit

class CommandBuilder(private val command: Array<String>) {
    var autoRegister = true
    var category: Category? = null

    private var helpReceiver: (HelpReceiver.() -> Unit)? = null
    private var executor: CommandExecutor? = null


    fun build(): ICommand {
        require(executor != null)
        return Command(command, autoRegister, category, helpReceiver, executor!!)
    }

    fun execute(block: CommandExecutor) {
        this.executor = block
    }

    fun help(body: HelpReceiver.() -> Unit) {
        this.helpReceiver = body
    }
}

inline fun command(vararg command: String, block: CommandBuilder.() -> Unit): ICommand {
    val icommand = CommandBuilder(command as Array<String>).apply(block).build()
    if (icommand.autoRegister) {
        val currentClassName = Thread.currentThread().stackTrace[1].className
        val currentModule = TechboxLauncher.core.moduleRegistry.modules.values
            .firstOrNull { it.clazz.name == currentClassName }
        val commandRegistry = TechboxLauncher.core.commandRegistry
        if (currentModule == null) {
            commandRegistry.log.error("Class $currentClassName tried to auto register command but is not a registered module.")
        } else {
            commandRegistry.register(currentModule, icommand)
        }
    }
    return icommand
}