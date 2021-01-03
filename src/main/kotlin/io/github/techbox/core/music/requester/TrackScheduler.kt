package io.github.techbox.core.music.requester

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import io.github.techbox.TechboxLauncher
import io.github.techbox.core.music.getTrackRequester
import io.github.techbox.core.music.sendNowPlayingEmbed
import lavalink.client.io.Link
import lavalink.client.player.IPlayer
import lavalink.client.player.event.PlayerEventListenerAdapter
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.ConcurrentLinkedDeque

class TrackScheduler(private var audioPlayer_: Link?, val guildId: String) : PlayerEventListenerAdapter() {
    val audioPlayer: Link by lazy { audioPlayer_ ?: TechboxLauncher.lavaLink.getLink(guildId) }
    val musicPlayer: IPlayer
        get() = audioPlayer.player

    val queue: ConcurrentLinkedDeque<AudioTrack> = ConcurrentLinkedDeque()
    var currentTrack: AudioTrack? = null
    var previousTrack: AudioTrack? = null
    var requestedChannel: Long = 0
    val requestedTextChannel: TextChannel?
        get() {
            if (requestedChannel == 0L)
                return null

            return TechboxLauncher.core.shardManager.getTextChannelById(requestedChannel);
        }

    private val voteSkips: MutableList<String> = mutableListOf()
    private val repeatMode: Repeat? = null

    fun queue(track: AudioTrack) {
        if (musicPlayer.playingTrack != null) {
            queue.offer(track)
        } else {
            musicPlayer.playTrack(track)
            currentTrack = track
        }
    }

    fun nextTrack(force: Boolean, skip: Boolean) {
        voteSkips.clear()
        if (repeatMode === Repeat.SONG && currentTrack != null && !force) {
            queue(currentTrack!!.makeClone())
        } else {
            if (currentTrack != null) {
                previousTrack = currentTrack
            }
            currentTrack = queue.poll()

            //This actually reads wrongly, but current = next in this context, since we switched it already.
            if (currentTrack != null) {
                musicPlayer.playTrack(currentTrack)
            }
            if (skip) {
                onTrackStart()
            }
            if (repeatMode === Repeat.QUEUE && previousTrack != null) {
                queue(previousTrack!!.makeClone())
            }
        }
    }

    fun onTrackStart() {
        if (currentTrack == null) {
            return onStop()
        }
        if (requestedChannel == 0L || requestedTextChannel == null) return
        val voiceState = requestedTextChannel?.guild?.selfMember?.voiceState
        if (voiceState == null || voiceState.channel == null) {
            audioPlayer.destroy()
            return
        }
        val voiceChannel = voiceState.channel!!


        sendNowPlayingEmbed(
            requestedTextChannel!!, currentTrack!!,
            getTrackRequester(currentTrack!!, voiceChannel.guild)?.user
        )
    }

    fun onStop() {
        if (audioPlayer.player.playingTrack != null) audioPlayer.player.stopTrack()

        voteSkips.clear()
        if (requestedTextChannel != null) requestedTextChannel!!.sendMessage("Finished").queue()
        requestedChannel = 0
        currentTrack = null
        previousTrack = null
        TechboxLauncher.audioManager.resetMusicManagerFor(guildId)
    }

    override fun onTrackEnd(player: IPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack(false, false)
            onTrackStart()
        }
    }

    override fun onTrackException(player: IPlayer, track: AudioTrack, exception: Exception) {
        if (requestedTextChannel != null && requestedTextChannel!!.canTalk()) {
            requestedTextChannel!!.sendMessage("Error happened").queue()
        }
    }

    fun stop() {
        queue.clear()
        onStop()
    }

    enum class Repeat {
        SONG, QUEUE
    }
}