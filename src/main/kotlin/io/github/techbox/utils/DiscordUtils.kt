package io.github.techbox.utils

import io.github.techbox.TechboxLauncher
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User


fun extractUserFromString(
    input: String,
    usersInContext: List<User>? = null,
    guild: Guild? = null,
    extractUserViaMention: Boolean = true,
    extractUserViaNameAndDiscriminator: Boolean = true,
    extractUserViaEffectiveName: Boolean = true,
    extractUserViaUsername: Boolean = true,
    extractUserViaUserIdRetrieval: Boolean = true
): User? {
    if (input.isEmpty())
        return null

    if (usersInContext != null && extractUserViaMention) {
        for (user in usersInContext) {
            if (user.asMention == input.replace("!", "")) {
                return user
            }
        }
    }

    if (guild != null) {
        if (extractUserViaNameAndDiscriminator) {
            val split = input.split("#")
            if (split.size == 2) {
                val discriminator = split.last()
                val name = split.dropLast(1).joinToString(" ")
                try {
                    val matchedMember = guild.getMemberByTag(name, discriminator)
                    if (matchedMember != null)
                        return matchedMember.user
                } catch (e: IllegalArgumentException) {} // We don't really care if it is in a invalid format
            }
        }

        if (extractUserViaEffectiveName) {
            val matchedMembers = guild.getMembersByEffectiveName(input, true)
            val matchedMember = matchedMembers.firstOrNull()
            if (matchedMember != null)
                return matchedMember.user
        }

        if (extractUserViaUsername) {
            val matchedMembers = guild.getMembersByName(input, true)
            val matchedMember = matchedMembers.firstOrNull()
            if (matchedMember != null)
                return matchedMember.user
        }
    }

    try {
        if (extractUserViaUserIdRetrieval) {
            val user = TechboxLauncher.core.shardManager.retrieveUserById(input).complete()

            if (user != null)
                return user
        }
    } catch (e: Exception) {
    }

    return null
}