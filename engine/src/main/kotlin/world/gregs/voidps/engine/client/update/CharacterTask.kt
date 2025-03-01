package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.type.Tile

abstract class CharacterTask<C : Character>(
    private val iterator: TaskIterator<C>
) : Runnable {

    abstract val characters: CharacterList<C>

    open fun predicate(character: C): Boolean = true

    abstract fun run(character: C)

    override fun run() {
        iterator.run(this)
    }

    protected fun checkTileFacing(character: Character) {
        if (!character.visuals.moved && character.contains("face_entity")) {
            val any = character.remove<Any>("face_entity")!!
            if (any is Entity) {
                if (any !is Character || character.watching(any)) {
                    character.clearWatch()
                }
                character.face(any)
            } else if (any is Tile) {
                character.clearWatch()
                character.face(any)
            }
        }
    }
}