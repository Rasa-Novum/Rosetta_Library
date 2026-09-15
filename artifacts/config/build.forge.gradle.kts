plugins { id("net.neoforged.moddev.legacyforge") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

legacyForge {
    version = prop("deps.forge")
    mods { register(prop("mod_id")) { sourceSet(sourceSets.main.get()) } }
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

repositories { maven("https://api.modrinth.com/maven") }
dependencies {
    implementation(project(":${project.name}"))
    implementation("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
}

tasks.jar { manifest.attributes["MixinConfigs"] = "rosetta-config.mixins.json" }

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))
