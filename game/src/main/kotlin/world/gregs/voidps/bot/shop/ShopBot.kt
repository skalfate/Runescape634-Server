package world.gregs.voidps.bot.shop

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.closeInterface
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.goToNearest
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.world.interact.entity.npc.shop.shopInventory

suspend fun Bot.openShop(id: String): NPC {
    return openShop(get<AreaDefinitions>().getOrNull(id)!!)
}

suspend fun Bot.openNearestShop(id: String): Boolean {
    val reached = goToNearest { it["items", emptyList<String>()].contains(id) }
    openShop()
    return reached
}

suspend fun Bot.openShop(map: AreaDefinition): NPC {
    goToArea(map)
    return openShop()
}

private suspend fun Bot.openShop(): NPC {
    val shop = get<NPCs>().first { it.tile.within(player.tile, Viewport.VIEW_RADIUS) && it.def.options.contains("Trade") }
    player.instructions.emit(InteractNPC(npcIndex = shop.index, option = shop.def.options.indexOfFirst { it == "Trade" } + 1))
    await("shop")
    return shop
}

suspend fun Bot.closeShop() = closeInterface(620, 18)

suspend fun Bot.buy(item: String, amount: Int = 1) {
    val shop = player.shopInventory()
    val index = shop.indexOf(item)
    if (index == -1) {
        throw IllegalArgumentException("Shop doesn't contain item '$item'")
    }
    val slot = index * 6
    var remaining = amount
    while (remaining > 0) {
        val option = when {
            remaining >= 500 -> 5
            remaining >= 50 -> 4
            remaining >= 10 -> 3
            remaining >= 5 -> 2
            else -> 1
        }
        player.instructions.emit(InteractInterface(interfaceId = 620, componentId = 25, itemId = -1, itemSlot = slot, option = option))
        remaining -= when {
            remaining >= 500 -> 500
            remaining >= 50 -> 50
            remaining >= 10 -> 10
            remaining >= 5 -> 5
            else -> 1
        }
    }
    await("tick")
}