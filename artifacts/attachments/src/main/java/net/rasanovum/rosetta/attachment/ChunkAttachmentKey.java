package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class ChunkAttachmentKey<T> extends AttachmentKey<ChunkAccess, T> {
    ChunkAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.CHUNK, namespace, path, factory, codec, false);
    }

    @Override
    public ChunkAttachmentKey<T> synced() {
        super.synced();
        return this;
    }

    @Override
    public ChunkAttachmentKey<T> synced(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        super.synced(writer, reader);
        return this;
    }
}
