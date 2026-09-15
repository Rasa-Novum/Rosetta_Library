import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

plugins.apply("maven-publish")

val target = project.name
val minecraftVersion = target.substringBeforeLast('-')
val loader = target.substringAfterLast('-')
val isLegacyFabric = loader == "fabric" && minecraftVersion in setOf("1.20.1", "1.21.1")

group = "net.rasanovum.rosetta"

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
        create<MavenPublication>("rosetta") {
            groupId = project.group.toString()
            artifactId = "rosetta-$target"
            version = project.version.toString()

            artifact(modJar) {
                classifier = null
            }
            artifact(sourcesJar) {
                classifier = "sources"
            }

            pom {
                name = "Rosetta ($target)"
                description = rootProject.property("mod_description").toString()
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
