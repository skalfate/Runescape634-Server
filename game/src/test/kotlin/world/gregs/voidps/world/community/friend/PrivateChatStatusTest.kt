package world.gregs.voidps.world.community.friend

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.network.encode.sendFriendsList
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption

internal class PrivateChatStatusTest : WorldTest() {

    private lateinit var player: Player
    private lateinit var befriend: Player
    private lateinit var friend: Player
    private lateinit var stranger: Player
    private lateinit var admin: Player

    @BeforeEach
    fun start() {
        mockkStatic("world.gregs.voidps.network.encode.FriendsEncoderKt")
        runBlocking(Dispatchers.Default) {
            player = createClient("player")
            friend = createClient("friend")
            befriend = createClient("befriend")
            stranger = createClient("stranger")
            admin = createClient("admin")
        }
        admin["rights"] = "admin"
        admin.friends["player"] = Rank.Friend
        befriend.friends["player"] = Rank.Friend
        friend.friends["player"] = Rank.Friend
        player.friends["friend"] = Rank.Friend
        player.friends["offline"] = Rank.Friend
    }

    @Test
    fun `Send friends list on login with status off`() {
        player["private_status"] = "off"

        runBlocking(Dispatchers.Default) {
            player.events.emit(Registered)
        }

        verify {
            player.client?.sendFriendsList(listOf(Friend("friend", "", online = true)))
            admin.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        verify(exactly = 0) {
            friend.client?.sendFriendsList(any())
            befriend.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Send friends list on login with status friends`() {
        player["private_status"] = "friends"

        runBlocking(Dispatchers.Default) {
            player.events.emit(Registered)
        }

        verify {
            player.client?.sendFriendsList(listOf(Friend("friend", "", online = true)))
            friend.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
            admin.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        verify(exactly = 0) {
            befriend.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Send friends list on login with status on`() {
        player["private_status"] = "on"

        runBlocking(Dispatchers.Default) {
            player.events.emit(Registered)
        }

        verify {
            player.client?.sendFriendsList(listOf(Friend("friend", "", online = true)))
            friend.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
            admin.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
            befriend.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        verify(exactly = 0) {
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Change status from off to on updates only befriends`() {
        player["private_status"] = "off"

        player.interfaceOption("filter_buttons", "private", "On")
        tick()

        verify {
            befriend.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        verify(exactly = 0) {
            stranger.client?.sendFriendsList(any())
        }
    }

    private fun createClient(name: String): Player {
        val player = createPlayer(name)
        val client: Client = mockk(relaxed = true)
        player.client = client
        return player
    }

    @Test
    fun `Change status from off to friends`() {
        player["private_status"] = "off"

        player.interfaceOption("filter_buttons", "private", "Friends")
        tick()

        verify {
            friend.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        verify(exactly = 0) {
            admin.client?.sendFriendsList(any())
            befriend.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Change status from friends to on`() {
        player["private_status"] = "friends"

        player.interfaceOption("filter_buttons", "private", "On")
        tick()

        verify {
            befriend.client?.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        verify(exactly = 0) {
            admin.client?.sendFriendsList(any())
            friend.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Change status from friends to off`() {
        player["private_status"] = "friends"

        player.interfaceOption("filter_buttons", "private", "Off")
        tick()

        verify {
            friend.client?.sendFriendsList(listOf(Friend("player", "", online = false)))
        }
        verify(exactly = 0) {
            admin.client?.sendFriendsList(any())
            befriend.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Change status from on to off`() {
        player["private_status"] = "on"

        player.interfaceOption("filter_buttons", "private", "Off")
        tick()

        verify {
            befriend.client?.sendFriendsList(listOf(Friend("player", "", online = false)))
            friend.client?.sendFriendsList(listOf(Friend("player", "", online = false)))
        }
        verify(exactly = 0) {
            admin.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

    @Test
    fun `Change status from on to friends`() {
        player["private_status"] = "on"

        player.interfaceOption("filter_buttons", "private", "Friends")
        tick()

        verify {
            befriend.client?.sendFriendsList(listOf(Friend("player", "", online = false)))
        }
        verify(exactly = 0) {
            friend.client?.sendFriendsList(any())
            admin.client?.sendFriendsList(any())
            stranger.client?.sendFriendsList(any())
        }
    }

}