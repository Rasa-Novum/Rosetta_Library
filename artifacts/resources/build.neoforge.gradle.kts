plugins { id("net.rasanovum.rosetta.pack-metadata"); id("net.neoforged.moddev") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

neoForge {
    version = prop("deps.neoforge")
    mods { register(prop("mod_id")) { sourceSet(sourceSets.main.get()) } }
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

tasks.processResources { exclude("runeweaver.forge.mixins.json") }

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
