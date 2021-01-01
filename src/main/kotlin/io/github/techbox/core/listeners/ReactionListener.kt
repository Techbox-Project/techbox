package io.github.techbox.core.listeners

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.jodah.expiringmap.ExpiringMap


class ReactionListener : EventListener {

    override fun onEvent(event: GenericEvent) {
        if (event is GenericMessageReactionEvent) {
            val operation = activeOperations[event.messageIdLong]
            if (operation != null) {
                if (operation.allowedUserIds.isNotEmpty() && !operation.allowedUserIds.contains(event.userIdLong)) return
                if (operation.allowedReactions.isNotEmpty() &&
                    !operation.allowedReactions.contains(
                        if (event.reactionEmote.isEmoji) event.reactionEmote.asReactionCode else event.reactionEmote.id
                    )
                ) return
                var keep: Any = false
                when (event) {
                    is MessageReactionAddEvent -> keep = operation.onAdd.invoke(event)
                    is MessageReactionRemoveEvent -> keep = operation.onRemove.invoke(event)
                    is MessageReactionRemoveAllEvent -> keep = operation.onRemoveAll.invoke(event)
                }
                if (keep !is Boolean || !keep) {
                    activeOperations.remove(event.messageIdLong)
                }
            }
        }
    }

    class ReactionOperation {
        var allowedReactions: List<String> = mutableListOf()
        val allowedUserIds: MutableList<Long> = mutableListOf()
        var allowedUsers: List<User> = mutableListOf()
            set(value) {
                allowedUserIds.addAll(value.map { it.idLong })
                field = value
            }

        lateinit var onAdd: (MessageReactionAddEvent) -> Any
        lateinit var onRemove: (MessageReactionRemoveEvent) -> Any
        lateinit var onRemoveAll: (MessageReactionRemoveAllEvent) -> Any
        var onExpire: (() -> Unit)? = null

        fun onAdd(body: (MessageReactionAddEvent) -> Any) {
            onAdd = body
        }

        fun onRemove(body: (MessageReactionRemoveEvent) -> Any) {
            onRemove = body
        }

        fun onRemoveAll(body: (MessageReactionRemoveAllEvent) -> Any) {
            onRemoveAll = body
        }

        fun onExpire(body: () -> Unit) {
            onExpire = body
        }
    }

    companion object {
        val activeOperations: ExpiringMap<Long, ReactionOperation> = ExpiringMap.builder()
            .asyncExpirationListener { _: Long, value: ReactionOperation -> (value.onExpire?.invoke()) }
            .variableExpiration()
            .build()
    }
}
