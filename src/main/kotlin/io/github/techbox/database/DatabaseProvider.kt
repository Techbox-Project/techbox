package io.github.techbox.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.techbox.core.Techbox
import io.github.techbox.database.tables.TechboxGuilds
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DatabaseProvider(private val techbox: Techbox) {

    lateinit var database: Database

    fun connect() {
        val config = techbox.config
        database = Database.connect(HikariDataSource(HikariConfig().apply {
            driverClassName = config.database.driver
            jdbcUrl = config.database.jdbcUrl
            username = config.database.user
            password = config.database.password
        }))
    }

    suspend fun runMigrations() {
        newSuspendedTransaction(db = database) {
            SchemaUtils.createMissingTablesAndColumns(TechboxGuilds)
        }
    }
}