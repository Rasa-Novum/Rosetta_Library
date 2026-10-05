package net.rasanovum.rosetta.attachment;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
public final class PlayerAttachmentKey<T> extends AttachmentKey<ServerPlayer, T> {
    PlayerAttachmentKey(String namespace, String path, boolean copyOnRespawn, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.PLAYER, namespace, path, factory, codec, copyOnRespawn);
    }
}
