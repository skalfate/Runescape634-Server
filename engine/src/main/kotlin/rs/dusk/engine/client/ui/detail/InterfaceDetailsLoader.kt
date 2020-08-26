package rs.dusk.engine.client.ui.detail

import org.koin.dsl.module
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_NAME
import rs.dusk.engine.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_RESIZE_NAME

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = GAME_FRAME_RESIZE_NAME

class InterfaceDetailsLoader(private val loader: FileLoader) : TimedLoader<InterfaceDetails>("interfaces") {

    fun loadFile(path: String): Map<String, Map<String, Any>> = loader.load(path)

    override fun load(args: Array<out Any?>): InterfaceDetails {
        return loadAll(args[0] as String, args[1] as String)
    }

    fun loadAll(detailPath: String, typesPath: String): InterfaceDetails {
        val detailData = loadFile(detailPath)
        val typeData = loadFile(typesPath)
        val names = loadNames(detailData)
        val types = loadTypes(typeData, names.map { it.value to it.key }.toMap())
        val details = loadDetails(detailData, types)
        count = names.size
        return InterfaceDetails(details, names)
    }

    fun loadNames(data: Map<String, Map<String, Any>>) = data.map { (name, values) -> values.getId() to name }.toMap()

    fun loadTypes(data: Map<String, Map<String, Any>>, names: Map<String, Int>) = data.map { (name, values) ->
        val index = values.readInt("index")
        val fixedIndex = index ?: values.readInt("fixedIndex")
        val resizeIndex = index ?: values.readInt("resizeIndex")

        val parent = values.readString("parent")
        val fixedParentName = parent ?: values.readString("fixedParent") ?: DEFAULT_FIXED_PARENT
        val resizeParentName = parent ?: values.readString("resizeParent") ?: DEFAULT_RESIZE_PARENT
        val fixedParent = names.getParentId(fixedParentName)
        val resizeParent = names.getParentId(resizeParentName)

        name to InterfaceData(
            fixedParent,
            resizeParent,
            fixedIndex,
            resizeIndex
        )
    }.toMap()

    fun loadDetails(
        data: Map<String, Map<String, Any>>,
        types: Map<String, InterfaceData>
    ) = data.map { (name, values) ->
        val id = values.getId()
        val typeName = values.readString("type") ?: DEFAULT_TYPE
        val type = types[typeName]
        checkNotNull(type) { "Missing interface type $typeName" }
        val components = values.getComponents()
        name to InterfaceDetail(id, name, typeName, type, components)
    }.toMap()

    private fun Map<String, Any>.getComponents(): Map<String, InterfaceComponentDetail> {
        val value = this["components"] as? Map<*, *>
        val components = value?.map {
            val name = it.key as String
            val id = it.value as Int
            name to InterfaceComponentDetail(id, name)
        }?.toMap()
        return components ?: emptyMap()
    }

    private fun Map<String, Any>.getId(): Int {
        val id = readInt("id")
        checkNotNull(id) { "Missing interface id $id" }
        return id
    }

    private fun Map<String, Int>.getParentId(name: String): Int {
        val id = this[name]
        checkNotNull(id) { "Missing parent $name" }
        return id
    }

    private fun Map<String, Any>.readInt(name: String) = this[name] as? Int
    private fun Map<String, Any>.readString(name: String) = this[name] as? String

}

val interfaceModule = module {
    single(createdAtStart = true) {
        InterfaceDetailsLoader(get())
            .run(getProperty("interfacesPath"), getProperty("interfaceTypesPath"))
    }
}