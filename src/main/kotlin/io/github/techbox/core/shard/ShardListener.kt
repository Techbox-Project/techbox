package io.github.techbox.core.shard

import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ShardListener(private val shard: Shard) : ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        shard.jda = event.jda
        shard.jda.presence.activity = Activity.streaming("Gg wp [#${shard.id + 1}]", "https://twitch.tv/srliamster")
    }

}