package world.gregs.voidps.world.community.assist

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.MoveType
import world.gregs.voidps.type.Tile

/**
 * Player leaving assistance range
 */

val maximumTileDistance = 20

on<Moved>({ it.contains("assistant") }) { player: Player ->
    when (player.movementType) {
        MoveType.Teleport -> player["assist_point"] = player.tile
        else -> {
            val point: Tile? = player.getOrNull("assist_point")
            if (point == null || !player.tile.within(point, maximumTileDistance)) {
                val assistant: Player? = player.getOrNull("assistant")
                assistant?.closeMenu()
            }
        }
    }
}