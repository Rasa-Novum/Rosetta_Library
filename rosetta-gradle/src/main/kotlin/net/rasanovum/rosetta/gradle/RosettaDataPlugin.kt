package net.rasanovum.rosetta.gradle

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import org.gradle.api.Action
import org.gradle.api.file.FileCopyDetails
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.StringReader

abstract class RosettaDataExtension {
    abstract val sourceDirectory: DirectoryProperty
    abstract val minecraftVersion: Property<String>
    abstract val loader: Property<String>
}

class RosettaDataPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val options = project.extensions.create("rosettaData", RosettaDataExtension::class.java)
        options.sourceDirectory.convention(project.rootProject.layout.projectDirectory.dir(
            project.findProperty("rosetta.data.source")?.toString() ?: "src/main/rosetta-data"))
        options.minecraftVersion.convention((project.findProperty("rosetta.data.minecraft")
            ?: project.findProperty("deps.minecraft"))?.toString() ?: "")
        options.loader.convention(project.findProperty("rosetta.data.loader")?.toString()
            ?: project.name.substringAfterLast('-'))
        val generated = project.tasks.register("generateRosettaData", GenerateRosettaData::class.java) {
            sourceDirectory.set(options.sourceDirectory)
            minecraftVersion.set(options.minecraftVersion)
            loader.set(options.loader)
            outputDirectory.convention(project.layout.buildDirectory.dir("generated/rosetta-data"))
        }
        project.tasks.withType(ProcessResources::class.java).configureEach {
            from(generated.flatMap { it.outputDirectory })
            eachFile(RejectOriginalData(generated.get().outputDirectory.get().asFile))
        }
    }
}

private class RejectOriginalData(private val output: File) : Action<FileCopyDetails> {
    override fun execute(details: FileCopyDetails) {
        if (output.resolve(details.path).isFile && !details.file.toPath().startsWith(output.toPath())) {
            error("Resource ${details.path} is defined in both ordinary resources and rosetta-data; keep one source")
        }
    }
}

@CacheableTask
abstract class GenerateRosettaData : DefaultTask() {
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty
    @get:Input abstract val minecraftVersion: Property<String>
    @get:Input abstract val loader: Property<String>
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val compiler = DataCompiler(minecraftVersion.get(), loader.get())
        val root = sourceDirectory.get().asFile
        val output = outputDirectory.get().asFile
        val generated = linkedMapOf<String, JsonObject>()
        root.walkTopDown().filter { it.isFile }.sortedBy { it.relativeTo(root).invariantSeparatorsPath }.forEach { file ->
            val path = file.relativeTo(root).invariantSeparatorsPath
            require(path.endsWith(".json")) { "$path: only JSON definitions are supported" }
            try {
                val definition = StrictDataJson.read(file.readText(Charsets.UTF_8))
                val result = if (path == "pack.json") "pack.mcmeta" to compiler.pack(definition)
                    else compiler.compile(path, definition)
                if (result != null) {
                    require(generated.put(result.first, result.second) == null) { "Duplicate generated path ${result.first}" }
                }
            } catch (failure: Exception) {
                throw IllegalArgumentException("$path [${minecraftVersion.get()}/${loader.get()}]: ${failure.message}", failure)
            }
        }
        require(!output.canonicalFile.toPath().startsWith(root.canonicalFile.toPath()) &&
            !root.canonicalFile.toPath().startsWith(output.canonicalFile.toPath())) { "Generated output must be separate from source definitions" }
        output.deleteRecursively()
        output.mkdirs()
        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        generated.forEach { (path, json) ->
            val file = output.resolve(path)
            require(file.canonicalFile.toPath().startsWith(output.canonicalFile.toPath())) { "Output path escapes generated directory" }
            file.parentFile.mkdirs()
            file.writeText(gson.toJson(json) + "\n", Charsets.UTF_8)
        }
    }
}

internal object StrictDataJson {
    fun read(text: String): JsonObject = JsonReader(StringReader(text)).use { reader ->
        reader.isLenient = false
        val value = readValue(reader)
        require(reader.peek() == JsonToken.END_DOCUMENT) { "Unexpected content after JSON value" }
        require(value.isJsonObject) { "Definition must be a JSON object" }
        value.asJsonObject
    }

    private fun readValue(reader: JsonReader): JsonElement = when (reader.peek()) {
        JsonToken.BEGIN_OBJECT -> JsonObject().also { obj ->
            reader.beginObject()
            while (reader.hasNext()) {
                val key = reader.nextName()
                require(!obj.has(key)) { "Duplicate JSON key '$key' at ${reader.path}" }
                obj.add(key, readValue(reader))
            }
            reader.endObject()
        }
        JsonToken.BEGIN_ARRAY -> JsonArray().also { array ->
            reader.beginArray()
            while (reader.hasNext()) array.add(readValue(reader))
            reader.endArray()
        }
        JsonToken.STRING -> JsonPrimitive(reader.nextString())
        JsonToken.NUMBER -> JsonPrimitive(reader.nextString().toBigDecimal())
        JsonToken.BOOLEAN -> JsonPrimitive(reader.nextBoolean())
        JsonToken.NULL -> { reader.nextNull(); JsonNull.INSTANCE }
        else -> error("Unexpected JSON token at ${reader.path}")
    }
}
