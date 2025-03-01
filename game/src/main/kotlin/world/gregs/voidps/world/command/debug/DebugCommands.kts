package world.gregs.voidps.world.command.debug

import net.pearx.kasechange.toSentenceCase
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.bot.path.Dijkstra
import world.gregs.voidps.bot.path.EdgeTraversal
import world.gregs.voidps.bot.path.NodeTargetStrategy
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.CollisionFlags
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.timer.TimerQueue
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.network.encode.clearCamera
import world.gregs.voidps.network.encode.npcDialogueHead
import world.gregs.voidps.network.encode.openInterface
import world.gregs.voidps.network.encode.playerDialogueHead
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.world.interact.dialogue.sendLines
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

val collisions: Collisions by inject()
val objects: GameObjects by inject()

on<Command>({ prefix == "test" }) { player: Player ->
        player.client?.openInterface(
            permanent = true,
            parent = 548,
            component = content.toInt(),
            id = 941
        )
}

on<Command>({ prefix == "reset_cam" }) { player: Player ->
    player.client?.clearCamera()
}

on<Command>({ prefix == "move_to" }) { player: Player ->
    val test = content.split(" ")
    val viewport = player.viewport!!
    val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
    val local = Tile(test[0].toInt(), test[1].toInt()).minus(result.tile)
    println(local)
    player.moveCamera(local, test[2].toInt(), test[3].toInt(), test[4].toInt())
}

on<Command>({ prefix == "look_at" }) { player: Player ->
    val test = content.split(" ")
    val viewport = player.viewport!!
    val result = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
    val local = Tile(test[0].toInt(), test[1].toInt()).minus(result.tile)
    println(local)
    player.turnCamera(local, test[2].toInt(), test[3].toInt(), test[4].toInt())
}

on<Command>({ prefix == "shake" }) { player: Player ->
    val test = content.split(" ")
    player.shakeCamera(test[0].toInt(), test[1].toInt(), test[2].toInt(), test[3].toInt(), test[4].toInt())
}

on<Command>({ prefix == "timers" }) { player: Player ->
    player.message("=== Timers ===", ChatType.Console)
    for (timer in player.timers.queue) {
        player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
    }
    player.message("=== Soft Timers ===", ChatType.Console)
    for (timer in (player.softTimers as TimerQueue).queue) {
        player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
    }
}

on<Command>({ prefix == "variables" }) { player: Player ->
    player.message("=== Variables ===", ChatType.Console)
    for ((variable, value) in (player.variables as PlayerVariables).temp) {
        player.message("$variable: $value", ChatType.Console)
    }
    player.message("=== Persistent Variables ===", ChatType.Console)
    for ((variable, value) in player.variables.data) {
        player.message("$variable: $value", ChatType.Console)
    }
}

on<Command>({ prefix == "pf_bench" }) { player: Player ->
    val pf = PathFinder(flags = collisions, useRouteBlockerFlags = true)
    val start = Tile(3270, 3331, 0)
    val timeShort = measureTimeMillis {
        repeat(100_000) {
            pf.findPath(0, start.x, start.y, 3280, 3321)
        }
    }

    val timeMedium = measureTimeMillis {
        repeat(10_000) {
            pf.findPath(0, start.x, start.y, 3287, 3306)
        }
    }

    val timeLong = measureTimeMillis {
        repeat(1_000) {
            pf.findPath(0, start.x, start.y, 3270, 3268)
        }
    }

    val timeInvalid = measureTimeMillis {
        repeat(1_000) {
            pf.findPath(0, start.x, start.y, 3271, 3235)
        }
    }

    println("Durations: ")
    println("Short path: ${timeShort / 1000.0}s")
    println("Medium path: ${timeMedium}ms")
    println("Long path: ${timeLong}ms")
    println("Invalid path: ${timeInvalid}ms")
}

on<Command>({ prefix == "rights" }) { player: Player ->
    val right = content.split(" ").last()
    val rights = PlayerRights.valueOf(right.toSentenceCase())
    val username = content.removeSuffix(" $right")
    val target = get<Players>().get(username)
    if (target == null) {
        player.message("Unable to find player '$username'.")
    } else {
        target.rights = rights
        player.message("${player.name} rights set to $rights.")
    }
}

on<Command>({ prefix == "expr" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id != null) {
        val npc = id < 1000
        if (player.open("dialogue_${if (npc) "npc_" else ""}chat1")) {
            if (npc) {
                player.client?.npcDialogueHead(241, 15794178, 2176)
            } else {
                player.client?.playerDialogueHead(64, 4194306)
            }
            player.interfaces.sendAnimation("dialogue_${if (npc) "npc_" else ""}chat1", "head", id)
            player.interfaces.sendText("dialogue_${if (npc) "npc_" else ""}chat1", "title", "title")
            player.interfaces.sendLines("dialogue_${if (npc) "npc_" else ""}chat1", listOf(content))
        }
    } else {
        npc("1902", content, content)
    }
}

on<Command>({ prefix == "showcol" }) { player: Player ->
    val area = player.tile.toCuboid(10)
    val collisions: Collisions = get()
    for (tile in area) {
        if (collisions[tile.x, tile.y, tile.level] != 0) {
            areaGraphic("2000", tile)
        }
    }
}

on<Command>({ prefix == "path" }) { player: Player ->
    player.softTimers.toggle("show_path")
}

on<TimerTick>({ timer == "show_path" }) { player: Player ->
    var tile = player.tile
    for (step in player.steps) {
        tile = tile.add(step)
        areaGraphic("2000", tile)
    }
}

on<Command>({ prefix == "col" }) { player: Player ->
    val collisions: Collisions = get()
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and (CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER) == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and CollisionFlag.BLOCK_NORTH == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and CollisionFlag.WALL_NORTH == 0}")
    println("Can move north? ${collisions[player.tile.x, player.tile.y, player.tile.level] and CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER == 0}")
    println(collisions[player.tile.x, player.tile.y - 1, player.tile.level])
    println(collisions[3281, 3327, 0])
    println(player.tile.minus(y = 1))

    println(CollisionFlag.BLOCK_NORTH or CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
    println(CollisionFlag.BLOCK_NORTH)
    println(CollisionFlag.BLOCK_NORTH_ROUTE_BLOCKER)
    println(CollisionFlags.ROUTE_NORTH.bit)
//
//    val pf = SmartPathFinder(flags = collisions.data, useRouteBlockerFlags = false)
//    println(pf.findPath(3205, 3220, 3205, 3223, 2))
}

operator fun Array<IntArray?>.get(baseX: Int, baseY: Int, localX: Int, localY: Int, z: Int): Int {
    val x = baseX + localX
    val y = baseY + localY
    val zone = this[Zone.tileIndex(x, y, z)] ?: return 0
    return zone[Tile.index(x, y)]
}

on<Command>({ prefix == "walkToBank" }) { player: Player ->
    val east = Tile(3179, 3433).toCuboid(15, 14)
    val west = Tile(3250, 3417).toCuboid(7, 8)
    val dijkstra: Dijkstra = get()
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return if (node is Tile) east.contains(node) || west.contains(node) else false
        }
    }
    println("Path took ${
        measureNanoTime {
            dijkstra.find(player, strategy, EdgeTraversal())
        }
    }ns")
    /*player.action { FIXME
        var first = true
        while (player.waypoints.isNotEmpty()) {
            val next = player.waypoints.poll()
            suspendCoroutine<Unit> { cont ->
                val tile = if (first && !player.tile.within(next.end as Tile, 20)) {
                    next.start
                } else {
                    next.end
                } as Tile
                first = false
                scheduler.add {
                    player.walkTo(tile)
                }
            }
        }
    }*/
}

on<Command>({ prefix == "sendItems" }) { player: Player ->
    val array = IntArray(28 * 2)
    array[0] = 995
    array[28] = 1
    player.sendInventoryItems(90, 28, array, false)
    val ags = IntArray(28 * 2)
    ags[0] = 11694
    ags[28] = 1
    player.sendInventoryItems(90, 28, ags, true)
}

on<Command>({ prefix == "obj" }) { player: Player ->
    if (content.isNotBlank()) {
        val parts = content.split(" ")
        val id = parts.getOrNull(0)
        if (id != null) {
            val rotation = parts.getOrNull(1)?.toIntOrNull() ?: 0
            objects.add(id, player.tile.addY(1), 0, rotation, 10)
            objects.add(id, player.tile.addY(1), 10, rotation, 10)
            objects.add(id, player.tile.addY(1), 22, rotation, 10)
        }
    } else {
        val objs = get<GameObjects>()
        objs[player.tile].forEach {
            println(it.intId)
        }
    }
}

on<Command>({ prefix == "tree" }) { player: Player ->
    val parts = content.split(" ")
    val tree = parts[0]
    val stump = parts[1]
    val type = parts.getOrNull(2)?.toIntOrNull() ?: 10
    objects.add(tree, player.tile, type, 0, 5)
    pause(5)
    objects.add(stump, player.tile, type, 0, 5)
}