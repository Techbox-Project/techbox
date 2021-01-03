package io.github.techbox.modules.music

import io.github.techbox.TechboxLauncher
import io.github.techbox.core.modules.Module
import io.github.techbox.core.modules.commands.command
import net.dv8tion.jda.api.entities.TextChannel
import java.net.URL

@Module
class MusicModule {

    fun onLoad() {
        command("play") {
            execute {
                if (args.isEmpty()) {
                    return@execute reply("You need to specify something to play.")
                }
                var query = args.joinToString(" ")

                try {
                    URL(query)
                } catch (e: Exception) {
                    if (query.startsWith("soundcloud")) query =
                        "scsearch: $query".replace("soundcloud ", "") else query =
                        "ytsearch: $query"
                }

                TechboxLauncher.audioManager.loadAndPlay(message.channel as TextChannel, member!!, query)
                message.delete().queue()
            }
        }
    }
}