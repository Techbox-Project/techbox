package io.github.techbox.core.modules.commands

import io.github.techbox.core.modules.ModuleRegistry


class Command(
    override val aliases: Array<String>,
    override val autoRegister: Boolean,
    override val category: Category?,
    private val executor: CommandExecutor
) : ICommand {
    override lateinit var module: ModuleRegistry.ModuleProxy

    override suspend fun execute(ctx: CommandContext) {
        executor.invoke(ctx)
    }
}