plugins { id("net.neoforged.moddev.legacyforge") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

legacyForge {
    version = prop("deps.forge")
    mods { register(prop("mod_id")) { sourceSet(sourceSets.main.get()) } }
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

mixin {
    add(sourceSets.main.get(), "runeweaver.refmap.json")
    config("runeweaver.forge.mixins.json")
}
repositories { mavenCentral() }
dependencies {
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${prop("deps.mixinextras")}")!!)
    jarJar(implementation("io.github.llamalad7:mixinextras-forge:${prop("deps.mixinextras")}")!!)
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
