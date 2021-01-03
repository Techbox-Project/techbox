package io.github.techbox.core.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.techbox.TechboxLauncher
import io.github.techbox.utils.TechboxEmbedBuilder
import io.github.techbox.utils.logger
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import lavalink.client.io.Link
import lavalink.client.io.jda.JdaLink
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import java.awt.Color
import java.util.concurrent.TimeUnit

val log = logger<TechboxAudioManager>()

fun joinVoiceChannelAsync(manager: JdaLink, channel: VoiceChannel) =
    TechboxLauncher.core.async(start = CoroutineStart.LAZY) {
        manager.connect(channel)
    }

fun openAudioConnectionAsync(
    voiceChannel: VoiceChannel,
    textChannel: TextChannel,
    link: JdaLink
) =
    TechboxLauncher.core.async(start = CoroutineStart.LAZY) {
        val selfMember = voiceChannel.guild.selfMember
        fun reply(content: String) {
            textChannel.sendMessage(content).queue()
        }

        if (voiceChannel.userLimit > 0 && voiceChannel.userLimit <= voiceChannel.members.size && !selfMember.hasPermission(
                voiceChannel,
                Permission.MANAGE_CHANNEL
            )
        ) {
            reply("Sorry, that voice channel is full.")
            return@async
        }

        try {
            joinVoiceChannelAsync(link, voiceChannel).await()
        } catch (e: NullPointerException) {
            reply("An error occurred while trying to join the channel, are you sure it exists?")
        }
    }


fun connectToVoiceChannelAsync(member: Member, textChannel: TextChannel): Deferred<Boolean> =
    TechboxLauncher.core.async(start = CoroutineStart.LAZY) {
        val voiceChannel = member.voiceState?.channel
        val guild = textChannel.guild
        fun reply(content: String) {
            textChannel.sendMessage(content).queue()
        }

        if (voiceChannel == null) {
            reply("You need to be connected to a voice channel.")
            return@async false
        }

        listOf(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_DEAF_OTHERS).forEach {
            if (!guild.selfMember.hasPermission(it)) {
                reply("I need the permission **${it.name}** in order to play music")
                return@async false
            }
        }

        val link = TechboxLauncher.audioManager.getMusicManager(guild).lavalink
        var weirdError = false

        if (link.state == Link.State.CONNECTED || link.state == Link.State.CONNECTING) {
            if (link.lastChannel != null && link.lastChannel != voiceChannel.id) {
                val vc = guild.getVoiceChannelById(link.lastChannel!!)
                val status = if (link.state == Link.State.CONNECTED) "connected" else "trying to connect"
                if (vc != null) {
                    reply("I'm already $status to another voice channel.")
                    return@async false
                } else {
                    weirdError = true
                }
            }
        }

        if (link.state != Link.State.CONNECTED && link.state != Link.State.CONNECTING || weirdError) {
            if (weirdError) {
                log.debug("We seemed to hit a Lavalink/JDA bug? Null voice channel, but {} state.", link.state)
            }
            val conMessage = textChannel.sendMessage("Connecting to ${voiceChannel.name}...").complete()
            openAudioConnectionAsync(voiceChannel, textChannel, link).await()
            conMessage.delete().complete()
        }

        true
    }

fun durationMinutes(length: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(length)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(length) - hours * 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(length) - minutes * 60 - hours * 60 * 60
    val hoursString = if (hours > 9) hours else "0$hours"
    val minutesString = if (minutes > 9) minutes else "0$minutes"
    val secondsString = if (seconds > 9) seconds else "0$seconds"
    val result = "$hoursString:$minutesString:$secondsString"
    return if (result.startsWith("00:")) result.substring(3) else result
}

fun sendNowPlayingEmbed(textChannel: TextChannel, track: AudioTrack, user: User?) {
    textChannel.sendMessage(TechboxEmbedBuilder().apply {
        title = "Now Playing:"
        description = "**[${track.info.title}](${track.info.uri})** (${durationMinutes(track.info.length)})"
        color = Color.GREEN
        if (user != null) {
            footer = "Requested by ${user.asTag}"
            footerIcon = user.effectiveAvatarUrl
        }
    }.build()).queue {
        it.addReaction("⭐").queue()
        it.addReaction("\uD83D\uDD02").queue()
    }
}

fun getTrackRequester(track: AudioTrack, guild: Guild): Member? {
    var user: Member? = null
    if (track.userData != null) {
        // Retrieve member instead of user, so it gets cached.
        try {
            user = guild.retrieveMemberById(track.userData.toString(), false).complete()
        } catch (ignored: Exception) {
        }
    }
    return user
}