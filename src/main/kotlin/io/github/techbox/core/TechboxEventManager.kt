package io.github.techbox.core

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.InterfacedEventManager


class TechboxEventManager : InterfacedEventManager() {
    private var lastJdaEvent: Long = 0

    override fun handle(event: GenericEvent) {
        lastJdaEvent = System.currentTimeMillis()
        super.handle(event)
    }

    fun lastJDAEventDiff(): Long {
        return System.currentTimeMillis() - lastJdaEvent
    }

}