package net.rasanovum.rosetta.attachment;

//? if forge {
/*import com.mojang.serialization.Codec;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.rasanovum.rosetta.Rosetta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ForgeAttachmentBackend<O, T> implements AttachmentBackend<O, T> {
    private static final ResourceLocation PROVIDER_ID = new ResourceLocation(Rosetta.MOD_ID, "attachments");
    private static final Capability<Store> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private static final Map<Object, Store> STORES = java.util.Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<String, Definition<?>> DEFINITIONS = new ConcurrentHashMap<>();
    private static boolean registered;

    private final Definition<T> definition;

    private ForgeAttachmentBackend(Definition<T> definition) { this.definition = definition; }

    static void register(Object modEventBus) {
        if (registered) return;
        ((IEventBus) modEventBus).addListener((RegisterCapabilitiesEvent event) -> event.register(Store.class));
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.<AttachCapabilitiesEvent<Entity>, Entity>addGenericListener(Entity.class, ForgeAttachmentBackend::attach);
        forgeBus.<AttachCapabilitiesEvent<BlockEntity>, BlockEntity>addGenericListener(BlockEntity.class, ForgeAttachmentBackend::attach);
        forgeBus.<AttachCapabilitiesEvent<Level>, Level>addGenericListener(Level.class, ForgeAttachmentBackend::attach);
        forgeBus.register(ForgeAttachmentBackend.class);
        registered = true;
    }

    static <O, T> AttachmentBackend<O, T> create(AttachmentKind kind, String namespace, String path,
            Supplier<T> factory, Codec<T> codec, boolean copyOnRespawn) {
        Definition<T> definition = new Definition<>(kind.path + "/" + namespace + "/" + path,
                factory, codec, copyOnRespawn);
        if (DEFINITIONS.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalArgumentException("Attachment was registered twice: " + definition.id());
        }
        return new ForgeAttachmentBackend<>(definition);
    }

    private static <O> void attach(AttachCapabilitiesEvent<O> event) {
        Provider provider = new Provider(event.getObject());
        event.addCapability(PROVIDER_ID, provider);
        event.addListener(provider::invalidate);
        // The weak map retains populated stores through capability invalidation for PlayerEvent.Clone.
    }

    private static Object storageOwner(Object owner) {
        return owner instanceof ImposterProtoChunk imposter ? imposter.getWrapped() : owner;
    }

    private static Store store(Object owner) {
        return STORES.computeIfAbsent(storageOwner(owner), ignored -> new Store());
    }

    public static void promoteChunk(ProtoChunk source, LevelChunk target) {
        Store attachments = STORES.get(storageOwner(source));
        if (attachments != null) STORES.put(target, attachments);
    }
    public Optional<T> find(O owner) {
        Store store = STORES.get(storageOwner(owner));
        return store == null ? Optional.empty() : store.find(definition);
    }
    public void set(O owner, T value) { store(owner).set(definition, value); AttachmentDirty.mark(owner); }
    public void remove(O owner) {
        Store store = STORES.get(storageOwner(owner));
        if (store != null) store.remove(definition);
        AttachmentDirty.mark(owner);
    }
    public void markDirty(O owner) { AttachmentDirty.mark(owner); }

    @SubscribeEvent
    public static void loadChunk(ChunkDataEvent.Load event) {
        if (event.getData().contains(Rosetta.MOD_ID + ":attachments", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = event.getData().getCompound(Rosetta.MOD_ID + ":attachments");
            Store store = STORES.get(storageOwner(event.getChunk()));
            if (store != null) store.deserialize(tag);
            else if (!tag.isEmpty()) store(event.getChunk()).deserialize(tag);
        }
    }

    @SubscribeEvent
    public static void saveChunk(ChunkDataEvent.Save event) {
        Store store = STORES.get(storageOwner(event.getChunk()));
        if (store != null) {
            CompoundTag tag = store.serialize();
            if (tag.isEmpty()) event.getData().remove(Rosetta.MOD_ID + ":attachments");
            else event.getData().put(Rosetta.MOD_ID + ":attachments", tag);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        Store from = STORES.get(event.getOriginal());
        if (from != null) {
            CompoundTag copied = from.serialize(event.isWasDeath());
            if (!copied.isEmpty()) store(event.getEntity()).merge(copied);
        }
    }

    private record Definition<T>(String id, Supplier<T> factory, Codec<T> codec, boolean copyOnRespawn) {}

    static final class Store {
        private Map<Definition<?>, Object> values;
        private CompoundTag unread;

        synchronized <T> Optional<T> find(Definition<T> definition) {
            Object value = values == null ? null : values.get(definition);
            if (value != null) return Optional.of((T) value);
            if (unread == null || !unread.contains(definition.id())) return Optional.empty();
            Optional<T> decoded = definition.codec().parse(NbtOps.INSTANCE, unread.get(definition.id()))
                    .resultOrPartial(Rosetta.LOGGER::error);
            decoded.ifPresent(result -> set(definition, result));
            return decoded;
        }

        synchronized <T> void set(Definition<T> definition, T value) {
            if (values == null) values = new IdentityHashMap<>();
            values.put(definition, value);
            if (unread != null) {
                unread.remove(definition.id());
                if (unread.isEmpty()) unread = null;
            }
        }

        synchronized void remove(Definition<?> definition) {
            if (values != null) {
                values.remove(definition);
                if (values.isEmpty()) values = null;
            }
            if (unread != null) {
                unread.remove(definition.id());
                if (unread.isEmpty()) unread = null;
            }
        }

        synchronized void deserialize(CompoundTag tag) { unread = tag.isEmpty() ? null : tag.copy(); }

        CompoundTag serialize() {
            return serialize(false);
        }

        synchronized CompoundTag serialize(boolean death) {
            CompoundTag result = new CompoundTag();
            if (values != null) {
                values.forEach((definition, value) -> {
                    if (!death || definition.copyOnRespawn()) encode(result, definition, value);
                });
            }
            // Keep data for unknown definitions and values that could not be decoded.
            if (unread != null) {
                for (String id : unread.getAllKeys()) {
                    Definition<?> definition = DEFINITIONS.get(id);
                    if ((!death || (definition != null && definition.copyOnRespawn())) && !result.contains(id)) {
                        result.put(id, unread.get(id).copy());
                    }
                }
            }
            return result;
        }

        private static <T> void encode(CompoundTag result, Definition<T> definition, Object value) {
            var tag = definition.codec().encodeStart(NbtOps.INSTANCE, (T) value)
                    .resultOrPartial(Rosetta.LOGGER::error)
                    .orElseThrow(() -> new IllegalStateException("Failed to encode attachment " + definition.id()));
            result.put(definition.id(), tag);
        }

        synchronized void merge(CompoundTag copied) {
            if (values != null) {
                values.keySet().removeIf(definition -> copied.contains(definition.id()));
            }
            if (unread == null) unread = new CompoundTag();
            for (String id : copied.getAllKeys()) unread.put(id, copied.get(id).copy());
        }
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final Object owner;
        private LazyOptional<Store> capability;

        private Provider(Object owner) {
            this.owner = owner;
        }

        public synchronized <C> @NotNull LazyOptional<C> getCapability(@NotNull Capability<C> cap, @Nullable Direction side) {
            if (cap != CAPABILITY) return LazyOptional.empty();
            if (capability == null) capability = LazyOptional.of(() -> store(owner));
            return capability.cast();
        }

        private synchronized void invalidate() {
            if (capability == null) return;
            LazyOptional<Store> previous = capability;
            capability = null;
            previous.invalidate();
        }

        public CompoundTag serializeNBT() {
            Store existing = STORES.get(storageOwner(owner));
            return existing == null ? new CompoundTag() : existing.serialize();
        }

        public void deserializeNBT(CompoundTag tag) {
            if (!tag.isEmpty()) store(owner).deserialize(tag);
        }
    }
}
*///?}
