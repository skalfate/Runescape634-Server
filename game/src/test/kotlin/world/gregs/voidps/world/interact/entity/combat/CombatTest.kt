package world.gregs.voidps.world.interact.entity.combat

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.instruct.InteractPlayer
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.script.*

internal class CombatTest : WorldTest() {

    @Test
    fun `Kill rat with magic`() {
        val player = createPlayer("player", emptyTile)
        val npc = createNPC("rat", emptyTile.addY(4))
        player.equipment.set(EquipSlot.Weapon.index, "staff_of_air")
        player.experience.set(Skill.Magic, experience)
        player.inventory.add("mind_rune", 100)

        player.interfaceOption("modern_spellbook", "wind_strike", option = "Autocast")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(6) // npc death

        assertEquals(emptyTile, player.tile)
        assertTrue(player.experience.get(Skill.Magic) > experience)
        assertTrue(floorItems[tile].any { it.id == "bones" })
        assertTrue(player.inventory.count("mind_rune") < 100)
    }

    @Test
    fun `Kill rat with melee`() {
        val player = createPlayer("player", emptyTile)
        val npc = createNPC("rat", emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, experience)
        player.experience.set(Skill.Strength, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.interfaceOption("combat_styles", "style3")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(7) // npc death

        assertNotEquals(emptyTile, player.tile)
        assertTrue(player.experience.get(Skill.Attack) > experience)
        assertTrue(player.experience.get(Skill.Strength) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        assertTrue(floorItems[tile].any { it.id == "bones" })
    }

    @Test
    fun `Kill rat with range`() {
        val player = createPlayer("player", emptyTile)
        val npc = createNPC("rat", emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Ranged, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Ranged, 25)

        player.interfaceOption("combat_styles", "style3")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(6) // npc death

        val drops = floorItems[tile]
        assertEquals(emptyTile, player.tile)
        assertTrue(player.equipment[EquipSlot.Ammo.index].amount < 100)
        assertTrue(drops.any { it.id == "bones" })
        assertTrue(floorItems[emptyTile.addY(4)].any { it.id == "rune_arrow" })
        assertTrue(player.experience.get(Skill.Ranged) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        assertTrue(player.inventory.count("rune_arrow") < 100)
    }

    @Test
    fun `Dragon dagger special attack`() {
        var hits = 0
        on<NPC, CombatHit> {
            hits++
        }
        val player = createPlayer("player",emptyTile)
        player.experience.set(Skill.Attack, experience)
        player.levels.set(Skill.Attack, 99)
        val npc = createNPC("rat", emptyTile.addY(1))

        player.inventory.add("dragon_dagger")
        player.equipItem("dragon_dagger")

        player.interfaceOption("combat_styles", "special_attack_bar", "Use")
        player.npcOption(npc, "Attack")
        tick()
        tick()

        assertEquals(2, hits)
    }

    @Test
    fun `Don't take damage with protection prayers`() {
        var shouldHaveDamaged = false
        on<NPC, HitDamageModifier>({ damage > 0 }, Priority.HIGHEST) {
            shouldHaveDamaged = true
        }
        val player = createPlayer("player", emptyTile)
        player.experience.set(Skill.Constitution, experience)
        player.levels.set(Skill.Constitution, 990)
        val npc = createNPC("rat", emptyTile.addY(1))
        npc.levels.link(npc.events, object : Levels.Level {
            override fun getMaxLevel(skill: Skill): Int {
                return if (skill == Skill.Constitution) 10000 else 99
            }
        })
        npc.levels.clear()

        player.interfaceOption("prayer_list", "regular_prayers", slot = 19, optionIndex = 0)
        player.npcOption(npc, "Attack")
        tickIf { !shouldHaveDamaged }

        assertEquals(990, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Ranged attacks will run within distance and stop`() {
        val player = createPlayer("player", Tile(3228, 3415))
        val npc = createNPC("rat", Tile(3228, 3407))

        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Ranged, experience)
        player.levels.boost(Skill.Ranged, 25)

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        tick(5) // npc death

        assertEquals(Tile(3228, 3414), player.tile)
    }

    @Test
    fun `Kill player with melee`() {
        val player = createPlayer("player", emptyTile)
        val target = createPlayer("target", emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, experience)
        player.experience.set(Skill.Strength, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)
        player["in_wilderness"] = true
        target["in_wilderness"] = true
        target.appearance.combatLevel = 90
        target.inventory.add("dragon_longsword", 1)
        target.inventory.add("magic_shortbow", 1)
        target.inventory.add("rune_arrow", 10)
        target.inventory.add("excalibur", 1)

        player.interfaceOption("combat_styles", "style3")
        player.playerOption(target, "Attack")
        tickIf { target.levels.get(Skill.Constitution) > 0 }
        tick(6) // player death

        assertNotEquals(emptyTile, player.tile)
        assertTrue(player.experience.get(Skill.Attack) > experience)
        assertTrue(player.experience.get(Skill.Strength) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        val items = floorItems[emptyTile.addY(4)]
        assertTrue(items.any { it.id == "coins" })
        assertTrue(items.any { it.id == "rune_arrow" && it.amount > 1 })
        assertTrue(items.any { it.id == "bones" })
    }

    @Test
    fun `Can't attack players`() {
        val player = createPlayer("player", emptyTile)
        val target = createPlayer("target", emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, experience)
        player.experience.set(Skill.Strength, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.interfaceOption("combat_styles", "style3")
        runTest {
            player.instructions.emit(InteractPlayer(target.index, 1))
        }
        tick(2)

        assertEquals(emptyTile, player.tile)
        assertEquals(experience, player.experience.get(Skill.Attack))
        assertEquals(experience, player.experience.get(Skill.Strength))
        assertEquals(experience, player.experience.get(Skill.Defence))
    }

    companion object {
        private const val experience = 14000000.0
    }
}