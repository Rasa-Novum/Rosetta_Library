# Building Rosetta

Run commands from the repository root with the Gradle wrapper.

```powershell
# All runtime artifacts and targets
.\gradlew.bat buildAllArtifacts

# One artifact and target
.\gradlew.bat :networking:26.3-fabric:build

# Local Maven repository, including POMs and sources
.\gradlew.bat publishMavenArtifacts

# Gradle plugins
.\gradlew.bat -p rosetta-gradle build
```

`buildReleaseArtifacts` collects core jars in `build/release`. Optional artifacts have their own tasks and output folders:

| Task | Output |
| --- | --- |
| `buildConfigArtifacts` | `build/release-config` |
| `buildNetworkingArtifacts` | `build/release-networking` |
| `buildAttachmentsArtifacts` | `build/release-attachments` |
| `buildResourcesArtifacts` | `build/release-resources` |
| `buildResourcesSyncArtifacts` | `build/release-resources-sync` |

`publishMavenArtifacts` writes all targets to `build/maven-repository`. The Gradle plugin build has its own local repository at `rosetta-gradle/build/maven-repository`, populated with `-p rosetta-gradle publish`.

## Source layout

- `src/`: core library.
- `artifacts/<module>/`: optional runtime artifacts.
- `rosetta-gradle/`: Gradle plugins.
- `docs/`: documentation.
- `gradle/`: shared build scripts.
- `versions/<target>/gradle.properties`: shared Minecraft, loader, API, and Java versions.

The target matrix is declared in `settings.gradle.kts`. Stonecutter maintains the version branches. Gradle project names omit `artifacts/`: use `:config:<target>`, for example.

## Build settings

`gradle/rosetta-common.gradle.kts` loads root properties, shared target properties, artifact properties, then artifact-specific target overrides. Config's target overrides select MidnightLib versions.

Shared compilation and resource settings belong in that script. Maven publication settings belong in `gradle/rosetta-publishing.gradle.kts`. Per-loader scripts contain plugin setup and artifact-specific dependencies or resources.
