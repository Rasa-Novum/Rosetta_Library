package net.rasanovum.rosetta.gradle

import com.google.gson.*

/** Shared format metadata for compiled data packs and resource-only library jars. */
object PackMetadata {
    fun formatFields(version: String, loader: String): String =
        forTarget(version, loader, "resources").entrySet().joinToString(", ") { (key, value) -> "\"$key\": $value" }

    internal fun forTarget(version: String, loader: String, kind: String): JsonObject {
        val formats = when (version) {
            "1.19.2" -> 10 to 9
            "1.20.1" -> 15 to 15
            "1.21.1" -> 48 to 34
            "26.1" -> 101 to 84
            "26.2" -> 107 to 88
            "26.3" -> 121 to 97
            else -> error("Unsupported Minecraft version '$version'; add a verified format adapter first")
        }
        val pack = JsonObject()
        val dataMinor = if (version == "26.1" || version == "26.2") 1 else 0
        val resourceMinor = if (version == "26.3") 1 else 0
        if (version.startsWith("26.")) {
            fun format(major: Int, minor: Int): JsonElement = JsonArray().also { it.add(major); it.add(minor) }
            pack.add("min_format", if (kind == "data") format(formats.first, dataMinor) else format(formats.second, resourceMinor))
            pack.add("max_format", if (kind == "resources") format(formats.second, resourceMinor) else format(formats.first, dataMinor))
        } else {
            pack.addProperty("pack_format", if (kind == "resources") formats.second else formats.first)
            if (kind == "combined" && formats.first != formats.second && version != "1.19.2") {
                pack.add("supported_formats", JsonObject().also {
                    it.addProperty("min_inclusive", formats.second)
                    it.addProperty("max_inclusive", formats.first)
                })
            }
        }
        if (version == "1.19.2" && loader == "forge") {
            pack.addProperty("forge:resource_pack_format", formats.second)
            pack.addProperty("forge:data_pack_format", formats.first)
        }
        return pack
    }
}
