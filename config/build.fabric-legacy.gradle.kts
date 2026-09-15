plugins { id("fabric-loom") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${prop("deps.loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

repositories { maven("https://api.modrinth.com/maven") }
dependencies {
    implementation(project(path = ":${project.name}", configuration = "namedElements"))
    modImplementation("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
}

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-module-dependencies.gradle.kts"))
