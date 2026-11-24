package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.rosetta.util.RegistryCompat;

/** A persistent attachment key whose storage is supplied by the active loader, with opt-in client sync. */
public class AttachmentKey<O, T> {
    private final AttachmentKind kind;
    private final String namespace;
    private final String path;
    private final ResourceLocation id;
    private final Supplier<T> factory;
    private final Codec<T> codec;
    private final AttachmentBackend<O, T> backend;
    private volatile AttachmentSync.ValueRegistration<O, T> sync;

    AttachmentKey(AttachmentKind kind, String namespace, String path, Supplier<T> factory, Codec<T> codec,
            boolean copyOnRespawn) {
        this.kind = kind;
        this.namespace = namespace;
        this.path = path;
        this.id = RegistryCompat.getLocation(namespace, kind.path + "/" + path);
        this.factory = Objects.requireNonNull(factory, "factory");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.backend = AttachmentBackend.create(kind, namespace, path, factory, codec, copyOnRespawn);
    }

    public String namespace() { return namespace; }
    public String path() { return path; }
    public ResourceLocation id() { return id; }
    AttachmentKind kind() { return kind; }
    T newDefault() { return Objects.requireNonNull(factory.get(), "attachment factory returned null"); }

    /** Opts this attachment into automatic server-to-client sync using its persistence {@link Codec}. */
    public AttachmentKey<O, T> synced() {
        return synced(
                (buf, value) -> AttachmentSync.writeCodec(buf, codec, value),
                buf -> AttachmentSync.readCodec(buf, codec));
    }

    /** Opts this attachment into automatic server-to-client sync using a custom wire encoder/decoder. */
    public AttachmentKey<O, T> synced(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        if (this.sync != null) {
            throw new IllegalStateException("Sync already enabled for attachment " + id);
        }
        this.sync = AttachmentSync.registerValue(this, writer, reader);
        return this;
    }

    public boolean isSynced() { return sync != null; }

    public T getOrCreate(O owner) {
        return find(owner).orElseGet(() -> {
            T value = newDefault();
            set(owner, value);
            return value;
        });
    }

    public Optional<T> find(O owner) {
        return backend.find(Objects.requireNonNull(owner, "owner"));
    }

    /** Reads the latest synced value on the client for {@code owner}, if sync is enabled and present. */
    public Optional<T> findClient(O owner) {
        return findClientOwner(owner);
    }

    final Optional<T> findClientOwner(Object owner) {
        AttachmentSync.ValueRegistration<O, T> reg = this.sync;
        return reg == null ? Optional.empty() : reg.findClient(Objects.requireNonNull(owner, "owner"));
    }

    public void set(O owner, T value) {
        backend.set(owner, Objects.requireNonNull(value, "value"));
        sync(owner);
    }

    public void remove(O owner) {
        backend.remove(owner);
        sync(owner);
    }

    public void markDirty(O owner) {
        backend.markDirty(owner);
        sync(owner);
    }

    public void sync(O owner) {
        AttachmentSync.ValueRegistration<O, T> reg = this.sync;
        if (reg != null) {
            reg.broadcast(Objects.requireNonNull(owner, "owner"));
        }
    }

    public void syncTo(O owner, ServerPlayer player) {
        AttachmentSync.ValueRegistration<O, T> reg = this.sync;
        if (reg != null && player != null) {
            reg.sendTo(Objects.requireNonNull(owner, "owner"), player);
        }
    }
}
