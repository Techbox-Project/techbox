package io.github.techbox.core.shard

import io.github.techbox.core.TechboxEventManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.annotation.Nonnull


class Shard(private val id: Int) {
    private lateinit var jda: JDA
    val manager = TechboxEventManager()

    val listener: EventListener = object : ListenerAdapter() {
        @Synchronized
        override fun onReady(@Nonnull event: ReadyEvent) {
            jda = event.jda
            jda.presence.activity = Activity.playing("Hi, I'm Techbox [$id]")
        }
    }

}