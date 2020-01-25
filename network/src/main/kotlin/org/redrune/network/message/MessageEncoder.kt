package org.redrune.network.message

import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketBuilder
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass


/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 3:47 p.m.
 */
abstract class MessageEncoder<T : Message> {

    fun getGenericTypeClass(): KClass<T> {
        return try {
            val className =
                (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0].typeName
            val clazz = Class.forName(className).kotlin
            clazz as KClass<T>
        } catch (e: Exception) {
            throw IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ", e)
        }
    }

    /**
     * Encodes a message of type [T] into a [Packet]
     * @return Packet
     */
    abstract fun encode(buf: PacketBuilder, msg: T)

}