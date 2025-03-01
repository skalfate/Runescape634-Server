package world.gregs.voidps.network.visual.encode.npc

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_HITS_MASK

class NPCHitsEncoder : VisualEncoder<NPCVisuals>(NPC_HITS_MASK) {

    override fun encode(writer: Writer, visuals: NPCVisuals, index: Int) {
        val (damage, player, other) = visuals.hits
        writer.apply {
            writeByteSubtract(damage.size)
            damage.forEach { hit ->
                hit.write(writer, index, player, add = false)
            }
        }
    }

    override fun encode(writer: Writer, visuals: NPCVisuals) {
        throw RuntimeException("Shouldn't be reachable")
    }

}