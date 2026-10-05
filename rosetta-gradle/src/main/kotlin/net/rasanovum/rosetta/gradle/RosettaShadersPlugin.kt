package net.rasanovum.rosetta.gradle

import org.gradle.api.Action
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File
import groovy.json.JsonSlurper

abstract class RosettaShadersExtension {
    abstract val sourceDirectory: DirectoryProperty
    abstract val minecraftVersion: Property<String>
    abstract val legacyImportNamespace: Property<String>
    abstract val shaders: SetProperty<String>
    abstract val legacyImports: SetProperty<String>
    abstract val locations: MapProperty<String, Int>
    abstract val dynamicTransforms: SetProperty<String>
    abstract val vertexShaders: MapProperty<String, String>

    fun shader(path: String, inputs: Map<String, Int> = emptyMap(), outputs: Map<String, Int> = emptyMap(),
               vanillaTransforms: Boolean = false, vertexShader: String? = null) {
        shaders.add(path)
        inputs.forEach { (name, location) -> locations.put("$path|in|$name", location) }
        outputs.forEach { (name, location) -> locations.put("$path|out|$name", location) }
        if (vanillaTransforms) dynamicTransforms.add(path)
        if (vertexShader != null) vertexShaders.put(path, vertexShader)
    }
}

class RosettaShadersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val options = project.extensions.create("rosettaShaders", RosettaShadersExtension::class.java)
        options.sourceDirectory.convention(project.layout.projectDirectory.dir("src/main/resources"))
        options.minecraftVersion.convention((project.findProperty("rosetta.shaders.minecraft") ?: project.findProperty("deps.minecraft"))?.toString() ?: "")
        options.legacyImportNamespace.convention("")
        options.shaders.convention(emptySet())
        options.legacyImports.convention(emptySet())
        options.locations.convention(emptyMap())
        options.dynamicTransforms.convention(emptySet())
        options.vertexShaders.convention(emptyMap())
        project.findProperty("rosetta.shaders.manifest")?.toString()?.let { manifestPath ->
            val manifestFile = project.rootProject.layout.projectDirectory.file(manifestPath)
            val json = project.providers.fileContents(manifestFile).asText.get()
            @Suppress("UNCHECKED_CAST")
            val manifest = JsonSlurper().parseText(json) as Map<String, Any>
            options.sourceDirectory.set(project.rootProject.file(manifest.getValue("sourceDirectory").toString()))
            options.legacyImportNamespace.set(project.findProperty("rosetta.shaders.legacyNamespace")?.toString() ?: "")
            @Suppress("UNCHECKED_CAST")
            val entries = manifest.getValue("shaders") as Map<String, Map<String, Any>>
            entries.forEach { (path, entry) ->
                if (entry["legacyOnly"] == true) {
                    if (options.minecraftVersion.get().startsWith("26.")) return@forEach
                    options.legacyImports.add(path)
                }
                fun locations(key: String): Map<String, Int> {
                    @Suppress("UNCHECKED_CAST")
                    val values = entry[key] as? Map<String, Number> ?: emptyMap()
                    return values.mapValues { it.value.toInt() }
                }
                options.shader(path, locations("inputs"), locations("outputs"),
                    entry["vanillaTransforms"] == true, entry["vertexShader"] as? String)
            }
        }
        val generated = project.tasks.register("generateRosettaShaders", GenerateRosettaShaders::class.java) {
            sourceDirectory.set(options.sourceDirectory)
            minecraftVersion.set(options.minecraftVersion)
            legacyImportNamespace.set(options.legacyImportNamespace)
            shaders.set(options.shaders)
            legacyImports.set(options.legacyImports)
            locations.set(options.locations)
            dynamicTransforms.set(options.dynamicTransforms)
            vertexShaders.set(options.vertexShaders)
            outputDirectory.convention(project.layout.buildDirectory.dir("generated/rosetta-shaders"))
        }
        project.tasks.withType(ProcessResources::class.java).configureEach {
            from(generated.flatMap { task -> task.outputDirectory })
            // Stonecutter can relocate originals. For selected paths, keep only this task's output.
            eachFile(KeepGeneratedShaders(options.shaders.get(), generated.get().outputDirectory.get().asFile))
        }
    }
}

private class KeepGeneratedShaders(private val shaders: Set<String>, private val output: File) : Action<FileCopyDetails> {
    override fun execute(details: FileCopyDetails) {
        if (details.path in shaders && !details.file.toPath().startsWith(output.toPath())) details.exclude()
    }
}

@CacheableTask
abstract class GenerateRosettaShaders : DefaultTask() {
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty
    @get:Input abstract val minecraftVersion: Property<String>
    @get:Input abstract val legacyImportNamespace: Property<String>
    @get:Input abstract val shaders: SetProperty<String>
    @get:Input abstract val legacyImports: SetProperty<String>
    @get:Input abstract val locations: MapProperty<String, Int>
    @get:Input abstract val dynamicTransforms: SetProperty<String>
    @get:Input abstract val vertexShaders: MapProperty<String, String>
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val root = sourceDirectory.get().asFile.canonicalFile
        val output = outputDirectory.get().asFile
        val modern = minecraftVersion.get().substringBefore('-').split('.').map { it.toInt() }
            .let { it[0] > 26 || it[0] == 26 && it.getOrElse(1) { 0 } >= 3 }
        val interfaces = mutableMapOf<String, Map<String, Pair<Int, String>>>()
        val rendered = shaders.get().associateWith { path ->
            val file = safeFile(root, path)
            var text = file.readText().replace("\r\n", "\n")
            if (modern) {
                text = expandIncludes(root, file, linkedSetOf())
                val stage = mutableMapOf<String, Pair<Int, String>>()
                text = text.lineSequence().joinToString("\n") { line ->
                    val declaration = Regex("^(?:flat )?(in|out) (\\w+) (\\w+);$").matchEntire(line.trim())
                    when {
                        line.startsWith("#version") -> "$line\n#extension GL_ARB_separate_shader_objects : require"
                        declaration != null -> {
                            val (direction, type, name) = declaration.destructured
                            val location = locations.get()["$path|$direction|$name"]
                                ?: error("$path: missing explicit $direction location for $name")
                            require(location >= 0) { "$path: negative location for $name" }
                            require(stage.keys.none { it.startsWith("$direction|") && stage[it]!!.first == location }) {
                                "$path: duplicate $direction location $location"
                            }
                            stage["$direction|$name"] = location to type
                            "layout(location = $location) $line"
                        }
                        else -> line
                    }
                }
                interfaces[path] = stage
                if (path in dynamicTransforms.get()) {
                    val block = Regex("layout\\(std140\\) uniform DynamicTransforms \\{[^}]*};")
                    require(block.findAll(text).count() == 1) { "$path: expected one DynamicTransforms block" }
                    text = block.replace(text, """layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};""")
                }
            } else if (minecraftVersion.get() == "1.18.2") {
                text = expandIncludes(root, file, linkedSetOf(), false)
            } else if (path in legacyImports.get() && legacyImportNamespace.get().isNotEmpty()) {
                text = text.replace("#moj_import \"", "#moj_import \"${legacyImportNamespace.get()}:")
            }
            text
        }
        if (modern) vertexShaders.get().forEach { (fragment, vertex) ->
            val outputs = interfaces.getValue(vertex).filterKeys { it.startsWith("out|") }.values.toSet()
            interfaces.getValue(fragment).filterKeys { it.startsWith("in|") }.values.forEach { input ->
                require(input in outputs) { "$fragment: input $input has no matching output in $vertex" }
            }
        }
        // This directory belongs solely to this task. Remove obsolete generated shaders.
        output.deleteRecursively()
        rendered.forEach { (path, text) ->
            val file = safeFile(output.canonicalFile, path)
            file.parentFile.mkdirs()
            file.writeText(text.replace("\n", System.lineSeparator()))
        }
    }

    private fun safeFile(root: File, path: String): File {
        val file = root.resolve(path).canonicalFile
        require(file.toPath().startsWith(root.toPath()) && file != root) { "Shader path escapes resource directory: $path" }
        return file
    }

    private fun expandIncludes(root: File, file: File, stack: MutableSet<File>, modernImports: Boolean = true): String {
        require(stack.add(file)) { "Cyclic shader include: $file" }
        try {
            return file.readText().replace("\r\n", "\n").lineSequence().joinToString("\n") { line ->
                val local = Regex("#moj_import \"([^\"]+)\"").matchEntire(line.trim())
                when {
                    local != null -> {
                        val include = file.parentFile.resolve(local.groupValues[1]).canonicalFile
                        require(include.toPath().startsWith(root.toPath())) { "Shader include escapes resource directory: $include" }
                        expandIncludes(root, include, stack, modernImports).trimEnd('\n')
                    }
                    modernImports && line.startsWith("#moj_import <") -> line.replace("#moj_import", "#include")
                    else -> line
                }
            }
        } finally {
            stack.remove(file)
        }
    }
}
