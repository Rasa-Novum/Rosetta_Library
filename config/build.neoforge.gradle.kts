import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

plugins { id("net.neoforged.moddev") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("config_version")
base.archivesName = "Rosetta-Config"

neoForge {
    version = prop("deps.neoforge")
    mods { register("rosetta_config") { sourceSet(sourceSets.main.get()) } }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mod_description" to prop("config_description"),
        "mod_authors" to prop("mod_authors"),
        "rosetta_version" to prop("mod_version"),
        "midnight_version" to prop("deps.midnightlib").substringBefore('+'),
        "minecraft_version_range" to prop("deps.minecraft_range"),
        "loader_version_range" to "[4,)",
    )
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    exclude("fabric.mod.json", "META-INF/mods.toml")
    exclude("rosetta.mixins.json")
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
tasks.jar {
    archiveClassifier.set(project.name)
    exclude("net/rasanovum/rosetta/loaders/fabric/mixin/**")
}

apply(from = rootProject.file("gradle/rosetta-config-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

repositories { maven("https://api.modrinth.com/maven") }
dependencies {
    implementation(project(":${project.name}"))
    implementation("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
}
