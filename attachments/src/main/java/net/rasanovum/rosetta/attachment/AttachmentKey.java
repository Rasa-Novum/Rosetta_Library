package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** A persistent attachment key whose storage is supplied by the active loader. */
public class AttachmentKey<O, T> {
    private final String namespace;
    private final String path;
    private final Supplier<T> factory;
    private final AttachmentBackend<O, T> backend;

    AttachmentKey(AttachmentKind kind, String namespace, String path, Supplier<T> factory, Codec<T> codec,
            boolean copyOnRespawn) {
        this.namespace = namespace;
        this.path = path;
        this.factory = Objects.requireNonNull(factory, "factory");
        this.backend = AttachmentBackend.create(kind, namespace, path, factory, Objects.requireNonNull(codec, "codec"), copyOnRespawn);
    }

    public String namespace() { return namespace; }
    public String path() { return path; }

    public T getOrCreate(O owner) {
        return find(owner).orElseGet(() -> {
            T value = Objects.requireNonNull(factory.get(), "attachment factory returned null");
            backend.set(owner, value);
            return value;
        });
    }

    public Optional<T> find(O owner) {
        Optional<T> current = backend.find(Objects.requireNonNull(owner, "owner"));
        if (current.isPresent()) return current;
        Optional<T> migrated = AttachmentMigration.tryMigrate(this, owner);
        migrated.ifPresent(value -> backend.set(owner, value));
        return migrated;
    }

    public void set(O owner, T value) { backend.set(owner, Objects.requireNonNull(value, "value")); }
    public void remove(O owner) { backend.remove(owner); }
    public void markDirty(O owner) { backend.markDirty(owner); }
}
