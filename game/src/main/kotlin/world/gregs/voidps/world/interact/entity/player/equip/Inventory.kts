package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.swap

val logger = InlineLogger()

on<InterfaceRefreshed>({ id == "inventory" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "inventory", 0 until 28)
    player.interfaceOptions.unlock(id, "inventory", 28 until 56, "Drag")
    player.sendInventory(id)
}

on<InterfaceSwitch> { player: Player ->
    player.queue.clearWeak()
}

on<InterfaceSwitch>({ id == "inventory" && toId == "inventory" }) { player: Player ->
    player.closeInterfaces()
    if (player.mode is CombatMovement) {
        player.mode = EmptyMode
    }
    if (!player.inventory.swap(fromSlot, toSlot)) {
        logger.info { "Failed switching interface items $this" }
    }
}

on<InterfaceOption>({ id == "inventory" && component == "inventory" }) { player: Player ->
    val itemDef = item.def
    val equipOption = when (optionIndex) {
        6 -> itemDef.options.getOrNull(3)
        7 -> itemDef.options.getOrNull(4)
        9 -> "Examine"
        else -> itemDef.options.getOrNull(optionIndex)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $item $optionIndex" }
        return@on
    }
    player.closeInterfaces()
    if (player.mode is CombatMovement) {
        player.mode = EmptyMode
    }
    player.events.emit(
        InventoryOption(
            player,
            id,
            item,
            itemSlot,
            equipOption
        )
    )
}