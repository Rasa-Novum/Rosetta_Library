import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Sync

plugins {
    id("dev.kikugie.stonecutter")
    id("net.rasanovum.rosetta.stonecutter")
    id("fabric-loom") version "1.17.20" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("net.neoforged.moddev.legacyforge") version "2.0.147" apply false
}

stonecutter.active("1.21.1-fabric")

val releaseTargets = listOf(
    "26.3-fabric",
    "26.3-neoforge",
    "1.20.1-fabric",
    "1.20.1-forge",
    "1.21.1-fabric",
    "1.21.1-neoforge",
    "26.2-fabric",
    "26.2-neoforge",
    "26.1-fabric",
    "26.1-neoforge",
)


val cleanReleaseArtifacts = tasks.register<Delete>("cleanReleaseArtifacts") {
    delete(layout.buildDirectory.dir("release"))
}

val collectReleaseArtifacts = tasks.register<Copy>("collectReleaseArtifacts") {
    group = "build"
    dependsOn(cleanReleaseArtifacts)
    dependsOn(releaseTargets.map { ":$it:build" })
    into(layout.buildDirectory.dir("release"))

    val archiveBaseName = providers.gradleProperty("archives_base_name").get()
    val modVersion = providers.gradleProperty("mod_version").get()
    releaseTargets.forEach { target ->
        from(layout.projectDirectory.dir("versions/$target/build/libs")) {
            include("$archiveBaseName-$modVersion-$target.jar")
        }
    }

    doLast {
        val jars = layout.buildDirectory.dir("release").get().asFile
            .listFiles { file -> file.isFile && file.extension == "jar" }
            ?.toList().orEmpty()
        check(jars.size == releaseTargets.size) {
            "Expected ${releaseTargets.size} release jars, found ${jars.size}: ${jars.joinToString { it.name }}"
        }
    }
}

tasks.register("buildReleaseArtifacts") {
    group = "build"
    dependsOn(collectReleaseArtifacts)
}

tasks.register("publishMavenArtifacts") {
    group = "publishing"
    description = "Publishes the supported Rosetta mod jars into build/maven-repository."
    dependsOn(releaseTargets.map { ":$it:publishRosettaPublicationToLocalRepository" })
}

rosettaStonecutter { profiles.set(setOf("common", "rendering")) }

tasks.register<Sync>("buildConfigArtifacts") {
    group = "build"
    description = "Builds the supported optional Rosetta Config jars."
    dependsOn(releaseTargets.map { ":config:$it:build" })
    into(layout.buildDirectory.dir("release-config"))
    releaseTargets.forEach { target ->
        from(layout.projectDirectory.dir("artifacts/config/versions/$target/build/libs")) {
            include("Rosetta-Config-${providers.gradleProperty("config_version").get()}-$target.jar")
        }
    }
}

val optionalModules = listOf("networking", "attachments", "resources", "resources-sync")
optionalModules.forEach { module ->
    val display = module.split("-").joinToString("-") { it.replaceFirstChar(Char::uppercase) }
    tasks.register<Sync>("build${display.replace("-", "")}Artifacts") {
        group = "build"
        dependsOn(releaseTargets.map { ":$module:$it:build" })
        into(layout.buildDirectory.dir("release-$module"))
        releaseTargets.forEach { target ->
            from(layout.projectDirectory.dir("artifacts/$module/versions/$target/build/libs")) {
                include("Rosetta-$display-${providers.gradleProperty("module_version").get()}-$target.jar")
            }
        }
    }
}
tasks.register("buildAllArtifacts") {
    group = "build"
    dependsOn("buildReleaseArtifacts", "buildConfigArtifacts")
    dependsOn(optionalModules.map { module -> "build" + module.split("-").joinToString("") { it.replaceFirstChar(Char::uppercase) } + "Artifacts" })
}
tasks.named("publishMavenArtifacts") {
    dependsOn(releaseTargets.map { ":config:$it:publishRosettaConfigPublicationToLocalRepository" })
    dependsOn(optionalModules.flatMap { module -> releaseTargets.map { ":$module:$it:publishRosettaModulePublicationToLocalRepository" } })
}
