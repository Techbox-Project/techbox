package io.github.techbox

data class TechboxConfig(
    val prefix: String,
    val token: String,
    val shardCount: Int,
    val database: Database
)

data class Database(
    val host: String,
    val port: Short,
    val name: String,
    val user: String,
    val password: String
) {

    val driver: String get() = "org.postgresql.Driver"
    val jdbcUrl: String get() = "jdbc:postgresql://$host:$port/$name"

}