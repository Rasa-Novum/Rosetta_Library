import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

plugins { id("net.neoforged.moddev") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("module_version")
base.archivesName = "Rosetta-Attachments"

neoForge {
    version = prop("deps.neoforge")
    mods { register("rosetta_attachments") { sourceSet(sourceSets.main.get()) } }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "module_version" to prop("module_version"),
        "icon_property" to (if (prop("deps.minecraft") == "26.2") "iconFile" else "logoFile"),
        "mixin_compatibility" to prop("mixin_compatibility"),
        "mod_description" to "Attachments compatibility module.",
        "mod_authors" to prop("mod_authors"),
        "rosetta_version" to prop("mod_version"),
        "minecraft_version_range" to prop("deps.minecraft_range"),
        "loader_version_range" to "[4,)",
    )
    inputs.properties(props)
    filesMatching(listOf("runeweaver.mixins.json", "runeweaver.forge.mixins.json")) { expand(props) }
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    exclude("fabric.mod.json", "META-INF/mods.toml")
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
}

apply(from = rootProject.file("gradle/rosetta-module-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))


apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))
