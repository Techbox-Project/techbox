package io.github.techbox.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable


object TechboxGuilds : LongIdTable() {
    val commandPrefix = text("prefix").nullable()
}