@file:Module

package io.github.techbox.modules.info

import io.github.techbox.TechboxLauncher
import io.github.techbox.core.modules.Module
import io.github.techbox.core.modules.commands.Category
import io.github.techbox.core.modules.commands.CommandContext
import io.github.techbox.core.modules.commands.command
import io.github.techbox.utils.nameAndDiscriminator

val commandRegistry = TechboxLauncher.core.commandRegistry

fun onLoad() {
    command("help", "h") {
        category = Category.INFO

        execute {
            sendHelp()
        }
    }
}

fun CommandContext.sendHelp() {
    replyEmbed {
        setAuthor("Techbox | Help")

        setDescription(
            """
            Here's all the cmmands I have available.
            To check command usage, use `${usedPrefix}help <command>`
        """
        )

        setFooter(
            "${commandRegistry.commands.size} commands | Requested by ${author.nameAndDiscriminator}",
            author.effectiveAvatarUrl
        )

        Category.values().forEach { category ->
            var count = 0
            val commands = commandRegistry.commands
                .map { it.value }
                .filter { it.category == category }
                .map { it.aliases[0] }
                .apply { ++count }
                .sorted()
                .toList()

            if (command.isNotEmpty()) {
                addField(
                    "${category.categoryName} ($count)",
                    commands.joinToString(prefix = "`", separator = "` `", postfix = "`"),
                    false
                )
            }
        }
    }
}