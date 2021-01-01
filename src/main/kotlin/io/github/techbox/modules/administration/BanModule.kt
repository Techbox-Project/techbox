package io.github.techbox.modules.administration

import io.github.techbox.core.modules.Module
import io.github.techbox.core.modules.commands.Category
import io.github.techbox.core.modules.commands.command
import io.github.techbox.utils.extractUserFromString
import io.github.techbox.utils.onReactionAdd
import net.dv8tion.jda.api.Permission

@Module
class BanModule {

    fun onLoad() {
        command("ban", "banir") {
            category = Category.MODERATION
            selfPermissions = listOf(Permission.BAN_MEMBERS)
            discordPermissions = listOf(Permission.BAN_MEMBERS)

            execute {
                val userName = args.getOrNull(0)
                    ?: return@execute reply("❌ You need to specify a user. Check `${usedPrefix}help ban` for more information.")
                val user = extractUserFromString(userName, message.mentionedUsers, message.guild)
                    ?: return@execute reply("❌ Sorry, I couldn't find any user named $userName")
                var reason = args.drop(1).joinToString(" ")
                val message =
                    replyBlocking("❔ Are you sure you want to ban ${user.asMention} (${user.asTag} / ${user.id})" +
                            "${if (reason.isNotEmpty()) " with the reason `$reason`" else ""}?")
                if (reason.isEmpty()) reason = "No reason specified."

                message.addReaction("✅").queue()
                message.addReaction("❌").queue()
                message.onReactionAdd {
                    allowedUsers = listOf(author)
                    allowedReactions = listOf("✅", "❌")
                    onAdd {
                        message.delete().queue()
                        when (it.reactionEmote.asReactionCode) {
                            "✅" -> {
                                reason += " - Banned by ${author.asTag} (${author.id})"
                                message.guild.ban(user, 0, reason).complete()
                                reply("✅ Gone, reduced to atoms.")
                            }
                            "❌" -> {
                                reply("❌ Cancelled, that was a close call.")
                            }
                        }
                    }
                    onExpire {
                        message.clearReactions().queue()
                    }
                }
            }

            help {
                aliases = arrayOf("ban")
                usages {
                    usage("ban @user", "Bans a user from your server")
                    usage("ban @user [reason]", "Bans a user from your server with the specified reason")
                }
                example(
                    "ban ${commandContext?.author?.asTag ?: "@Someone"} Being too cute",
                    "ban ${commandContext?.author?.id ?: "793868886478815252"}"
                )
            }
        }
    }

}