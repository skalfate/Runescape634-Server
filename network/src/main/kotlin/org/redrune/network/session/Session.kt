package org.redrune.network.session

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import org.redrune.tools.constants.NetworkConstants
import java.net.InetSocketAddress

/**
 * This class represents the network session from the client (player) to the server
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
abstract class Session(
    /**
     * The channel for the connection
     */
    var channel: Channel
) {

    /**
     * When a message is received, this function is invoked
     */
    abstract fun messageReceived(msg: Any)

    /**
     * Sends a message to the channel by [Channel.writeAndFlush]
     */
    fun send(msg: Any) {
        channel.writeAndFlush(msg)
    }

    fun getHost(): String = (channel.remoteAddress() as? InetSocketAddress)?.address?.hostAddress
        ?: NetworkConstants.LOCALHOST

    companion object {
        /**
         * The attribute that contains the key for a session.
         */
        val SESSION_KEY: AttributeKey<Session> = AttributeKey.valueOf<Session>("session.key")


        val ENCRYPTION_KEY = AttributeKey.valueOf<Int>("encryption.key")!!
    }

}