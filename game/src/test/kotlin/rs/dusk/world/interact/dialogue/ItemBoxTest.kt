package rs.dusk.world.interact.dialogue

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.dialogue.DialogueIO
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

internal class ItemBoxTest {

    lateinit var interfaces: Interfaces
    lateinit var manager: Dialogues
    lateinit var io: DialogueIO
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        mockkStatic("rs.dusk.engine.client.ui.InterfacesKt")
        mockkStatic("rs.dusk.engine.client.SessionsKt")
        player = mockk(relaxed = true)
        interfaces = mockk(relaxed = true)
        io = mockk(relaxed = true)
        manager = spyk(Dialogues(io, player))
        every { player.open(any()) } returns true
        every { player.send(any<Message>()) } just Runs
        every { player.interfaces } returns interfaces
    }

    @Test
    fun `Send item box`() = runBlocking {
        manager.start {
            itemBox("question", 9009, 650, 10)
        }
        withContext(Contexts.Game) {
            assertEquals("item", manager.currentType())
            verify {
                player.open("obj_box")
                player.send(ScriptMessage(3449, 9009, 650))
                interfaces.sendSprite("obj_box", "sprite", 10)
                interfaces.sendText("obj_box", "line1", "question")
            }
        }
    }

    @Test
    fun `Item box not sent if interface not opened`() = runBlocking {
        coEvery { manager.await<Unit>(any()) } just Runs
        every { player.open("obj_box") } returns false
        manager.start {
            itemBox("question", 9009, 650, 10)
        }

        withContext(Contexts.Game) {
            coVerify(exactly = 0) {
                manager.await<Unit>("item")
                player.send(ScriptMessage(3449, 9009, 650))
            }
        }
    }
}