package world.gregs.voidps.engine.entity.character.player.skill.exp

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

data class GrantExp(
    val skill: Skill,
    val from: Double,
    val to: Double
) : Event
