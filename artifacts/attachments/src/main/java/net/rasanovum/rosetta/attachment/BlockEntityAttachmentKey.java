package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityAttachmentKey<T> extends AttachmentKey<BlockEntity, T> {
    BlockEntityAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.BLOCK_ENTITY, namespace, path, factory, codec, false);
    }

    @Override
    public BlockEntityAttachmentKey<T> synced() {
        super.synced();
        return this;
    }

    @Override
    public BlockEntityAttachmentKey<T> synced(
            BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        super.synced(writer, reader);
        return this;
    }
}
