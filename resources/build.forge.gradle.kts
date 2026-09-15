import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

plugins { id("net.neoforged.moddev.legacyforge") }

val versionProperties = Properties().apply {
    file("gradle.properties").inputStream().use(::load)
}
fun prop(name: String): String = versionProperties.getProperty(name)
    ?: rootProject.findProperty(name)?.toString()
    ?: error("Missing property '$name'")

version = prop("module_version")
base.archivesName = "Rosetta-Resources"

legacyForge {
    version = prop("deps.forge")
    mods { register("rosetta_resources") { sourceSet(sourceSets.main.get()) } }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "module_version" to prop("module_version"),
        "icon_property" to (if (prop("deps.minecraft") == "26.2") "iconFile" else "logoFile"),
        "mixin_compatibility" to prop("mixin_compatibility"),
        "mod_description" to "Resources compatibility module.",
        "mod_authors" to prop("mod_authors"),
        "rosetta_version" to prop("mod_version"),
        "minecraft_version_range" to prop("deps.minecraft_range"),
        "loader_version_range" to prop("deps.forge_range"),
    )
    inputs.properties(props)
    filesMatching(listOf("runeweaver.mixins.json", "runeweaver.forge.mixins.json")) { expand(props) }
    filesMatching("META-INF/mods.toml") { expand(props) }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
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

mixin {
    add(sourceSets.main.get(), "runeweaver.refmap.json")
    config("runeweaver.forge.mixins.json")
}
repositories { mavenCentral() }
dependencies {
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")!!)
    jarJar(implementation("io.github.llamalad7:mixinextras-forge:0.5.4")!!)
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
}
tasks.processResources { exclude("runeweaver.mixins.json") }
tasks.jar { manifest.attributes["MixinConfigs"] = "runeweaver.forge.mixins.json" }

tasks.jar { from(project.parent!!.file("LICENSE.txt")) }

repositories { mavenCentral() }
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform() }

val mainSourceSet = sourceSets.main.get()
sourceSets.named("test") {
    compileClasspath += mainSourceSet.compileClasspath + mainSourceSet.output
    runtimeClasspath += mainSourceSet.runtimeClasspath + mainSourceSet.output
}
