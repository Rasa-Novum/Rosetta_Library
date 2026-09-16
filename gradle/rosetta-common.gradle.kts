import java.util.Properties
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.language.jvm.tasks.ProcessResources

// Shared target defaults, artifact metadata, then optional artifact/target overrides.
val module = if (project.parent == rootProject) "core" else project.parent!!.name
val properties = Properties().apply {
    rootProject.file("gradle.properties").inputStream().use(::load)
    rootProject.file("versions/${project.name}/gradle.properties").inputStream().use(::load)
    if (module != "core") {
        project.parent!!.file("gradle.properties").inputStream().use(::load)
        file("gradle.properties").takeIf { it.isFile }?.inputStream()?.use(::load)
    }
}
properties.forEach { key, value -> extra[key.toString()] = value }
fun prop(key: String): String = properties.getProperty(key) ?: error("Missing property '$key' in $path")
val loader = project.name.substringAfterLast('-')
val legacyFabric = loader == "fabric" && !prop("deps.minecraft").startsWith("26.")
val targetJava = prop("java_version").toInt()
version = prop(when (module) { "core" -> "mod_version"; "config" -> "config_version"; else -> "module_version" })
configure<BasePluginExtension> { archivesName = prop("archives_base_name") }
configure<JavaPluginExtension> {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJava)
    withSourcesJar()
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJava)
}
tasks.named<AbstractArchiveTask>(if (legacyFabric) "remapJar" else "jar") {
    archiveClassifier.set(project.name)
}
tasks.named<AbstractArchiveTask>(if (legacyFabric) "remapSourcesJar" else "sourcesJar") {
    archiveClassifier.set("${project.name}-sources")
}
if (loader != "fabric" && module in setOf("core", "config")) {
    tasks.named<Jar>("jar") { exclude("net/rasanovum/rosetta/loaders/fabric/mixin/**") }
}
tasks.named<ProcessResources>("processResources") {
    if (prop("deps.minecraft") != "26.3") {
        filesMatching("rosetta-client.mixins.json") {
            filter { line: String -> line.takeUnless { it.contains("PictureInPictureRendererAccessor") } }
        }
    }
    val props = mutableMapOf<String, Any>(
        "version" to project.version,
        "module_version" to prop("module_version"),
        "rosetta_version" to prop("mod_version"),
        "icon_property" to (if (prop("deps.minecraft") in setOf("26.2", "26.3")) "iconFile" else "logoFile"),
        "mixin_compatibility" to "JAVA_$targetJava",
        "mod_name" to prop("mod_name"),
        "mod_description" to prop("mod_description"),
        "mod_license" to prop("mod_license"),
        "mod_homepage" to prop("mod_homepage"),
        "mod_sources" to prop("mod_sources"),
        "mod_issues" to prop("mod_issues"),
        "mod_authors" to prop("mod_authors"),
    )
    if (module == "config") props["midnight_version"] = prop("deps.midnightlib").substringBefore('+')
    if (loader == "fabric") {
        props["mod_description"] = groovy.json.JsonOutput.toJson(prop("mod_description"))
        props["mod_authors"] = groovy.json.JsonOutput.toJson(prop("mod_authors").split(",").map { it.trim() })
        props["minecraft_version"] = properties.getProperty("deps.minecraft_range") ?: prop("deps.minecraft")
        props["loader_version"] = prop("deps.loader")
    } else {
        props["minecraft_version_range"] = prop("deps.minecraft_range")
        props["loader_version_range"] = if (loader == "forge") prop("deps.forge_range") else "[4,)"
    }
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml",
        "runeweaver.mixins.json", "runeweaver.forge.mixins.json", "rosetta-client.mixins.json")) { expand(props) }
    if (loader != "fabric") exclude("fabric.mod.json")
    if (loader != "forge") exclude("META-INF/mods.toml")
    if (loader != "neoforge") exclude("META-INF/neoforge.mods.toml")
    if (loader != "fabric" && module in setOf("core", "config")) exclude("rosetta.mixins.json")
}
