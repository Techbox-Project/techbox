package io.github.techbox

import io.github.techbox.core.Techbox
import io.github.techbox.core.logging.LogFilter
import io.github.techbox.core.music.TechboxAudioManager
import io.github.techbox.data.Config
import io.github.techbox.utils.logger
import lavalink.client.io.LavalinkLoadBalancer
import lavalink.client.io.jda.JdaLavalink
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import org.slf4j.Logger
import java.net.URI
import kotlin.system.exitProcess


object TechboxLauncher {
    private val log: Logger = logger<TechboxLauncher>()
    lateinit var lavaLink: JdaLavalink
    lateinit var audioManager: TechboxAudioManager
    val config = Config
    lateinit var core: Techbox

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            TechboxLauncher()
        } catch (e: Exception) {
            log.error("Could not launch Techbox", e)
            exitProcess(1)
        }
    }

    fun TechboxLauncher() {
        log.info("Starting Techbox v0.0.1")
        log.info("Filtering all logs below {}", LogFilter.LEVEL)

        RestAction.setDefaultFailure(
            ErrorResponseException.ignore(
                RestAction.getDefaultFailure(),
                ErrorResponse.UNKNOWN_MESSAGE
            )
        )

        lavaLink = JdaLavalink(
            config.clientId,
            config.shardCount
        ) { shardId -> core.shardManager.getShardById(shardId) }

        for (node in config.lavalinkNodes) {
            lavaLink.addNode(URI(node), config.lavalinkPass ?: "")
        }

        lavaLink.loadBalancer.addPenalty(LavalinkLoadBalancer.Penalties::getPlayerPenalty)
        lavaLink.loadBalancer.addPenalty(LavalinkLoadBalancer.Penalties::getCpuPenalty)


        core = Techbox(config)
        audioManager = TechboxAudioManager()

        core.start()

        log.info("Finished loading.")
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Destroying all active players...")
            for (player in audioManager.musicManagers.entries) {
                player.value.lavalink.destroy()
            }

            log.info("Techbox is shutting down...")
        })
    }
}