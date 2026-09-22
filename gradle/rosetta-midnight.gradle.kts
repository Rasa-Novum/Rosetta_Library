import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

apply(from = rootProject.file("gradle/midnight-repositories.gradle.kts"))
repositories { maven("https://api.modrinth.com/maven") }

val loader = project.name.substringAfterLast('-')
val minecraft = property("deps.minecraft").toString()
val midnightVersion = property("deps.midnightlib").toString()
val midnight = if (minecraft == "1.19.2" && loader == "forge") {
    "eu.midnightdust:midnightlib-forge:$midnightVersion"
} else {
    "maven.modrinth:midnightlib:$midnightVersion"
}
val legacyFabric = loader == "fabric" && !minecraft.startsWith("26.")
dependencies {
    add(if (legacyFabric) "modImplementation" else "implementation", midnight)
    add(if (loader == "fabric") "include" else "jarJar", midnight)
}

// Expose the exact bundled binary to consumers without requiring another repository
// or letting their compile/runtime classpaths resolve an unpatched upstream copy.
val publishedMidnight = configurations.detachedConfiguration(dependencies.create(midnight)).apply {
    isTransitive = false
}
extensions.configure<PublishingExtension> {
    publications.create<MavenPublication>("midnightLib") {
        groupId = "net.rasanovum.rosetta"
        artifactId = "midnightlib-${project.name}"
        version = project.version.toString()
        artifact(provider { publishedMidnight.singleFile })
        pom {
            name = "MidnightLib (${project.name})"
            description = "The MidnightLib binary bundled by Rosetta Config."
            url = "https://github.com/TeamMidnightDust/MidnightLib"
            licenses { license { name = "MIT"; url = "https://opensource.org/license/mit" } }
        }
    }
}
tasks.named("publishRosettaConfigPublicationToLocalRepository") {
    dependsOn("publishMidnightLibPublicationToLocalRepository")
}
