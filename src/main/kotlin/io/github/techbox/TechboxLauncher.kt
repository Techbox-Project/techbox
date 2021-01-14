package io.github.techbox

import com.sksamuel.hoplite.ConfigLoader
import io.github.techbox.core.Techbox
import io.github.techbox.core.logging.LogFilter
import io.github.techbox.utils.logger
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import kotlin.system.exitProcess

object TechboxLauncher {

    private val config: TechboxConfig by lazy { loadConfig() }
    private val log: Logger = logger<TechboxLauncher>()

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            runBlocking {
                init()
            }
        } catch (e: Exception) {
            log.error("Could not launch Techbox", e)
            exitProcess(1)
        }
    }

    private fun loadConfig(): TechboxConfig {
        return runCatching {
            ConfigLoader().loadConfigOrThrow<TechboxConfig>("/config.json")
        }.onFailure { error ->
            log.error("Failed to load config: $error")
            log.trace(null, error)
            exitProcess(1)
        }.getOrThrow()
    }

    private suspend fun init() {
        log.info("Starting Techbox v${Techbox.TECHBOX_VERSION}")
        log.info("Filtering all logs below {}", LogFilter.LEVEL)

        val core = Techbox(config)
        core.start()

        log.info("Finished loading.")
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Techbox is shutting down...")
            runBlocking {
                core.close()
            }
        })
    }
}