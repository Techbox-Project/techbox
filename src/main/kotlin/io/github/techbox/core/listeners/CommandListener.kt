package io.github.techbox.core.listeners

import io.github.techbox.TechboxLauncher
import io.github.techbox.core.modules.commands.CommandProcessor
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.hooks.EventListener
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


class CommandListener(private val processor: CommandProcessor) : EventListener {

    override fun onEvent(event: GenericEvent) {
        if (event is GuildMessageReceivedEvent) {
            if (event.author.isBot || event.isWebhookMessage || !event.channel.canTalk()) return
            TechboxLauncher.core.launch {
                try {
                    withTimeout(300000) {
                        try {
                            newSuspendedTransaction {
                                processor.run(event)
                            }
                        } catch (e: PermissionException) {
                            if (e.permission != Permission.UNKNOWN) {
                                event.message.reply(
                                    "❌I need the permission **${e.permission.name} in order to do that."
                                ).queue()
                            } else {
                                event.message.reply(
                                    "❌I don't have enough permissions to do that. Is my role higher than the roles I'm trying to assign?"
                                ).queue()
                            }
                        } catch (e: Exception) {
                            // TODO Exception handling
                            e.printStackTrace()
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    // TODO log message to admins
                }
            }
        }
    }

}