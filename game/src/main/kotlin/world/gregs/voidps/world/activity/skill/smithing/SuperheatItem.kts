package world.gregs.voidps.world.activity.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.data.definition.data.Smelting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.remove
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes
import world.gregs.voidps.world.interact.entity.sound.playSound

val spellDefinitions: SpellDefinitions by inject()
val itemDefinitions: ItemDefinitions by inject()
val logger = InlineLogger()

on<ItemOnItem>({ fromInterface == "modern_spellbook" && fromComponent == "superheat_item" }) { player: Player ->
    if (!toItem.id.endsWith("_ore")) {
        player.message("You need to cast superheat item on ore.")
        return@on
    }
    var bar = toItem.id.replace("_ore", "_bar")
    if (bar == "iron_bar" && player.inventory.count("coal") >= 2) {
        bar = "steel_bar"
    }
    val smelting: Smelting = itemDefinitions.get(bar)["smelting"]
    if (!player.has(Skill.Smithing, smelting.level, message = true)) {
        return@on
    }
    val runes = mutableListOf<Item>()
    val items = mutableListOf<Item>()
    val spell = fromComponent
    if (!Runes.spellRequirements(player, spell, runes, items)) {
        return@on
    }
    player.inventory.transaction {
        remove(runes)
        remove(smelting.items)
        add(bar)
    }
    when (player.inventory.transaction.error) {
        TransactionError.Invalid -> {}
        TransactionError.None -> {
            for (item in items) {
                // player.equipment.get(item.id).charge -= item.amount
            }
            player.playSound("superheat_all")
            player.setAnimation(spell)
            player.setGraphic(spell)
            val definition = spellDefinitions.get(spell)
            player.experience.add(Skill.Magic, definition.experience)
            player.experience.add(Skill.Smithing, smelting.exp(player, bar))
        }
        else -> logger.warn { "Superheat transaction error $player $bar ${player.inventory.transaction.error}" }
    }
}