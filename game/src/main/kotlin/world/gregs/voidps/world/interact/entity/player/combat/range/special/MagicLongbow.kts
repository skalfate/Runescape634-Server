package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.bowHitDelay
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isMagicLong(weapon: Item?) = weapon != null && (weapon.id.startsWith("magic_longbow") || weapon.id.startsWith("magic_composite_bow"))

on<HitChanceModifier>({ type == "range" && special && isMagicLong(weapon) }, Priority.HIGHEST) { _: Player ->
    chance = 1.0
}

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isMagicLong(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 350)) {
        delay = -1
        return@on
    }
    player.setAnimation("bow_shoot")
    player.setGraphic("special_arrow_shoot")
    player.playSound("magic_longbow_special")
    player.shoot(id = "special_arrow", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = bowHitDelay(distance))
}