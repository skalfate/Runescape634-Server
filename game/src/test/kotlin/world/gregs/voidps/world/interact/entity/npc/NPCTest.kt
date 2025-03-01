package world.gregs.voidps.world.interact.entity.npc

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.world.script.WorldTest

internal class NPCTest : WorldTest() {

    init {
        extraProperties["randomWalk"] = "true"
    }

    @Test
    fun `Man randomly walks around`() {
        val spawn = emptyTile
        val npc = createNPC("chicken", spawn) { npc ->
            npc["area"] = Rectangle(spawn.minus(25, 25), 50, 50)
            npc.softTimers.start("no_clip")
        }
        tickIf { npc.tile == spawn }

        assertNotEquals(spawn, npc.tile)
    }
}