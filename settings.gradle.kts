pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "Rosetta"

include("config")

stonecutter {
    create(rootProject) {
        version("26.1-fabric", "26.1").buildscript = "build.fabric-modern.gradle.kts"
        version("26.1-neoforge", "26.1").buildscript = "build.neoforge.gradle.kts"
        version("1.21.1-fabric", "1.21.1").buildscript = "build.fabric-legacy.gradle.kts"
        version("1.21.1-neoforge", "1.21.1").buildscript = "build.neoforge.gradle.kts"
        version("1.20.1-fabric", "1.20.1").buildscript = "build.fabric-legacy.gradle.kts"
        version("1.20.1-forge", "1.20.1").buildscript = "build.forge.gradle.kts"
        vcsVersion = "1.21.1-fabric"
    }
    create(project(":config")) {
        version("26.1-fabric", "26.1").buildscript = "build.fabric-modern.gradle.kts"
        version("26.1-neoforge", "26.1").buildscript = "build.neoforge.gradle.kts"
        version("1.21.1-fabric", "1.21.1").buildscript = "build.fabric-legacy.gradle.kts"
        version("1.21.1-neoforge", "1.21.1").buildscript = "build.neoforge.gradle.kts"
        version("1.20.1-fabric", "1.20.1").buildscript = "build.fabric-legacy.gradle.kts"
        version("1.20.1-forge", "1.20.1").buildscript = "build.forge.gradle.kts"
        vcsVersion = "1.21.1-fabric"
    }
}
