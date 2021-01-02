package io.github.techbox.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.techbox.TechboxLauncher
import io.github.techbox.data.tables.TechboxGuilds
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseProvider {
    lateinit var database: Database

    fun connect() {
        val config = TechboxLauncher.config

        val hikariConfig = HikariConfig()

        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.jdbcUrl = "jdbc:postgresql://${config.dbHost}:${config.dbPort}/${config.dbName}"
        hikariConfig.username = config.dbUser
        hikariConfig.password = config.dbPassword

        val hikariDataSource = HikariDataSource(hikariConfig)

        database = Database.connect(hikariDataSource)
    }

    fun runMigrations() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                TechboxGuilds
            )
        }
    }
}