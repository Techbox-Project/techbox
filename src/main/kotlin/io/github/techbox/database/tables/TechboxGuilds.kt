package io.github.techbox.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable


object TechboxGuilds : LongIdTable() {
    val commandPrefix = text("prefix").nullable()
}