package io.github.techbox

import io.github.techbox.utils.logger
import org.slf4j.Logger
import java.lang.Exception

object TechboxLauncher {
    private val log: Logger = logger<TechboxLauncher>()
    private lateinit var instance: Techbox

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            TechboxLauncher()
        } catch (e: Exception) {
            log.error("Could not launch Techbox", e)
            System.exit(1)
        }
    }

    fun TechboxLauncher() {
        log.info("Starting Techbox v0.0.1")
        instance = Techbox

        log.info("Finished loading.")
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Techbox is shutting down...")
        })
    }
}