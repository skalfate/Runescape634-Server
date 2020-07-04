import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.gfx.AreaGraphic
import rs.dusk.engine.model.entity.gfx.Graphics
import rs.dusk.engine.model.entity.index.update.visual.Graphic
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.GraphicAreaMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.gfx.SpawnGraphic

val graphics: Graphics by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()

SpawnGraphic then {
    val ag = AreaGraphic(tile, Graphic(id, delay, height, rotation, forceRefresh), owner)
    graphics.add(ag)
    batcher.update(tile.chunkPlane, ag.toMessage())
    decay(ag)
    bus.emit(Registered(ag))
}

/**
 * Reduces timers to keep approx in sync for players starting to view mid-way through
 */
fun decay(ag: AreaGraphic) {
    scheduler.add {
        try {
            repeat(ag.graphic.delay / 30) {
                delay(1)
                ag.graphic.delay -= 30
            }
            ag.graphic.delay = 0
            delay(1)// TODO delay by definition duration
        } finally {
            graphics.remove(ag)
            bus.emit(Unregistered(ag))
        }
    }
}

fun AreaGraphic.toMessage() = GraphicAreaMessage(tile.offset(), graphic.id, graphic.height, graphic.delay, graphic.rotation)

batcher.addInitial { player, chunkPlane, messages ->
    graphics[chunkPlane]?.forEach {
        if (it.visible(player)) {
            messages += it.toMessage()
        }
    }
}