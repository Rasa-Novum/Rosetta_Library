package net.rasanovum.rosetta.attachment;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
public final class EntityAttachmentKey<T> extends AttachmentKey<Entity, T> {
    EntityAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.ENTITY, namespace, path, factory, codec, false);
    }
}
