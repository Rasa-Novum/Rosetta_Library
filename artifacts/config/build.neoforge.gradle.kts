plugins { id("net.neoforged.moddev") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

neoForge {
    version = prop("deps.neoforge")
    mods { register(prop("mod_id")) { sourceSet(sourceSets.main.get()) } }
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

repositories { maven("https://api.modrinth.com/maven") }
dependencies {
    implementation(project(":${project.name}"))
    implementation("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
}

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))
