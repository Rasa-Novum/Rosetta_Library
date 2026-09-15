import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

plugins { id("fabric-loom") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("config_version")
base.archivesName = "Rosetta-Config"

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${prop("deps.loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
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

tasks.named<AbstractArchiveTask>("remapJar") {
    archiveClassifier.set(project.name)
}
tasks.named<AbstractArchiveTask>("remapSourcesJar") { archiveClassifier.set("${project.name}-sources") }

apply(from = rootProject.file("gradle/rosetta-config-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

repositories { maven("https://api.modrinth.com/maven") }
dependencies {
    implementation(project(path = ":${project.name}", configuration = "namedElements"))
    modImplementation("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
}

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))
