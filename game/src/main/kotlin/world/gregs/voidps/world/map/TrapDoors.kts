package world.gregs.voidps.world.map

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

on<ObjectOption>({ operate && target.id.startsWith("trapdoor_") && target.id.endsWith("_closed")}) { player: Player ->
    arriveDelay()
    player.setAnimation("open_chest")
    target.replace(target.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
}