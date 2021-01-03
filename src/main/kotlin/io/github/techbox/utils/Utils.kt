package io.github.techbox.utils

import io.github.techbox.core.listeners.ReactionListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.stream.Stream


inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)

fun formatDuration(time: Long): String {
    if (time < 1000) {
        return "less than a second"
    }
    val days = TimeUnit.MILLISECONDS.toDays(time)
    val hours = TimeUnit.MILLISECONDS.toHours(time) % TimeUnit.DAYS.toHours(1)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1)

    val parts: Iterator<String?> = Stream.of(
        formatUnit(days, "day"), formatUnit(hours, "hour"),
        formatUnit(minutes, "minute"), formatUnit(seconds, "second")
    ).filter { it != null && it.isNotEmpty() }.iterator()

    val sb = StringBuilder()
    var multiple = false
    while (parts.hasNext()) {
        sb.append(parts.next())
        if (parts.hasNext()) {
            multiple = true
            sb.append(", ")
        }
    }
    if (multiple) {
        val last = sb.lastIndexOf(", ")
        sb.replace(last, last + 2, " and ")
    }
    return sb.toString()
}


private fun formatUnit(amount: Long, baseName: String): String {
    if (amount == 0L) {
        return ""
    }
    return if (amount == 1L) {
        "1 $baseName"
    } else amount.toString() + " " + baseName + "s"
}

fun EmbedBuilder.addField(name: String, value: String) = addField(name, value, false)

fun List<*>.joinToStringOrNull(separator: String): String? =
    if (isEmpty()) null else joinToString(separator)

fun Message.onReactionAdd(timeoutSeconds: Long = 120, body: ReactionListener.ReactionOperation.() -> Unit): Unit = run {
    val op = ReactionListener.ReactionOperation()
    body.invoke(op)
    ReactionListener.activeOperations.put(this.idLong, op, timeoutSeconds, TimeUnit.SECONDS)
}