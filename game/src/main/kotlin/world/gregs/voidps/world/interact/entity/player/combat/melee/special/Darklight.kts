package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isDemon(target: Character?): Boolean = target is NPC && target["race", ""] == "demon"

fun isDarklight(weapon: Item?) = weapon != null && weapon.id == "darklight"

on<CombatSwing>({ !swung() && it.specialAttack && isDarklight(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("darklight_weaken")
    player.setGraphic("darklight_weaken")
    val damage = player.hit(target)
    if (damage > 0) {
        val amount = if (isDemon(target)) 0.10 else 0.05
        target.levels.drain(Skill.Attack, multiplier = amount)
        target.levels.drain(Skill.Strength, multiplier = amount)
        target.levels.drain(Skill.Defence, multiplier = amount)
    }
    delay = 5
}