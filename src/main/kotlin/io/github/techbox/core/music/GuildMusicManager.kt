package io.github.techbox.core.music

import io.github.techbox.TechboxLauncher
import io.github.techbox.TechboxLauncher.lavaLink
import io.github.techbox.core.music.requester.TrackScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lavalink.client.io.jda.JdaLink


class GuildMusicManager(val guildId: String) {
    var isAwaitingDeath = false
    val lavalink: JdaLink
        get() = lavaLink.getLink(guildId)
    var leaveTask: Job? = null
    val trackScheduler = TrackScheduler(lavalink, guildId)

    init {
        lavalink.player.addListener(trackScheduler)
    }

    fun destroy() {
        lavalink.player.removeListener(trackScheduler)
        lavalink.resetPlayer()
        lavalink.destroy()
    }

    fun scheduleLeave() {
        if (leaveTask != null) {
            return;
        }
        isAwaitingDeath = true
        leaveTask = TechboxLauncher.core.launch {
            delay(120000)
            val guild = TechboxLauncher.core.shardManager.getGuildById(trackScheduler.guildId)
            if (guild == null) {
                lavalink.destroy()
                return@launch
            }
            isAwaitingDeath = false
            if (trackScheduler.requestedTextChannel != null) {
                trackScheduler.requestedTextChannel!!.sendMessage("Leaving because everyone left.").queue()
            }
            trackScheduler.stop()
        }
    }

    fun cancelLeave() {
        leaveTask?.cancel()
        isAwaitingDeath = false
    }
}