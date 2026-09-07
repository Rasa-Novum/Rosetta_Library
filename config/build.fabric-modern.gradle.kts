import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Attributes
import java.util.jar.Manifest

plugins { id("net.fabricmc.fabric-loom") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("config_version")
base.archivesName = "Rosetta-Config"

repositories { mavenCentral() }
dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
    implementation("net.fabricmc:fabric-loader:${prop("deps.loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mod_description" to groovy.json.JsonOutput.toJson(prop("config_description")),
        "mod_authors" to groovy.json.JsonOutput.toJson(prop("mod_authors").split(",").map { it.trim() }),
        "rosetta_version" to prop("mod_version"),
        "midnight_version" to prop("deps.midnightlib").substringBefore('+'),
        "minecraft_version" to prop("deps.minecraft"),
        "loader_version" to prop("deps.loader"),
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
}

val targetJavaVersion = prop("java_version").toInt()
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}
tasks.named<AbstractArchiveTask>("sourcesJar") { archiveClassifier.set("${project.name}-sources") }

fun stripFabricLoomVersion(archive: java.io.File) {
    val temporary = archive.resolveSibling("${archive.name}.tmp")
    JarFile(archive).use { input ->
        val manifest = (input.manifest ?: Manifest()).apply {
            mainAttributes.remove(Attributes.Name("Fabric-Loom-Version"))
        }

        JarOutputStream(temporary.outputStream(), manifest).use { output ->
            input.entries().asSequence()
                .filterNot { it.name == JarFile.MANIFEST_NAME }
                .forEach { entry ->
                    output.putNextEntry(JarEntry(entry.name).apply { time = entry.time })
                    if (!entry.isDirectory) {
                        input.getInputStream(entry).use { it.copyTo(output) }
                    }
                    output.closeEntry()
                }
        }
    }
    temporary.copyTo(archive, overwrite = true)
    temporary.delete()
}

tasks.jar {
    archiveClassifier.set(project.name)
    doLast { stripFabricLoomVersion(archiveFile.get().asFile) }
}

apply(from = rootProject.file("gradle/rosetta-config-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

repositories { maven("https://api.modrinth.com/maven") }
dependencies {
    implementation(project(":${project.name}"))
    implementation("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
}
