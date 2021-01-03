package io.github.techbox.core.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import io.github.techbox.core.music.requester.AudioLoader
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.ConcurrentHashMap


class TechboxAudioManager {
    val musicManagers: MutableMap<String, GuildMusicManager> = ConcurrentHashMap()
    private val playerManager = DefaultAudioPlayerManager()

    init {
        playerManager.registerSourceManager(YoutubeAudioSourceManager(true))
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        playerManager.registerSourceManager(BandcampAudioSourceManager())
        playerManager.registerSourceManager(VimeoAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BeamAudioSourceManager())
    }

    suspend fun loadAndPlay(channel: TextChannel, member: Member, query: String) {
        val connected = connectToVoiceChannelAsync(member, channel).await()
        if (connected) {
            val musicManager = getMusicManager(channel.guild)
            val scheduler = musicManager.trackScheduler

            scheduler.musicPlayer.isPaused = false

            /* TODO if (scheduler.getQueue().isEmpty()) {
                 scheduler.setRepeatMode(null)
             }
 */
            val loader = AudioLoader(musicManager, channel, member)
            playerManager.loadItemOrdered(musicManager, query, loader)
        }
    }

    fun getMusicManager(guild: Guild): GuildMusicManager {
        return musicManagers.computeIfAbsent(guild.id) { GuildMusicManager(guild.id) }
    }

    fun resetMusicManagerFor(guildId: String) {
        val previousManager = musicManagers[guildId];
        previousManager?.destroy()
        musicManagers.remove(guildId);
    }

}