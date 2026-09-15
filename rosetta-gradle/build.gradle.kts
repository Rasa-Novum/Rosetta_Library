plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}
group = "net.rasanovum.rosetta"
version = "0.1.0"
repositories {
    mavenCentral()
    maven("https://maven.kikugie.dev/releases")
}
dependencies { implementation("dev.kikugie:stonecutter:0.7.10") }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }
gradlePlugin {
    plugins {
        create("stonecutter") {
            id = "net.rasanovum.rosetta.stonecutter"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaStonecutterPlugin"
        }
        create("shaders") {
            id = "net.rasanovum.rosetta.shaders"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaShadersPlugin"
        }
    }
}
publishing { repositories { maven { name = "local"; url = uri(layout.buildDirectory.dir("maven-repository")) } } }
