package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public final class EntityAttachmentKey<T> extends AttachmentKey<Entity, T> {
    EntityAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.ENTITY, namespace, path, factory, codec, false);
    }

    @Override
    public EntityAttachmentKey<T> synced() {
        super.synced();
        return this;
    }

    @Override
    public EntityAttachmentKey<T> synced(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        super.synced(writer, reader);
        return this;
    }
}
