import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

plugins { id("net.neoforged.moddev.legacyforge") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("mod_version")
base.archivesName = prop("archives_base_name")

legacyForge {
    version = prop("deps.forge")
    mods { register("rosetta_library") { sourceSet(sourceSets.main.get()) } }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mod_description" to prop("mod_description"),
        "mod_authors" to prop("mod_authors"),
        "minecraft_version_range" to prop("deps.minecraft_range"),
        "loader_version_range" to prop("deps.forge_range"),
    )
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") { expand(props) }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
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

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))
