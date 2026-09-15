<p align="center">
  <img src="src/main/resources/logo.png" alt="Rosetta Library icon">
</p>

<h1 align="center">Rosetta Library<br></h1>

Rosetta Library is a compatibility library for Minecraft mods that support multiple game versions and mod loaders from a shared codebase.

It provides compatibility helpers for registries, NBT, worlds, biomes, attributes, entities, textures, GUI rendering, gamerules, networking, and persistent attachments. Platform utilities are available for Fabric, Forge, and NeoForge.

Rosetta Library uses [Stonecutter](https://stonecutter.kikugie.dev/) to maintain its version-specific implementations.

## Modules in 0.2.0

Each artifact appends the Minecraft/loader target (for example `rosetta-networking-26.2-fabric`) and uses the Maven group `net.rasanovum.rosetta`.

| Artifact | Responsibility | Rosetta dependencies |
|---|---|---|
| `rosetta` | Compatibility helpers and lifecycle hooks | None |
| `rosetta-networking` | Packet registration and transport | Core |
| `rosetta-attachments` | Persistent attached data | Core |
| `rosetta-config` | Configuration and config screens | Core, networking |
| `rosetta-resources` | Resource modification, formerly Runeweaver | Core |
| `rosetta-resources-sync` | Server datapack resources sent to clients | Core, networking, resources |

Config also requires MidnightLib. See [the module migration guide](docs/MODULES.md).

## Support

| MC Version | Fabric Version | Forge Version | NeoForge Version | Quilt Version |
|:----------:|:--------------:|:-------------:|:----------------:|:-------------:|
|    26.3    |       ✅        |       ❌       |        ❌         |       ❌       |
|    26.2    |       ✅        |       ❌       |        ✅         |       ❌       |
|   26.1.x   |       ✅        |       ❌       |        ✅         |       ❌       |
|   1.21.1   |       ✅        |       ❌       |        ✅         |       ❌       |
|   1.20.1   |       ✅        |       ✅       |        ❌         |       ❌       |

## Building

Build all six artifacts for every supported target:

```powershell
.\gradlew.bat buildAllArtifacts
```

Core jars are written to `build/release`; optional modules use `build/release-<module>`. Use `buildReleaseArtifacts` for core only. The `26.1` jars support Minecraft 26.1, 26.1.1, and 26.1.2 (`>=26.1 <26.2`); 26.2 and 26.3 use separate jars (26.3 is Fabric only). Rosetta Config uses the same version ranges and can be built with `buildConfigArtifacts`.

Build one target:

```powershell
.\gradlew.bat :1.21.1-fabric:build
```

Available targets are `26.2-fabric`, `26.2-neoforge`, `26.1-fabric`, `26.1-neoforge`, `1.21.1-fabric`, `1.21.1-neoforge`, `1.20.1-fabric`, and `1.20.1-forge`.

## Usage

See the [developer usage guide](docs/USAGE.md) for supported targets, networking examples, persistent attachments, and migration notes.

### Maven

Remote releases use the `maven` branch. The new 0.2.0 artifacts are currently generated locally; this migration does not publish them remotely.

```kotlin
repositories {
    maven("https://raw.githubusercontent.com/Rasa-Novum/Rosetta_Library/maven/")
}

dependencies {
    implementation("net.rasanovum.rosetta:rosetta-1.21.1-fabric:0.2.0")
}
```

Use the artifact matching the Minecraft version and loader: `rosetta-1.20.1-fabric`, `rosetta-1.20.1-forge`, `rosetta-1.21.1-fabric`, `rosetta-1.21.1-neoforge`, `rosetta-26.1-fabric`, `rosetta-26.1-neoforge`, `rosetta-26.2-fabric`, or `rosetta-26.2-neoforge`.

### Registries

Declare content once in common source. `RegistryHandle` is the same access type on every target, while `ModRegistrar` adapts Fabric's eager registration and Forge/NeoForge deferred registration.

```java
public final class ExampleContent {
    public static final ModRegistrar REGISTRAR = new ModRegistrar("example_mod");

    public static final ModRegistrar.BlockItemEntry<MachineBlock, MachineBlockItem> MACHINE =
            REGISTRAR.blockWithItem(
                    "machine",
                    MachineBlock::new,
                    BlockBehaviour.Properties.of().strength(3.0F),
                    MachineBlockItem::new,
                    new Item.Properties());

    public static final RegistryHandle<BlockEntityType<MachineBlockEntity>> MACHINE_ENTITY =
            REGISTRAR.blockEntity("machine", MachineBlockEntity::new, MACHINE.block());

    public static final RegistryHandle<BroadKnifeItem> BROAD_KNIFE =
            REGISTRAR.item("broad_knife", BroadKnifeItem::new, new Item.Properties());

    static {
        REGISTRAR.creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .add(MACHINE)
                .addStacks("generated_machine_variants", output ->
                        createMachineVariantStacks().forEach(output::accept));

        REGISTRAR.creativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .add(BROAD_KNIFE);
    }

    private ExampleContent() {}
}
```

Attach the registrar exactly once from each ordinary loader entrypoint:

```java
// Fabric
ExampleContent.REGISTRAR.register(RegistrationContext.create());

// Forge / NeoForge
ExampleContent.REGISTRAR.register(RegistrationContext.create(modEventBus));
```

Use `handle.get()` wherever the registered value is needed. Calling it before that registry's loader event finishes throws an error naming the unavailable identifier. `register(BuiltInRegistries.SOME_REGISTRY, path, factory)` covers other vanilla registries; paths are namespace-relative and duplicate paths in the same registry are rejected.

Creative-tab declarations are attached by the same single `register(RegistrationContext)` call. `add(handle)`, `add(BlockItemEntry)`, and `add(ItemLike)` cover ordinary entries. `addStack(key, supplier)` creates one dynamic stack per rebuild, while `addStacks(key, output -> ...)` can emit any number of configured variants. Entry and callback order is declaration order; duplicate entries or callback keys and declarations made after registrar attachment fail descriptively.

## Configuration Artifact

[Rosetta Config](docs/CONFIGURATION.md) provides server-owned MidnightLib settings, synchronization and disabled GUI indicators.
## Repository layout

The primary Rosetta mod lives in `src/`. Optional modules live under `artifacts/`: `config`, `networking`, `attachments`, `resources`, and `resources-sync`. Build tooling lives separately in `rosetta-gradle/`.

Directory placement does not change Gradle project names (for example, `:networking:26.3-fabric:build`) or Maven coordinates. `buildAllArtifacts` continues to collect release jars under `build/release*`.
