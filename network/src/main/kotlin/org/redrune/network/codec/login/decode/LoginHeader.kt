package org.redrune.network.codec.login.decode

import com.github.michaelbull.logging.InlineLogger
import org.redrune.cache.secure.RSA
import org.redrune.cache.secure.Xtea
import org.redrune.core.network.model.packet.access.PacketReader
import org.redrune.utility.constants.LoginResponseCodes
import org.redrune.utility.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object LoginHeader {

    private val logger = InlineLogger()

    /**
     * Decodes login message
     * @param reader Packet to decode
     * @param extra Whether to read extra byte
     * @return Triple(password, server seed, client seed)
     */
    fun decode(reader: PacketReader, extra: Boolean = false): Triple<LoginResponseCodes, String?, IntArray?> {
        val version = reader.readInt()
        if (version != NetworkConstants.CLIENT_MAJOR_BUILD) {
            return Triple(LoginResponseCodes.GAME_UPDATED, null, null)
        }

        if (extra) {
            reader.readUnsignedByte()
        }

        val rsaBlockSize = reader.readUnsignedShort()//RSA block size
        if (rsaBlockSize > reader.readableBytes()) {
            logger.warn { "Received bad rsa block size [size=$rsaBlockSize, readable=${reader.readableBytes()}" }
            return Triple(LoginResponseCodes.BAD_SESSION_ID, null, null)
        }
        val data = ByteArray(rsaBlockSize)
        reader.readBytes(data)
        val rsa = RSA.crypt(data, NetworkConstants.LOGIN_RSA_MODULUS, NetworkConstants.LOGIN_RSA_PRIVATE)
        val rsaBuffer = PacketReader(rsa)
        val sessionId = rsaBuffer.readUnsignedByte()
        if (sessionId != 10) {//rsa block start check
            logger.warn { "Bad session id received ($sessionId)" }
            return Triple(LoginResponseCodes.BAD_SESSION_ID, null, null)
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsaBuffer.readInt()
        }
        println("isaacKeys=${isaacKeys.contentToString()}")

        val passBlock = rsaBuffer.readLong()
        if (passBlock != 0L) {//password should start here (marked by 0L)
            logger.info { "Rsa start marked by 0L was not true ($passBlock)" }
            return Triple(LoginResponseCodes.BAD_SESSION_ID, null, null)
        }

        val password: String = rsaBuffer.readString()
        val serverSeed = rsaBuffer.readLong()
        val clientSeed = rsaBuffer.readLong()
        Xtea.decipher(reader.getBuffer(), isaacKeys)
        return Triple(LoginResponseCodes.SUCCESSFUL, password, isaacKeys)
    }

}