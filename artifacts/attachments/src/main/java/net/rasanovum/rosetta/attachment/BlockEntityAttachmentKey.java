package net.rasanovum.rosetta.attachment;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntity;
public final class BlockEntityAttachmentKey<T> extends AttachmentKey<BlockEntity, T> {
    BlockEntityAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.BLOCK_ENTITY, namespace, path, factory, codec, false);
    }
}
