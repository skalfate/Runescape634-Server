package world.gregs.voidps.world.activity.skill.fishing

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Area
import kotlin.random.Random

val minRespawnTick = 280
val maxRespawnTick = 530

on<Registered>({ it.id.startsWith("fishing_spot") }) { npc: NPC ->
    val area: Area = npc.getOrNull("area") ?: return@on
    move(npc, area)
}

fun move(npc: NPC, area: Area) {
    npc.softQueue("spot_move", Random.nextInt(minRespawnTick, maxRespawnTick)) {
        area.random(npc)?.let { tile ->
            npc.tele(tile)
        }
        move(npc, area)
    }
}