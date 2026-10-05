package net.rasanovum.rosetta.registry;

import net.rasanovum.rosetta.util.RegistryCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
//? if >=1.19.3
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

//? if forge {
/*//? if >=1.19.3
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
*///?} else if neoforge {
/*import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

//? if >=26.1 && fabric {
/*import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
*///?} else if fabric && >=1.19.3 {
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
//?}

//? if >=26.1 && fabric {
/*import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
*///?}

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Per-mod registry facade. Declare values first, then attach the registrar once
 * from the loader entrypoint.
 */
public final class ModRegistrar {
    private final String namespace;
    private final Map<Registry<?>, Set<String>> paths = new LinkedHashMap<>();
    //? if <1.19.3 {
    /*private final Map<CreativeModeTab, CreativeTabEntries> creativeTabs = new LinkedHashMap<>();
    *///?} else {
    private final Map<ResourceKey<CreativeModeTab>, CreativeTabEntries> creativeTabs = new LinkedHashMap<>();
    //?}
    //? if fabric {
    private final List<FabricRegistration<?>> fabricRegistrations = new ArrayList<>();
    //?} else {
    /*private final Map<Registry<?>, DeferredRegister<?>> deferredRegisters = new LinkedHashMap<>();
    *///?}
    private State state = State.DECLARING;

    public ModRegistrar(String namespace) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        RegistryCompat.getLocation(namespace, "registration_probe");
    }

    public String namespace() {
        return namespace;
    }

    /** Register any value in a vanilla registry. */
    public <T, I extends T> RegistryHandle<I> register(
            Registry<T> registry,
            String path,
            Supplier<? extends I> factory
    ) {
        requireDeclaring();
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(factory, "factory");
        ResourceLocation id = id(path);
        Supplier<I> checkedFactory = () -> Objects.requireNonNull(factory.get(),
                "Factory for '" + id + "' returned null");
        Set<String> registryPaths = paths.computeIfAbsent(registry, ignored -> new LinkedHashSet<>());
        if (!registryPaths.add(path)) {
            throw new IllegalArgumentException("Duplicate registration '" + id + "' in registry '"
                    + RegistryCompat.keyLocation(registry.key()) + "'");
        }

        Handle<I> handle = new Handle<>(id);
        //? if fabric {
        fabricRegistrations.add(new FabricRegistration<>(registry, handle, checkedFactory));
        //?} else if forge {
        /*DeferredRegister<T> deferred = deferredRegister(registry);
        var holder = deferred.register(path, checkedFactory);
        handle.bind(holder, holder::isPresent);
        *///?} else if neoforge {
        /*DeferredRegister<T> deferred = deferredRegister(registry);
        var holder = deferred.register(path, checkedFactory);
        handle.bind(holder, holder::isBound);
        *///?}
        return handle;
    }

    public <B extends Block> RegistryHandle<B> block(
            String path,
            Function<BlockBehaviour.Properties, ? extends B> factory,
            BlockBehaviour.Properties properties
    ) {
        ResourceLocation id = id(path);
        //? if <1.19.3 {
        /*return register(Registry.BLOCK, path,
        *///?} else {
        return register(BuiltInRegistries.BLOCK, path,
        //?}
                () -> factory.apply(RegistryCompat.blockProperties(id, properties)));
    }

    public <I extends Item> RegistryHandle<I> item(
            String path,
            Function<Item.Properties, ? extends I> factory,
            Item.Properties properties
    ) {
        ResourceLocation id = id(path);
        //? if <1.19.3 {
        /*return register(Registry.ITEM, path,
        *///?} else {
        return register(BuiltInRegistries.ITEM, path,
        //?}
                () -> factory.apply(RegistryCompat.itemProperties(id, properties)));
    }

    /** Declare entries for an existing creative tab. Repeated calls for the same tab share one ordered list. */
    //? if <1.19.3 {
    /*public CreativeTabEntries creativeTab(CreativeModeTab tab) {
    *///?} else {
    public CreativeTabEntries creativeTab(ResourceKey<CreativeModeTab> tab) {
    //?}
        requireCreativeDeclaring(tab);
        return creativeTabs.computeIfAbsent(tab, CreativeTabEntries::new);
    }

    public <B extends Block> BlockItemEntry<B, BlockItem> blockWithItem(
            String path,
            Function<BlockBehaviour.Properties, ? extends B> blockFactory,
            BlockBehaviour.Properties blockProperties,
            Item.Properties itemProperties
    ) {
        return blockWithItem(path, blockFactory, blockProperties, BlockItem::new, itemProperties);
    }

    public <B extends Block, I extends BlockItem> BlockItemEntry<B, I> blockWithItem(
            String path,
            Function<BlockBehaviour.Properties, ? extends B> blockFactory,
            BlockBehaviour.Properties blockProperties,
            BiFunction<? super B, Item.Properties, ? extends I> itemFactory,
            Item.Properties itemProperties
    ) {
        RegistryHandle<B> block = block(path, blockFactory, blockProperties);
        ResourceLocation id = id(path);
        //? if <1.19.3 {
        /*RegistryHandle<I> item = register(Registry.ITEM, path,
        *///?} else {
        RegistryHandle<I> item = register(BuiltInRegistries.ITEM, path,
        //?}
                () -> itemFactory.apply(block.get(), RegistryCompat.itemProperties(id, itemProperties)));
        return new BlockItemEntry<>(block, item);
    }

    @SafeVarargs
    public final <T extends BlockEntity> RegistryHandle<BlockEntityType<T>> blockEntity(
            String path,
            BlockEntityFactory<T> factory,
            RegistryHandle<? extends Block>... validBlocks
    ) {
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(validBlocks, "validBlocks");
        if (validBlocks.length == 0) {
            throw new IllegalArgumentException("Block entity type '" + id(path) + "' needs at least one valid block");
        }
        RegistryHandle<? extends Block>[] blocks = validBlocks.clone();
        for (RegistryHandle<? extends Block> block : blocks) Objects.requireNonNull(block, "validBlocks");
        //? if <1.19.3 {
        /*return register(Registry.BLOCK_ENTITY_TYPE, path, () -> createBlockEntityType(factory, blocks));
        *///?} else {
        return register(BuiltInRegistries.BLOCK_ENTITY_TYPE, path, () -> createBlockEntityType(factory, blocks));
        //?}
    }

    /** Attach all declared registries. This operation is deliberately one-shot. */
    public void register(RegistrationContext context) {
        Objects.requireNonNull(context, "context");
        if (state != State.DECLARING) {
            throw new IllegalStateException("Registrar for '" + namespace + "' was already attached");
        }
        state = State.ATTACHING;
        try {
            //? if >=26.1 && fabric {
            /*for (FabricRegistration<?> registration : fabricRegistrations) registration.register();
            for (CreativeTabEntries entries : creativeTabs.values()) {
                CreativeModeTabEvents.modifyOutputEvent(entries.tab).register(output -> entries.accept(new CreativeTabOutput() {
                    @Override
                    public void accept(ItemStack stack) {
                        output.accept(stack);
                    }

                    @Override
                    public void insertAfter(ItemLike after, ItemLike item) {
                        output.insertAfter(after, item);
                    }
                }));
            }
            *///?} else if fabric {
            for (FabricRegistration<?> registration : fabricRegistrations) registration.register();
            for (CreativeTabEntries entries : creativeTabs.values()) {
                //? if <1.19.3 {
                /*LegacyCreativeTabHooks.register(entries.tab, entries::accept);
                *///?} else {
                ItemGroupEvents.modifyEntriesEvent(entries.tab).register(output -> entries.accept(new CreativeTabOutput() {
                    @Override
                    public void accept(ItemStack stack) {
                        output.accept(stack);
                    }

                    @Override
                    public void insertAfter(ItemLike after, ItemLike item) {
                        output.addAfter(after, item);
                    }
                }));
                //?}
            }
            //?} else {
            /*for (DeferredRegister<?> deferred : deferredRegisters.values()) deferred.register(context.eventBus());
            //? if <1.19.3 {
            for (CreativeTabEntries entries : creativeTabs.values()) {
                LegacyCreativeTabHooks.register(entries.tab, entries::accept);
            }
            //?} else {
            if (!creativeTabs.isEmpty()) context.eventBus().addListener(this::addCreativeTabContents);
            //?}
            *///?}
            state = State.ATTACHED;
        } catch (RuntimeException exception) {
            state = State.FAILED;
            throw exception;
        }
    }

    private ResourceLocation id(String path) {
        Objects.requireNonNull(path, "path");
        if (path.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Registration paths are relative to namespace '" + namespace
                    + "'; use '" + path.substring(path.indexOf(':') + 1) + "' instead of '" + path + "'");
        }
        return RegistryCompat.getLocation(namespace, path);
    }

    private void requireDeclaring() {
        if (state != State.DECLARING) {
            throw new IllegalStateException("Cannot add registrations to '" + namespace + "' after attachment started");
        }
    }

    //? if <1.19.3 {
    /*private void requireCreativeDeclaring(CreativeModeTab tab) {
    *///?} else {
    private void requireCreativeDeclaring(ResourceKey<CreativeModeTab> tab) {
    //?}
        Objects.requireNonNull(tab, "tab");
        if (state != State.DECLARING) {
            throw new IllegalStateException("Cannot add entries to creative tab '"
                    + tab.toString() + "' after registrar '" + namespace + "' attachment started");
        }
    }

    //? if >=1.19.3 && (forge || neoforge) {
    /*private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        CreativeTabEntries entries = creativeTabs.get(event.getTabKey());
        if (entries != null) entries.accept(new CreativeTabOutput() {
            @Override
            public void accept(ItemStack stack) {
                event.accept(stack);
            }

            //? if neoforge {
            /^@Override
            public void insertAfter(ItemLike after, ItemLike item) {
                event.insertAfter(
                        after.asItem().getDefaultInstance(),
                        item.asItem().getDefaultInstance(),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            ^///?}
        });
    }
    *///?}

    //? if forge || neoforge {
    /*@SuppressWarnings("unchecked")
    private <T> DeferredRegister<T> deferredRegister(Registry<T> registry) {
        return (DeferredRegister<T>) deferredRegisters.computeIfAbsent(registry,
                key -> DeferredRegister.create(registry.key(), namespace));
    }
    *///?}

    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityFactory<T> factory,
            RegistryHandle<? extends Block>[] handles
    ) {
        Block[] blocks = new Block[handles.length];
        for (int i = 0; i < handles.length; i++) blocks[i] = handles[i].get();
        //? if >=26.1 && fabric {
        /*return FabricBlockEntityTypeBuilder.create(factory::create, blocks).build();
        *///?} else if >=26.1 && neoforge {
        /*return new BlockEntityType<>(factory::create, Set.of(blocks));
        *///?} else {
        return BlockEntityType.Builder.of(factory::create, blocks).build(null);
        //?}
    }

    public record BlockItemEntry<B extends Block, I extends BlockItem>(
            RegistryHandle<B> block,
            RegistryHandle<I> item
    ) {}

    /** Ordered entries for one vanilla creative tab. */
    public final class CreativeTabEntries {
        //? if <1.19.3 {
        /*private final CreativeModeTab tab;
        *///?} else {
        private final ResourceKey<CreativeModeTab> tab;
        //?}
        private final List<Consumer<CreativeTabOutput>> callbacks = new ArrayList<>();
        private final Set<ResourceLocation> handles = new LinkedHashSet<>();
        private final Set<Item> items = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<String> callbackKeys = new LinkedHashSet<>();

        //? if <1.19.3 {
        /*private CreativeTabEntries(CreativeModeTab tab) {
        *///?} else {
        private CreativeTabEntries(ResourceKey<CreativeModeTab> tab) {
        //?}
            this.tab = tab;
        }

        public CreativeTabEntries add(RegistryHandle<? extends ItemLike> handle) {
            requireCreativeDeclaring(tab);
            Objects.requireNonNull(handle, "handle");
            if (!handles.add(handle.id())) {
                throw duplicate("registry handle '" + handle.id() + "'");
            }
            callbacks.add(output -> output.accept(handle.get()));
            return this;
        }

        public CreativeTabEntries add(BlockItemEntry<?, ?> entry) {
            Objects.requireNonNull(entry, "entry");
            return add(entry.item());
        }

        /** Add a registered item immediately after an existing item where the loader supports ordering. */
        public CreativeTabEntries addAfter(ItemLike after, RegistryHandle<? extends ItemLike> handle) {
            requireCreativeDeclaring(tab);
            Objects.requireNonNull(after, "after");
            Objects.requireNonNull(handle, "handle");
            if (!handles.add(handle.id())) {
                throw duplicate("registry handle '" + handle.id() + "'");
            }
            callbacks.add(output -> output.insertAfter(after, handle.get()));
            return this;
        }

        public CreativeTabEntries add(ItemLike item) {
            requireCreativeDeclaring(tab);
            Objects.requireNonNull(item, "item");
            Item value = item.asItem();
            if (!items.add(value)) {
                throw duplicate("item '" + value + "'");
            }
            callbacks.add(output -> output.accept(value));
            return this;
        }

        /** Add one freshly-created stack each time creative-tab contents are rebuilt. */
        public CreativeTabEntries addStack(String key, Supplier<ItemStack> stack) {
            Objects.requireNonNull(stack, "stack");
            return addStacks(key, output -> output.accept(Objects.requireNonNull(stack.get(),
                    "Creative-tab stack callback '" + id(key) + "' returned null")));
        }

        /** Add any number of dynamic stacks through the vanilla output interface. */
        public CreativeTabEntries addStacks(String key, Consumer<CreativeTabOutput> callback) {
            requireCreativeDeclaring(tab);
            Objects.requireNonNull(callback, "callback");
            ResourceLocation callbackId = id(key);
            if (!callbackKeys.add(key)) {
                throw duplicate("callback '" + callbackId + "'");
            }
            callbacks.add(callback);
            return this;
        }

        private void accept(CreativeTabOutput output) {
            for (Consumer<CreativeTabOutput> callback : callbacks) callback.accept(output);
        }

        private IllegalArgumentException duplicate(String entry) {
            return new IllegalArgumentException("Duplicate creative-tab " + entry + " for tab '"
                    + tab.toString() + "'");
        }
    }

    @FunctionalInterface
    public interface CreativeTabOutput {
        void accept(ItemStack stack);

        default void accept(ItemLike item) {
            accept(new ItemStack(item));
        }

        default void insertAfter(ItemLike after, ItemLike item) {
            accept(item);
        }
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos position, BlockState state);
    }

    private enum State { DECLARING, ATTACHING, ATTACHED, FAILED }

    private static final class Handle<T> implements RegistryHandle<T> {
        private final ResourceLocation id;
        private Supplier<? extends T> delegate;
        private BooleanSupplier ready;

        private Handle(ResourceLocation id) {
            this.id = id;
        }

        private void bind(Supplier<? extends T> delegate) {
            bind(delegate, () -> true);
        }

        private void bind(Supplier<? extends T> delegate, BooleanSupplier ready) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
            this.ready = Objects.requireNonNull(ready, "ready");
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public T get() {
            if (delegate == null || !ready.getAsBoolean()) throw notReady();
            T value = delegate.get();
            if (value == null) {
                throw new IllegalStateException("Registry provider for '" + id
                        + "' returned null after reporting that the value was ready");
            }
            return value;
        }

        private IllegalStateException notReady() {
            return new IllegalStateException("Registry value '" + id
                    + "' is not available yet; access it after its registry event has completed");
        }
    }

    //? if fabric {
    private record FabricRegistration<T>(
            Registry<? super T> registry,
            Handle<T> handle,
            Supplier<? extends T> factory
    ) {
        private void register() {
            T value = Objects.requireNonNull(factory.get(), "Factory for '" + handle.id() + "' returned null");
            T registered = Registry.register(registry, handle.id(), value);
            handle.bind(() -> registered);
        }
    }
    //?}
}
