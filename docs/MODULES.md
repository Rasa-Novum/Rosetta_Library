## Modules

Each artifact appends the Minecraft/loader target (for example `rosetta-networking-26.2-fabric`) and uses the Maven group `net.rasanovum.rosetta`.

| Artifact | Responsibility | Rosetta dependencies |
|---|---|---|
| `rosetta` | Compatibility helpers and lifecycle hooks | None |
| `rosetta-networking` | Packet registration and transport | Core |
| `rosetta-attachments` | Persistent attached data | Core |
| `rosetta-config` | Configuration and config screens | Core, networking |
| `rosetta-resources` | Resource modification | Core |
| `rosetta-resources-sync` | Server datapack resources sent to clients | Core, networking, resources |


## Coordinates and loader IDs

Use version `0.2.0` and append `-<minecraft>-<loader>` to each artifact prefix. For example: `net.rasanovum.rosetta:rosetta-resources-sync-26.2-neoforge:0.2.0`.

Loader IDs are `rosetta_library`, `rosetta_networking`, `rosetta_attachments`, `rosetta_config`, `rosetta_resources`, and `rosetta_resources_sync`. The shared `26.1` artifacts still cover 26.1, 26.1.1, and 26.1.2.

## Building

`./gradlew buildAllArtifacts` builds 48 distributable JARs. `buildReleaseArtifacts` builds core only. Each optional module has its own collector: `buildConfigArtifacts`, `buildNetworkingArtifacts`, `buildAttachmentsArtifacts`, `buildResourcesArtifacts`, or `buildResourcesSyncArtifacts`.

`./gradlew publishMavenArtifacts` writes all 48 publications, POMs, and source JARs to `build/maven-repository`. This is local generation, not remote publication. Optional release JARs are under `build/release-<module>`.
