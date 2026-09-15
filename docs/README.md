# Rosetta documentation

Start with [setup](setup.md) to select artifacts and dependencies. See [building](building.md) for working on this repository.

| Artifact                 | Guide                                                                    | Dependencies                                                          |
|--------------------------|--------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `rosetta` (core)         | [Registries, compatibility helpers, and lifecycle hooks](core/README.md) |                                                                       |
| `rosetta-networking`     | [Packets](networking/README.md)                                          | core                                                                  |
| `rosetta-attachments`    | [Persistent data](attachments/README.md)                                 | core                                                                  |
| `rosetta-config`         | [Server-owned settings](config/README.md)                                | [MidnightLib](https://modrinth.com/mod/midnightlib), core, networking |
| `rosetta-resources`      | [Runtime resource editing](resources/README.md)                          | core                                                                  |
| `rosetta-resources-sync` | [Datapack assets sent to clients](resources-sync/README.md)              | core, networking, resources                                           |

[Gradle plugins](gradle/README.md): [Stonecutter](gradle/stonecutter.md), [shaders](gradle/shaders.md), [datapack JSON](gradle/data.md), and [local dependencies](gradle/local-dependencies.md).
