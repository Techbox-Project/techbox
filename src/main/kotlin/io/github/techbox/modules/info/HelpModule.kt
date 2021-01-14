package io.github.techbox.modules.info

import io.github.techbox.core.modules.Module
import io.github.techbox.core.modules.commands.*
import io.github.techbox.utils.TechboxEmbedBuilder
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

@Module
class HelpModule {

    fun onLoad() {
        command("help", "h") {
            category = Category.INFO

            execute {
                if (args.isEmpty()) {
                    sendHelp()
                } else {
                    findHelp(args.joinToString(" "))
                }
            }

            help {
                title = "Help Command"
                aliases = arrayOf("help", "h")
                description = "Here's some help about helping."

                usages {
                    usage("help", "Lists all commands.")
                    usage("help <command>", "Displays a command's help.")
                    usage("help <category>", "Displays a category's help.")
                }
            }
        }
    }

    fun CommandContext.sendHelp() {
        replyEmbed {
            authorName = "Techbox | Help"

            description =
                """
            Here's all the commands I have available.
            To check command usage, use `${usedPrefix}help <command>`
        """

            footer = "${core.commandRegistry.commands.size} commands | Requested by ${author.asTag}"
            footerIcon = author.effectiveAvatarUrl

            Category.values().forEach { category ->
                var count = 0
                val commands = core.commandRegistry.commands
                    .map { it.value }
                    .filter { it.category == category }
                    .map { it.aliases[0] }
                    .apply { ++count }
                    .sorted()
                    .toList()

                if (command.isNotEmpty()) {
                    field(
                        "${category.categoryName} ($count)",
                        commands.joinToString(prefix = "`", separator = "` `", postfix = "`"),
                        false
                    )
                }
            }
        }
    }

    fun CommandContext.findAnything(args: String): Any? {
        return core.commandRegistry.commands[args.toLowerCase()]
            ?: core.commandRegistry.commands[core.commandRegistry.aliases.getOrDefault(args.toLowerCase(), "")]
            ?: Category.values()
                .firstOrNull {
                    it.name.equals(args, ignoreCase = true) || it.categoryName.equals(
                        args,
                        ignoreCase = true
                    )
                }
    }

    fun CommandContext.findHelp(args: String) {
        var toHelp = findAnything(args)
        var extra: String? = null
        if (toHelp == null) {
            val match: BoundExtractedResult<String> = FuzzySearch.extractOne(
                args.toLowerCase(), core.commandRegistry.helpPossibilities
            ) { it -> it }
            if (match.score > 70) {
                val newArg = match.referent
                toHelp = findAnything(newArg)
                if (toHelp != null) extra = "Couldn't find anything named `$args`, did you mean `$newArg`?"
            }
        }
        var help: MessageEmbed? = null

        when (toHelp) {
            is ICommand, is Command -> {
                help = (toHelp as ICommand).onHelp(this)
            }
            is Category -> {
                help = TechboxEmbedBuilder().apply {
                    authorName = "Techbox | Help: ${toHelp.categoryName}"
                    description = """
                        Here's all the category's commands.
                        To check the command usage, type `${core.config.prefix}help <command>`.
                    """

                    val commands = core.commandRegistry.commands
                        .map {
                            it.value
                        }
                        .filter { it.category == toHelp }
                        .map { it.aliases[0] }
                        .sorted()
                        .toList()

                    if (commands.isNotEmpty()) {
                        field("Commands:", commands.joinToString(prefix = "`", separator = "` `", postfix = "`"))
                    } else {
                        description = "Nobody here but us chickens."
                    }

                    footer = "${commands.size} commands | Requested by ${author.asTag}"
                    footerIcon = author.effectiveAvatarUrl
                }.build()
            }
            null -> {
                reply("❌There's no command or category with that name!")
                return
            }
        }
        if (help != null) reply(MessageBuilder().setEmbed(help).append(extra ?: "").build())
        else reply("❌Sorry, there is no help available for that command.")
    }

}
