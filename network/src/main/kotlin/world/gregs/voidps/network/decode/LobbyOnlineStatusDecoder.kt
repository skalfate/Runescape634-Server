package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction

/**
 * Player has changed their online status while in the lobby
 */
class LobbyOnlineStatusDecoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val first = packet.readByte()
        val status = packet.readByte()
        val second = packet.readByte()
    }

}