package io.github.techbox.core.music.requester

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.techbox.core.music.*
import io.github.techbox.utils.TechboxEmbedBuilder
import io.github.techbox.utils.onReactionAdd
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.TimeUnit
import kotlin.math.min


class AudioLoader(val musicManager: GuildMusicManager, val textChannel: TextChannel, val member: Member?) : AudioLoadResultHandler {
    var failureCount = 0

    override fun trackLoaded(track: AudioTrack) {
        loadSingle(track)
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (playlist.isSearchResult) {
            onSearch(playlist)
            return
        } else {
            val originalSize = musicManager.trackScheduler.queue.size
            var count = musicManager.trackScheduler.queue.size
            for (track in playlist.tracks) {
                if (count > 350) {
                    reply("Could not load the whole playlist, max queue size reached.")
                    break
                } else {
                    loadSingle(track, true)
                    count++
                }
            }
            reply("Loaded ${count - originalSize} songs from ${playlist.name}")
        }
    }

    override fun noMatches() {
        reply("Sorry, couldn't find anything by that search term.")
    }

    override fun loadFailed(exception: FriendlyException) {
        if (failureCount == 0) {
            reply("An error ocurred while loading track\n${exception.message}")
        }
        failureCount++

        exception.printStackTrace()
    }

    fun loadSingle(audioTrack: AudioTrack, silent: Boolean = false) {
        val trackScheduler = musicManager.trackScheduler
        val queue = trackScheduler.queue
        fun reply(content: String) {
            if (!silent) this.reply(content)
        }

        audioTrack.userData = member?.user?.idLong

        if (queue.size > 350) {
            reply("Sorry, your queue is too large. Wait for a few songs to finish before adding more.")
            return
        }

        if (audioTrack.info.length > TimeUnit.HOURS.toMillis(2)) {
            reply("Sorry, that song exceeds the two hour length limit.")
            return
        }

        if (queue.filter { audioTrack.info.uri.equals(it.info.uri) }.count() > 3) {
            reply("There are way too many equal songs in the queue, please play something else.")
            return
        }
        val isEmpty = trackScheduler.queue.isEmpty()

        trackScheduler.queue(audioTrack)
        trackScheduler.requestedChannel = textChannel.idLong

        if (!isEmpty || trackScheduler.currentTrack != null) reply("Loaded song **${audioTrack.info.title}**")
        else if (!silent) sendNowPlayingEmbed(textChannel, audioTrack, member?.user)
    }

    fun onSearch(playlist: AudioPlaylist) {
        val size = min(5, playlist.tracks.size)
        val tracks = playlist.tracks.subList(0, size)
        val content = tracks.joinToString("\n") {
            "**${playlist.tracks.indexOf(it) + 1}.** [${it.info.title}](${it.info.uri}) (${durationMinutes(it.info.length)})"
        }
        val message = textChannel.sendMessage(TechboxEmbedBuilder().apply {
            authorName = "Select a song"
            description = content
        }.build()).complete()
        val reacts = listOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣").subList(0, size)
        reacts.forEach { message.addReaction(it).queue() }
        message.addReaction("❌").queue()
        message.onReactionAdd(30) {
            if (member != null) allowedUsers = listOf(member.user)
            allowedReactions = listOf(listOf("❌"), reacts.subList(0, size)).flatten()
            onAdd {
                val emoji = it.reactionEmote.asReactionCode
                message.delete().queue()
                if (emoji == "❌") {
                    return@onAdd false
                }
                val index = reacts.indexOf(emoji)
                val track = tracks[index]
                loadSingle(track)
            }
        }
    }

    fun reply(content: String) {
        textChannel.sendMessage(content).queue()
    }
}