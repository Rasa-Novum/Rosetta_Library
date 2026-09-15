package net.rasanovum.rosetta.attachment;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
public final class LevelAttachmentKey<T> extends AttachmentKey<ServerLevel, T> {
    LevelAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.LEVEL, namespace, path, factory, codec, false);
    }
}
