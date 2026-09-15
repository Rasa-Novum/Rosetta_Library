package net.rasanovum.rosetta.attachment;

//? if fabric {
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.rasanovum.rosetta.util.RegistryCompat;

final class FabricAttachmentBackend<O, T> implements AttachmentBackend<O, T> {
    private final AttachmentType<T> type;

    private FabricAttachmentBackend(AttachmentType<T> type) { this.type = type; }

    static <O, T> AttachmentBackend<O, T> create(AttachmentKind kind, String namespace, String path,
            Supplier<T> factory, Codec<T> codec, boolean copyOnRespawn) {
        AttachmentRegistry.Builder<T> builder = AttachmentRegistry.<T>builder().persistent(codec);
        if (copyOnRespawn) builder.copyOnDeath();
        AttachmentType<T> type = builder.buildAndRegister(
                RegistryCompat.getLocation(namespace, kind.path + "/" + path));
        return new FabricAttachmentBackend<>(type);
    }

    private AttachmentTarget target(O owner) {
        if (!(owner instanceof AttachmentTarget target)) {
            throw new IllegalArgumentException("Unsupported Fabric attachment owner: " + owner.getClass().getName());
        }
        return target;
    }

    public Optional<T> find(O owner) { return Optional.ofNullable(target(owner).getAttached(type)); }
    public void set(O owner, T value) { target(owner).setAttached(type, value); }
    public void remove(O owner) { target(owner).removeAttached(type); }
    public void markDirty(O owner) { find(owner).ifPresent(value -> target(owner).setAttached(type, value)); }
}
//?}
