package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_FORCE_MOVEMENT_MASK

class NPCForceMovementEncoder : VisualEncoder<NPCVisuals>(NPC_FORCE_MOVEMENT_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        val (tile1X, tile1Y, delay1, tile2X, tile2Y, delay2, direction) = visuals.forceMovement
        writer.apply {
            writeByteSubtract(tile1X)
            writeByteSubtract(tile1Y)
            writeByte(tile2X)
            writeByteSubtract(tile2Y)
            writeShortLittle(delay1)
            writeShortAdd(delay2)
            writeByteSubtract(direction / 2)
        }
    }

}