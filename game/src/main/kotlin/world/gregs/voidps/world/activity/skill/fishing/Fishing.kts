package world.gregs.voidps.world.activity.skill.fishing

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Catch
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.hasItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.pause
import kotlin.random.Random

val logger = InlineLogger()
val itemDefinitions: ItemDefinitions by inject()

on<Moved>({ it.contains("fishers") && it.def.has("fishing") }) { npc: NPC ->
    val fishers: Set<Player> = npc.remove("fishers") ?: return@on
    for (fisher in fishers) {
        fisher.queue.clearWeak()
    }
}

on<NPCOption>({ operate && def.has("fishing") }) { player: Player ->
    arriveDelay()
    target.getOrPut("fishers") { mutableSetOf<Player>() }.add(player)
    player.softTimers.start("fishing")
    player.closeDialogue()
    var first = true
    fishing@ while (true) {
        if (player.inventory.isFull()) {
            player.message("Your inventory is too full to hold any more fish.")
            break
        }

        val data = target.spot[option] ?: return@on

        if (!player.has(Skill.Fishing, data.minimumLevel, true)) {
            break
        }

        val tackle = data.tackle.firstOrNull { tackle -> player.hasItem(tackle) }
        if (tackle == null) {
            player.message("You need a ${data.tackle.first().toTitleCase()} to catch these fish.")
            break@fishing
        }

        val bait = data.bait.keys.firstOrNull { bait -> bait == "none" || player.hasItem(bait) }
        val catches = data.bait[bait]
        if (bait == null || catches == null) {
            player.message("You don't have any ${data.bait.keys.first().toTitleCase().plural(2)}.")
            break
        }
        if (first) {
            player.message(itemDefinitions.get(tackle)["cast", ""], ChatType.Filter)
            first = false
        }

        val remaining = player.remaining("skill_delay")
        if (remaining < 0) {
            player.face(target)
            val rod = tackle == "fishing_rod" || tackle == "fly_fishing_rod" || tackle == "barbarian_rod"
            player.setAnimation("fish_${if (rod) if (first) "fishing_rod" else "rod" else tackle}")
            pause(5)
        } else if (remaining > 0) {
            return@on
        }
        for (item in catches) {
            val catch = itemDefinitions.get(item)["fishing", Catch.EMPTY]
            val level = player.levels.get(Skill.Fishing)
            if (level >= catch.level && success(level, catch.chance)) {
                if (bait != "none" && !player.inventory.remove(bait)) {
                    break@fishing
                }
                player.experience.add(Skill.Fishing, catch.xp)
                addCatch(player, item)
                break
            }
        }
        player.stop("skill_delay")
    }
    target.get<MutableSet<Player>>("fishers").remove(player)
    player.softTimers.stop("fishing")
}

fun addCatch(player: Player, catch: String) {
    var fish = catch
    var message = "You catch some ${fish.toLowerSpaceCase()}."
    if (bigCatch(fish)) {
        fish = fish.replace("raw_", "big_")
        message = "You catch an enormous ${catch.toLowerSpaceCase()}!"
    }
    player.inventory.add(fish)
    when (player.inventory.transaction.error) {
        TransactionError.None -> player.message(message, ChatType.Filter)
        is TransactionError.Full -> player.inventoryFull()
        else -> logger.warn { "Error adding fish $fish ${player.inventory.transaction.error}" }
    }
}

fun bigCatch(catch: String): Boolean = when {
    World.members -> false
    catch == "raw_bass" && Random.nextInt(1000) == 0 -> true
    catch == "raw_swordfish" && Random.nextInt(2500) == 0 -> true
    catch == "raw_shark" && Random.nextInt(5000) == 0 -> true
    else -> false
}

val NPC.spot: Map<String, Spot>
    get() = def["fishing", emptyMap()]

val Spot.minimumLevel: Int
    get() = bait.keys.minOf { minimumLevel(it) ?: Int.MAX_VALUE }

fun Spot.minimumLevel(bait: String): Int? {
    return this.bait[bait]?.minOf { itemDefinitions.get(it)["fishing", Catch.EMPTY].level }
}