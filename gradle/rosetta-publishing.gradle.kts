import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

plugins.apply("maven-publish")

val module = if (project.parent == rootProject) "core" else project.parent!!.name
val displayName = project.property("archives_base_name").toString().replace("-", " ")
val publicationName = when (module) { "core" -> "rosetta"; "config" -> "rosettaConfig"; else -> "rosettaModule" }
val target = project.name
val minecraftVersion = target.substringBeforeLast('-')
val loader = target.substringAfterLast('-')
val isLegacyFabric = loader == "fabric" && minecraftVersion in setOf("1.20.1", "1.21.1")

group = if (module == "core") "net.rasanovum.rosetta" else "net.rasanovum.rosetta.$module"

val modJar = if (isLegacyFabric) {
    tasks.named<AbstractArchiveTask>("remapJar")
} else if (loader == "forge") {
    tasks.named<AbstractArchiveTask>("reobfJar")
} else {
    tasks.named<Jar>("jar")
}

val sourcesJar = if (isLegacyFabric) {
    tasks.named<AbstractArchiveTask>("remapSourcesJar")
} else {
    tasks.named<Jar>("sourcesJar")
}

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "local"
            url = rootProject.layout.buildDirectory.dir("maven-repository").get().asFile.toURI()
        }
    }
    publications {
        create<MavenPublication>(publicationName) {
            groupId = "net.rasanovum.rosetta"
            artifactId = if (module == "core") "rosetta-$target" else "rosetta-$module-$target"
            version = project.version.toString()

            artifact(modJar) {
                classifier = null
            }
            artifact(sourcesJar) {
                classifier = "sources"
            }

            pom.withXml {
                val dependencies = asNode().appendNode("dependencies")
                fun dependency(group: String, artifact: String, version: String) {
                    val node = dependencies.appendNode("dependency")
                    node.appendNode("groupId", group)
                    node.appendNode("artifactId", artifact)
                    node.appendNode("version", version)
                    node.appendNode("scope", "compile")
                }
                if (module != "core") {
                    dependency("net.rasanovum.rosetta", "rosetta-$target", rootProject.property("mod_version").toString())
                }
                if (module == "config") {
                    dependency("net.rasanovum.rosetta", "rosetta-networking-$target", rootProject.property("module_version").toString())
                    dependency("maven.modrinth", "midnightlib", project.property("deps.midnightlib").toString())
                }
                if (module == "resources-sync") {
                    dependency("net.rasanovum.rosetta", "rosetta-networking-$target", rootProject.property("module_version").toString())
                    dependency("net.rasanovum.rosetta", "rosetta-resources-$target", rootProject.property("module_version").toString())
                }
            }
            pom {
                name = "$displayName ($target)"
                description = project.property("mod_description").toString()
                url = "https://github.com/Rasa-Novum/Rosetta_Library"
                developers {
                    rootProject.property("mod_authors").toString().split(",").forEach { author ->
                        developer { name = author.trim() }
                    }
                }
                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/license/mit"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/Rasa-Novum/Rosetta_Library.git"
                    developerConnection = "scm:git:ssh://github.com/Rasa-Novum/Rosetta_Library.git"
                    url = "https://github.com/Rasa-Novum/Rosetta_Library"
                }
            }
        }
    }
}
