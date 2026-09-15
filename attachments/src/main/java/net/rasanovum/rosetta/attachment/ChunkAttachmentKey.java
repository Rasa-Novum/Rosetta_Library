package net.rasanovum.rosetta.attachment;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.level.chunk.ChunkAccess;
public final class ChunkAttachmentKey<T> extends AttachmentKey<ChunkAccess, T> {
    ChunkAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.CHUNK, namespace, path, factory, codec, false);
    }
}
