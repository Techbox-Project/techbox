package io.github.techbox.core.shard

import io.github.techbox.core.TechboxEventManager
import net.dv8tion.jda.api.JDA

class Shard(val id: Int) {

    internal lateinit var jda: JDA

    val eventManager = TechboxEventManager()
    val eventListener = ShardListener(this)

}