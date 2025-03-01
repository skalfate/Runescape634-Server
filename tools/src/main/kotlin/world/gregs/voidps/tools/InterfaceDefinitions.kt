package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoderFull

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = InterfaceDecoderFull().loadCache(cache)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            for (comp in def.components ?: continue) {
                comp.clientVarp
            }
        }
    }
}