package io.github.techbox.data

import io.github.techbox.utils.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Paths


object Config {

    private val log: Logger = LoggerFactory.getLogger(Config::class.java)

    var token: String? = null
    var prefix = arrayOf("t+")
    var shardCount = 0
    var dbHost = "localhost"
    var dbPort = 5432
    val dbName = "techbox"
    var dbUser = "techbox"
    var dbPassword = "techbox"

    init {
        val configPath = Paths.get("config.json")
        if (!configPath.toFile().exists()) {
            try {
                if (configPath.toFile().createNewFile()) {
                    log.info("Generated a new config file, please fill it with the appropriate values.")
                    FileUtils.write(configPath, FileUtils.jsonMapper.writeValueAsString(this))
                } else {
                    log.warn("Could not create config file.")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                System.exit(1)
            }
            System.exit(0)
        }

        try {
            val config = FileUtils.jsonMapper.readValue(FileUtils.read(configPath), Config::class.java)
            this.token = config.token
            this.prefix = config.prefix
            this.shardCount = config.shardCount
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}