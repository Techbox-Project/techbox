package io.github.techbox.core.listeners

import io.github.techbox.core.modules.commands.CommandProcessor
import io.github.techbox.utils.replyError
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.hooks.EventListener

class CommandListener(
    private val processor: CommandProcessor
) : EventListener {

    override fun onEvent(event: GenericEvent) {
        if (event !is GuildMessageReceivedEvent)
            return

        if (event.author.isBot || event.isWebhookMessage || !event.channel.canTalk())
            return

        try {
            processor.handle(event)
        } catch (e: PermissionException) {
            val permission = e.permission
            event.message.replyError(
                if (permission == Permission.UNKNOWN)
                    "I don't have enough permissions to do that. Is my role higher than the roles I'm trying to assign?"
                else
                    "I need the permission **${permission.name} in order to do that"
            ).queue()
        } catch (e: Throwable) {
            // TODO Exception handling
            e.printStackTrace()
        }

        /* coroutineScope.launch {
            try {
                processor.run()
            } catch (e: Throwable) {
                when (e) {
                    is PermissionException -> {
                        val permission = e.permission
                        event.message.replyError(
                            if (permission == Permission.UNKNOWN)
                                "I don't have enough permissions to do that. Is my role higher than the roles I'm trying to assign?"
                            else
                                "I need the permission **${permission.name} in order to do that"
                        )
                    }
                    else -> {
                        // TODO Exception handling
                        e.printStackTrace()
                    }
                }
            }

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
        } */
    }

}