package io.github.techbox.core.listeners

import io.github.techbox.TechboxLauncher
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.*
import net.dv8tion.jda.api.hooks.EventListener


class VoiceChannelListener : EventListener {

    override fun onEvent(event: GenericEvent) {
        if (event is GuildVoiceMoveEvent) {
            onGuildVoiceMove(event)
        } else if (event is GuildVoiceJoinEvent) {
            onGuildVoiceJoin(event)
        } else if (event is GuildVoiceLeaveEvent) {
            onGuildVoiceLeave(event)
        } else if (event is GuildVoiceMuteEvent) {
            onGuildVoiceMute(event)
        } else if (event is GuildVoiceGuildDeafenEvent) {
            onGuildVoiceGuildDeafen(event)
        }
    }

    private fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        if (event.channelJoined.members.contains(event.guild.selfMember)) {
            onJoin(event.channelJoined)
        }
        if (event.channelLeft.members.contains(event.guild.selfMember)) {
            onLeave(event.channelLeft)
        }
    }

    private fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.channelJoined.members.contains(event.guild.selfMember)) {
            onJoin(event.channelJoined)
        }
    }

    private fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.channelLeft.members.contains(event.guild.selfMember)) {
            onLeave(event.channelLeft)
        }
    }

    private fun onGuildVoiceMute(event: GuildVoiceMuteEvent) {
        if (event.member.user.idLong != event.jda.selfUser.idLong) return

        val voiceState = event.voiceState
        if (!voiceState.inVoiceChannel()) {
            return
        }

        val musicManager = TechboxLauncher.audioManager.getMusicManager(event.guild)
        if (event.isMuted) {
            val scheduler = musicManager.trackScheduler
            if (scheduler.currentTrack != null && scheduler.requestedTextChannel != null) {
                val textChannel = scheduler.requestedTextChannel
                if (textChannel!!.canTalk()) {
                    textChannel.sendMessage("Paused").queue()
                }
                musicManager.lavalink.player.isPaused = true
            }
        } else {
            if (!isAlone(voiceState.channel!!) && musicManager.trackScheduler.currentTrack != null) {
                musicManager.lavalink.player.isPaused = false
            }
        }
    }

    private fun onJoin(vc: VoiceChannel) {
        val vs = vc.guild.selfMember.voiceState
        if (vs == null || !vs.inVoiceChannel()) {
            return
        }
        if (!vs.isGuildDeafened) vs.guild.selfMember.deafen(true).queue()

        if (!isAlone(vc)) {
            val musicManager = TechboxLauncher.audioManager.getMusicManager(vc.guild)
            val scheduler = musicManager.trackScheduler
            if (scheduler.currentTrack != null) {
                if (musicManager.isAwaitingDeath) {
                    val textChannel = scheduler.requestedTextChannel
                    if (textChannel != null && textChannel.canTalk() /* TODO && vcRatelimiter.process(vc.guild.id)*/) {
                        textChannel.sendMessage("Someone joined, resuming.").queue()
                    }
                }
            }
            musicManager.cancelLeave()
            musicManager.lavalink.player.isPaused = false
        }
    }

    private fun onLeave(vc: VoiceChannel) {
        val vs = vc.guild.selfMember.voiceState
        if (vs == null || !vs.inVoiceChannel()) {
            return
        }

        if (isAlone(vc)) {
            val musicManager = TechboxLauncher.audioManager.getMusicManager(vc.guild)
            val scheduler = musicManager.trackScheduler
            if (scheduler.currentTrack != null && scheduler.requestedTextChannel != null) {
                val textChannel = scheduler.requestedTextChannel
                if (textChannel!!.canTalk() /* TODO && vcRatelimiter.process(vc.guild.id)*/) {
                    textChannel.sendMessage("I was left alone so I'm pausing until someone comes back.").queue()
                }
            }
            musicManager.scheduleLeave()
            musicManager.lavalink.player.isPaused = true
        }
    }

    private fun onGuildVoiceGuildDeafen(ev: GuildVoiceGuildDeafenEvent) {
        if (ev.member.idLong == ev.guild.selfMember.idLong) {
            if (!ev.isGuildDeafened) {
                ev.member.deafen(true).queue()
            }
        }
    }

    private fun isAlone(vc: VoiceChannel): Boolean {
        return vc.members.stream().allMatch { m -> m.user.isBot }
    }

}