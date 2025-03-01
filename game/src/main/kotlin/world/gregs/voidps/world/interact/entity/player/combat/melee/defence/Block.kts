package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatAttack

on<CombatAttack>({ !blocked && target is Player }, Priority.LOWEST) { _: Character ->
    target.setAnimation("block")
    blocked = true
}

on<CombatAttack>({ !blocked && target is NPC }, Priority.LOWEST) { _: Character ->
    val npc = target as NPC
    val race: String? = npc.def.getOrNull("race")
    val animation = if (race != null) "${race}_hit" else npc.def.getOrNull("hit_anim") ?: return@on
    target.setAnimation(animation)
    blocked = true
}