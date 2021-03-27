package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Players

class InstructionTask(
    private val players: Players,
    private val handler: InstructionHandler
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        players.forEach { player ->
            val instructions = player.instructions
            for (instruction in instructions.replayCache) {
                logger.debug { "${player.name} ${player.tile} - $instruction" }
                handler.handle(player, instruction)
            }
            instructions.resetReplayCache()
        }
    }
}