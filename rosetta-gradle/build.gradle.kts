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
dependencies {
    compileOnly("dev.kikugie:stonecutter:0.9.7")
    implementation("com.google.code.gson:gson:2.10.1")
}
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }
gradlePlugin {
    plugins {
        create("stonecutter") {
            id = "net.rasanovum.rosetta.stonecutter"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaStonecutterPlugin"
        }
        create("localDependencies") {
            id = "net.rasanovum.rosetta.local-dependencies"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaLocalDependenciesPlugin"
        }
        create("packMetadata") {
            id = "net.rasanovum.rosetta.pack-metadata"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaPackMetadataPlugin"
        }
        create("data") {
            id = "net.rasanovum.rosetta.data"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaDataPlugin"
        }
        create("shaders") {
            id = "net.rasanovum.rosetta.shaders"
            implementationClass = "net.rasanovum.rosetta.gradle.RosettaShadersPlugin"
        }
    }
}
publishing { repositories { maven { name = "local"; url = uri(layout.buildDirectory.dir("maven-repository")) } } }
