plugins { id("net.rasanovum.rosetta.pack-metadata"); id("net.fabricmc.fabric-loom") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

repositories { mavenCentral() }
dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
    implementation("net.fabricmc:fabric-loader:${prop("deps.loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
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
