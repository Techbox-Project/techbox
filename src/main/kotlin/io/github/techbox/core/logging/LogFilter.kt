package io.github.techbox.core.logging


import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply


class LogFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }
        return if (event.level.isGreaterOrEqual(LEVEL)) {
            FilterReply.NEUTRAL
        } else {
            FilterReply.DENY
        }
    }

    companion object {
        val LEVEL: Level = Level.DEBUG
    }
}