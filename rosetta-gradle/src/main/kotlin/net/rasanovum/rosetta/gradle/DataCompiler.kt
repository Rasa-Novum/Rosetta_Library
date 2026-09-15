package net.rasanovum.rosetta.gradle

import com.google.gson.*

/** Converts a deliberately bounded, modern JSON vocabulary into each supported pack format. */
internal class DataCompiler(private val version: String, private val loader: String) {
    private val legacy = version == "1.20.1"
    private val modern = version.startsWith("26.")
    private val formats = when (version) {
        "1.20.1" -> 15 to 15
        "1.21.1" -> 48 to 34
        "26.1" -> 101 to 84
        "26.2" -> 107 to 88
        "26.3" -> 121 to 97
        else -> error("Unsupported Minecraft version '$version'; add a verified format adapter first")
    }
    private val hint = "; use a complete target-specific \$rosetta.native definition for unsupported formats"
    private val idPattern = Regex("[a-z0-9_.-]+:[a-z0-9/._-]+")

    fun pack(input: JsonObject): JsonObject {
        fields(input, setOf("description", "kind"))
        require(input.has("description")) { "pack.json requires description" }
        textComponent(input["description"])
        val kind = input.get("kind")?.asString ?: "combined"
        require(kind in setOf("combined", "data", "resources")) { "Pack kind must be combined, data, or resources" }
        val pack = JsonObject()
        pack.add("description", input["description"].deepCopy())
        val dataMinor = if (version == "26.1" || version == "26.2") 1 else 0
        val resourceMinor = if (version == "26.3") 1 else 0
        if (modern) {
            fun format(major: Int, minor: Int): JsonElement = JsonArray().also { it.add(major); it.add(minor) }
            pack.add("min_format", if (kind == "data") format(formats.first, dataMinor) else format(formats.second, resourceMinor))
            pack.add("max_format", if (kind == "resources") format(formats.second, resourceMinor) else format(formats.first, dataMinor))
        } else {
            pack.addProperty("pack_format", if (kind == "resources") formats.second else formats.first)
            if (kind == "combined" && formats.first != formats.second) {
                pack.add("supported_formats", JsonObject().also {
                    it.addProperty("min_inclusive", formats.second)
                    it.addProperty("max_inclusive", formats.first)
                })
            }
        }
        return JsonObject().also { it.add("pack", pack) }
    }

    fun compile(path: String, input: JsonObject): Pair<String, JsonObject>? {
        require(Regex("data/[a-z0-9_.-]+/(recipe|advancement|tags)/[a-z0-9/._-]+\\.json").matches(path) &&
            path.split('/').none { it == "." || it == ".." }) { "Expected data/<namespace>/{recipe,advancement,tags}/<path>.json" }
        val json = input.deepCopy()
        val metadata = json.remove("\$rosetta")?.asJsonObject
        var native: JsonObject? = null
        if (metadata != null) {
            fields(metadata, setOf("overrides", "native", "exclude"))
            val selectors = listOf(version, "$version-$loader")
            val allowed = setOf("1.20.1", "1.21.1", "26.1", "26.2", "26.3")
                .flatMap { listOf(it, "$it-fabric", "$it-forge", "$it-neoforge") }.toSet()
            metadata["exclude"]?.asJsonArray?.forEach { require(it.asString in allowed) { "Unknown target selector $it" } }
            for (name in listOf("overrides", "native")) metadata[name]?.asJsonObject?.keySet()?.forEach {
                require(it in allowed) { "Unknown target selector '$it'" }
            }
            if (metadata["exclude"]?.asJsonArray?.any { it.asString in selectors } == true) return null
            for (selector in selectors) {
                metadata["overrides"]?.asJsonObject?.get(selector)?.asJsonObject?.let { merge(json, it) }
                metadata["native"]?.asJsonObject?.get(selector)?.asJsonObject?.let { native = it.deepCopy() }
            }
        }
        val result = native ?: json
        require(!result.has("\$rosetta")) { "Nested override metadata is not supported" }
        val segments = path.split('/').toMutableList()
        if (segments[2] == "tags") require(segments.size >= 5) { "Tags require a registry and resource path" }
        if (native == null) when (segments[2]) {
            "recipe" -> recipe(result)
            "advancement" -> advancement(result)
            "tags" -> tag(result)
        }
        if (legacy) {
            segments[2] = when (segments[2]) { "recipe" -> "recipes"; "advancement" -> "advancements"; else -> segments[2] }
            if (segments[2] == "tags") {
                require(segments.size >= 5) { "Tags require a registry and resource path" }
                segments[3] = mapOf("block" to "blocks", "item" to "items", "fluid" to "fluids",
                    "entity_type" to "entity_types", "game_event" to "game_events", "function" to "functions")[segments[3]] ?: segments[3]
            }
        }
        return segments.joinToString("/") to result
    }

    private fun merge(target: JsonObject, patch: JsonObject) {
        patch.entrySet().forEach { (key, value) ->
            if (value.isJsonNull) target.remove(key)
            else if (value.isJsonObject) {
                val child = target[key]?.takeIf { it.isJsonObject }?.asJsonObject ?: JsonObject().also { target.add(key, it) }
                merge(child, value.asJsonObject)
            } else target.add(key, value.deepCopy())
        }
    }

    private fun recipe(json: JsonObject) {
        val type = string(json, "type").removePrefix("minecraft:")
        val cooking = type in setOf("smelting", "blasting", "smoking", "campfire_cooking")
        require(cooking || type in setOf("crafting_shaped", "crafting_shapeless", "stonecutting")) { "Unsupported recipe type '$type'$hint" }
        val common = setOf("type", "group", "category", "result")
        fields(json, common + when (type) {
            "crafting_shaped" -> setOf("pattern", "key", "show_notification")
            "crafting_shapeless" -> setOf("ingredients")
            "stonecutting" -> setOf("ingredient")
            else -> setOf("ingredient", "experience", "cookingtime")
        })
        when (type) {
            "crafting_shaped" -> {
                val rows = json.getAsJsonArray("pattern") ?: error("Missing pattern")
                require(rows.size() in 1..3) { "Pattern must have 1-3 rows" }
                val lines = rows.map { it.asString }
                require(lines.all { it.length in 1..3 && it.length == lines[0].length }) { "Pattern rows must have equal width (1-3)" }
                val keys = json.getAsJsonObject("key") ?: error("Missing key")
                require(keys.keySet().all { it.length == 1 && it != " " }) { "Pattern keys must be one non-space character" }
                val used = lines.joinToString("").filter { it != ' ' }.map { it.toString() }.toSet()
                require(used.isNotEmpty() && used == keys.keySet()) { "Pattern symbols and ingredient keys must match" }
                keys.keySet().toList().forEach { keys.add(it, ingredient(keys[it])) }
            }
            "crafting_shapeless" -> {
                val ingredients = json.getAsJsonArray("ingredients") ?: error("Missing ingredients")
                require(ingredients.size() in 1..9) { "Expected 1-9 ingredients" }
                json.add("ingredients", JsonArray().also { out -> ingredients.forEach { out.add(ingredient(it)) } })
            }
            else -> json.add("ingredient", ingredient(json["ingredient"] ?: error("Missing ingredient")))
        }
        val result = json.getAsJsonObject("result") ?: error("Result must be an item stack object")
        fields(result, setOf("id", "count"))
        id(string(result, "id"))
        val count = result["count"]?.let { integer(it) } ?: 1
        require(count in 1..99) { "Result count must be 1-99" }
        if (legacy) {
            if (cooking || type == "stonecutting") {
                require(!cooking || count == 1) { "Legacy cooking results require count 1$hint" }
                json.addProperty("result", result["id"].asString)
                if (type == "stonecutting") json.addProperty("count", count)
            } else {
                result.add("item", result.remove("id"))
                require(count <= 64) { "Legacy crafting count exceeds 64$hint" }
            }
        }
    }

    private fun ingredient(value: JsonElement): JsonElement {
        if (value.isJsonArray) {
            require(value.asJsonArray.size() > 0) { "Ingredient alternatives cannot be empty" }
            require(value.asJsonArray.all { it.isJsonPrimitive && it.asJsonPrimitive.isString && !it.asString.startsWith('#') }) {
                "Ingredient alternatives must contain only item IDs$hint"
            }
            return JsonArray().also { out -> value.asJsonArray.forEach { out.add(ingredient(it)) } }
        }
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "Canonical ingredients must be item IDs or #tag strings$hint" }
        val text = value.asString
        id(text.removePrefix("#"))
        require(text != "minecraft:air") { "Air is not a valid ingredient" }
        return if (modern) value.deepCopy() else JsonObject().also { it.addProperty(if (text.startsWith('#')) "tag" else "item", text.removePrefix("#")) }
    }

    private fun advancement(json: JsonObject) {
        fields(json, setOf("parent", "display", "criteria", "requirements", "rewards", "sends_telemetry_event"))
        json["parent"]?.let { id(it.asString) }
        val criteria = json.getAsJsonObject("criteria") ?: error("Missing criteria")
        require(criteria.size() > 0) { "Advancement requires at least one criterion" }
        criteria.entrySet().forEach { (_, value) ->
            val criterion = value.asJsonObject
            fields(criterion, setOf("trigger", "conditions"))
            id(string(criterion, "trigger"))
            val trigger = criterion["trigger"].asString
            require(trigger in setOf("minecraft:impossible", "minecraft:tick", "minecraft:inventory_changed")) {
                "Unsupported advancement trigger$hint"
            }
            criterion["conditions"]?.asJsonObject?.let { conditions ->
                fields(conditions, if (trigger == "minecraft:inventory_changed") setOf("items", "slots") else emptySet())
                conditions["items"]?.asJsonArray?.forEach { element ->
                    val predicate = element.asJsonObject
                    fields(predicate, setOf("items", "count"))
                    predicate["count"]?.let { range(it) }
                    predicate["items"]?.let { items ->
                        if (items.isJsonArray) {
                            require(items.asJsonArray.size() > 0) { "Item predicate list cannot be empty" }
                            items.asJsonArray.forEach { id(it.asString) }
                        } else {
                            val value = items.asString
                            id(value.removePrefix("#"))
                            if (legacy) {
                                if (value.startsWith('#')) {
                                    predicate.remove("items")
                                    predicate.addProperty("tag", value.drop(1))
                                } else predicate.add("items", JsonArray().also { it.add(value) })
                            }
                        }
                    }
                }
                conditions["slots"]?.asJsonObject?.let { slots ->
                    fields(slots, setOf("occupied", "full", "empty"))
                    slots.entrySet().forEach { range(it.value) }
                }
            }
        }
        json["requirements"]?.asJsonArray?.let { groups ->
            val referenced = mutableSetOf<String>()
            require(groups.size() > 0) { "Requirements cannot be empty" }
            groups.forEach { group ->
                require(group.asJsonArray.size() > 0) { "Requirement groups cannot be empty" }
                group.asJsonArray.forEach { require(it.asString in criteria.keySet()) { "Unknown criterion $it" }; referenced.add(it.asString) }
            }
            require(referenced == criteria.keySet()) { "Requirements must reference every criterion" }
        }
        json["display"]?.asJsonObject?.let { display ->
            fields(display, setOf("icon", "title", "description", "background", "frame", "show_toast", "announce_to_chat", "hidden"))
            require(display.has("title") && display.has("description")) { "Display requires title and description" }
            textComponent(display["title"])
            textComponent(display["description"])
            val icon = display.getAsJsonObject("icon") ?: error("Display requires icon")
            fields(icon, setOf("id"))
            id(string(icon, "id"))
            if (legacy) icon.add("item", icon.remove("id"))
            display["background"]?.let {
                val background = it.asString
                id(background)
                require(!background.substringAfter(':').startsWith("textures/") && !background.endsWith(".png")) { "Use the modern background ID without textures/ or .png" }
                if (!modern) display.addProperty("background", background.substringBefore(':') + ":textures/" + background.substringAfter(':') + ".png")
            }
        }
        json["rewards"]?.asJsonObject?.let { rewards ->
            fields(rewards, setOf("experience", "function", "recipes", "loot"))
            rewards["function"]?.let { id(it.asString) }
            for (key in listOf("recipes", "loot")) rewards[key]?.asJsonArray?.forEach { id(it.asString) }
        }
    }

    private fun tag(json: JsonObject) {
        fields(json, setOf("replace", "values"))
        json["replace"]?.let { require(it.isJsonPrimitive && it.asJsonPrimitive.isBoolean) { "replace must be boolean" } }
        val values = json.getAsJsonArray("values") ?: error("Tag requires values")
        values.forEach { value ->
            if (value.isJsonObject) {
                fields(value.asJsonObject, setOf("id", "required"))
                id(string(value.asJsonObject, "id").removePrefix("#"))
                value.asJsonObject["required"]?.let { require(it.isJsonPrimitive && it.asJsonPrimitive.isBoolean) { "required must be boolean" } }
            } else id(value.asString.removePrefix("#"))
        }
    }

    private fun textComponent(value: JsonElement) {
        if (value.isJsonPrimitive && value.asJsonPrimitive.isString) return
        if (value.isJsonArray) {
            require(value.asJsonArray.size() > 0) { "Text component array cannot be empty" }
            value.asJsonArray.forEach { textComponent(it) }
            return
        }
        require(value.isJsonObject) { "Expected text or translate component$hint" }
        val obj = value.asJsonObject
        fields(obj, setOf("text", "translate", "fallback", "with", "extra", "color", "bold", "italic", "underlined", "strikethrough", "obfuscated"))
        require(obj.has("text") != obj.has("translate")) { "Text component requires either text or translate" }
        string(obj, if (obj.has("text")) "text" else "translate")
        for (key in listOf("with", "extra")) obj[key]?.asJsonArray?.forEach { textComponent(it) }
    }

    private fun range(value: JsonElement) {
        if (value.isJsonObject) {
            fields(value.asJsonObject, setOf("min", "max"))
            val min = value.asJsonObject["min"]?.let { integer(it) } ?: 0
            val max = value.asJsonObject["max"]?.let { integer(it) } ?: Int.MAX_VALUE
            require(min >= 0 && max >= min) { "Invalid integer range" }
        } else require(integer(value) >= 0) { "Range value cannot be negative" }
    }

    private fun fields(json: JsonObject, allowed: Set<String>) {
        require(json.keySet().all { it in allowed }) { "Unsupported fields ${json.keySet() - allowed}$hint" }
    }
    private fun string(json: JsonObject, name: String): String {
        val value = json[name] ?: error("Missing $name")
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "$name must be a string" }
        return value.asString
    }
    private fun integer(value: JsonElement): Int = value.asBigDecimal.intValueExact()
    private fun id(value: String) {
        require(idPattern.matches(value) && value.substringAfter(':').split('/').none { it == "." || it == ".." }) { "Invalid namespaced resource ID '$value'" }
    }
}
