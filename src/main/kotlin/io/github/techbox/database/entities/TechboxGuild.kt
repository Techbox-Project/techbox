package io.github.techbox.database.entities

import io.github.techbox.database.tables.TechboxGuilds
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID


class TechboxGuild(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<TechboxGuild>(TechboxGuilds)

    val guildId = this.id.value
    var commandPrefix by TechboxGuilds.commandPrefix
}