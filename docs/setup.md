# Setup

Choose the artifacts your mod uses from the [artifact list](README.md). All runtime artifacts use Maven group `net.rasanovum.rosetta` and version `0.2.0`.

Artifact names end in `<minecraft>-<loader>`. For example:

```kotlin
repositories {
    maven("https://raw.githubusercontent.com/Rasa-Novum/Rosetta_Library/maven/")
}

dependencies {
    implementation("net.rasanovum.rosetta:rosetta-26.3-fabric:0.2.0")
    implementation("net.rasanovum.rosetta:rosetta-networking-26.3-fabric:0.2.0")
}
```

Use the dependency configuration required by your loader plugin, such as `modImplementation` for remapped Fabric targets. Runtime artifacts must also be installed or bundled with your mod using the loader's packaging mechanism.

The repository URL above serves published releases. To use a local build, run `publishMavenArtifacts` and point your Maven repository at Rosetta's `build/maven-repository` directory. Building locally does not publish to the remote repository.

See the root [support table](../README.md) for available combinations. Use `26.1` in artifact names for Minecraft 26.1, 26.1.1, and 26.1.2.

## Loader IDs

| Artifact | Mod ID |
| --- | --- |
| Core | `rosetta_library` |
| Networking | `rosetta_networking` |
| Attachments | `rosetta_attachments` |
| Config | `rosetta_config` |
| Resources | `rosetta_resources` |
| Resources Sync | `rosetta_resources_sync` |

Gradle plugins are configured separately; see [plugin setup](gradle/README.md).
