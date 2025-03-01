package world.gregs.voidps.engine.entity.character.player.skill.exp

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

data class BlockedExperience(
    val skill: Skill,
    val experience: Double
) : Event
