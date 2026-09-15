package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;

interface AttachmentBackend<O, T> {
    Optional<T> find(O owner);
    void set(O owner, T value);
    void remove(O owner);
    void markDirty(O owner);

    static <O, T> AttachmentBackend<O, T> create(
            AttachmentKind kind, String namespace, String path, Supplier<T> factory, Codec<T> codec,
            boolean copyOnRespawn) {
        //? if fabric
        return FabricAttachmentBackend.create(kind, namespace, path, factory, codec, copyOnRespawn);
        //? if neoforge
        /*return NeoForgeAttachmentBackend.create(kind, namespace, path, factory, codec, copyOnRespawn);*/
        //? if forge
        /*return ForgeAttachmentBackend.create(kind, namespace, path, factory, codec, copyOnRespawn);*/
    }
}
