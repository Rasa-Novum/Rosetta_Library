import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

plugins { id("net.fabricmc.fabric-loom") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("module_version")
base.archivesName = "Rosetta-Resources"

repositories { mavenCentral() }
dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
    implementation("net.fabricmc:fabric-loader:${prop("deps.loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "module_version" to prop("module_version"),
        "icon_property" to (if (prop("deps.minecraft") == "26.2") "iconFile" else "logoFile"),
        "mixin_compatibility" to prop("mixin_compatibility"),
        "mod_description" to groovy.json.JsonOutput.toJson("Resources compatibility module."),
        "mod_authors" to groovy.json.JsonOutput.toJson(prop("mod_authors").split(",").map { it.trim() }),
        "rosetta_version" to prop("mod_version"),
        "minecraft_version" to (versionProperties.getProperty("deps.minecraft_range") ?: prop("deps.minecraft")),
        "loader_version" to prop("deps.loader"),
    )
    inputs.properties(props)
    filesMatching(listOf("runeweaver.mixins.json", "runeweaver.forge.mixins.json")) { expand(props) }
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

tasks.jar {
    archiveClassifier.set(project.name)
}

apply(from = rootProject.file("gradle/rosetta-module-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))


apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

tasks.processResources { exclude("runeweaver.forge.mixins.json") }

tasks.jar { from(project.parent!!.file("LICENSE.txt")) }

repositories { mavenCentral() }
dependencies {
    testImplementation("net.fabricmc:fabric-loader-junit:${prop("deps.loader")}")
}
tasks.test { useJUnitPlatform() }

val mainSourceSet = sourceSets.main.get()
sourceSets.named("test") {
    compileClasspath += mainSourceSet.compileClasspath + mainSourceSet.output
    runtimeClasspath += mainSourceSet.runtimeClasspath + mainSourceSet.output
}
