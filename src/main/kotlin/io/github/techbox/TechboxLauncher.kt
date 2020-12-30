package io.github.techbox

import io.github.techbox.core.Techbox
import io.github.techbox.data.Config
import io.github.techbox.utils.logger
import org.slf4j.Logger
import java.lang.Exception
import kotlin.system.exitProcess

object TechboxLauncher {
    private val log: Logger = logger<TechboxLauncher>()
    private val config = Config
    private lateinit var core: Techbox

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

        core = Techbox(config)
        core.start()

        log.info("Finished loading.")
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Techbox is shutting down...")
        })
    }
}