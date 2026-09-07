import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

plugins.apply("maven-publish")

val target = project.name
val minecraftVersion = target.substringBeforeLast('-')
val loader = target.substringAfterLast('-')
val isLegacyFabric = loader == "fabric" && minecraftVersion in setOf("1.20.1", "1.21.1")

group = "com.rasanovum.rosetta.config"

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
        create<MavenPublication>("rosettaConfig") {
            groupId = "com.rasanovum.rosetta"
            artifactId = "rosetta-config-$target"
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
                dependency("com.rasanovum.rosetta", "rosetta-$target", rootProject.property("mod_version").toString())
                val midnightVersion = if (minecraftVersion == "26.1") "1.9.3" else "1.9.1"
                dependency("maven.modrinth", "midnightlib", "$midnightVersion+$target")
            }
            pom {
                name = "Rosetta Config ($target)"
                description = rootProject.property("config_description").toString()
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
