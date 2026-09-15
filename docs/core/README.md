# Core

`rosetta` provides compatibility helpers and lifecycle hooks. See [setup](../setup.md).

## Registries


Use `ModRegistrar` to declare content in common source. It handles registration timing for each loader and returns `RegistryHandle` values.

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

Creative-tab declarations are attached by the same single `register(RegistrationContext)` call. `add(handle)`, `add(BlockItemEntry)`, and `add(ItemLike)` cover ordinary entries. `addStack(key, supplier)` creates one dynamic stack per rebuild, while `addStacks(key, output -> ...)` can emit any number of configured variants. Entries appear in declaration order. Register all entries before attaching the registrar; duplicate entries and callback keys are rejected.


## Compatibility helpers

NBT, worlds, biomes, attributes, entities, GUI rendering, gamerules, and platform queries have helpers under `net.rasanovum.rosetta`. Browse the [source](../../src/main/java/net/rasanovum/rosetta) for available methods. Use Stonecutter for version-specific overrides, method signatures, and mixin targets.

## Client lifecycle and rendering


`ClientHooks.onDisconnect` callbacks run on the Minecraft client thread, even when a loader reports the disconnect from a networking thread. `onClientStopping` runs synchronously on the render thread before graphics teardown; do not defer GPU cleanup from that callback. Callbacks that release resources should tolerate a disconnect followed by shutdown.

`ClientCompat.screen`, `setScreen`, `mainCamera`, and `isHudHidden` hide the 26.2 client GUI and camera API changes. These helpers do not schedule calls: screen changes still belong on the client thread.

`GeometryBuffers` provides `getBuffer`, `endBatch`, `endFrame`, and `close` for immediate world overlays across all supported versions. Keep an adapter in the consumer, flush each completed render type, call `endFrame` after the overlay pass, and call `close` on disconnect and shutdown. Operations require the render thread. Storage is allocated lazily and can be recreated after closing on reconnect. Use Rosetta's client lifecycle hooks for cleanup.

For 26.1 and newer, `GpuCompat` adapts buffer writes, render passes that preserve existing attachments, vertex slot zero, non-instanced indexed draws, uniform-buffer/sampler bindings, and quad pipeline layout. The consumer owns textures, buffers, pipeline policy, and render-pass lifetime. A `writeBuffer` callback must not retain its mapped byte buffer. These helpers neither submit the frame nor free caller-owned resources.
