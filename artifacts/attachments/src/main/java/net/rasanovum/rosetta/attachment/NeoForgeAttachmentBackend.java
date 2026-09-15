package net.rasanovum.rosetta.attachment;

//? if neoforge {
/*import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.rasanovum.rosetta.Rosetta;

final class NeoForgeAttachmentBackend<O, T> implements AttachmentBackend<O, T> {
    private static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Rosetta.MOD_ID);
    private static boolean registered;

    private final Supplier<AttachmentType<T>> type;

    private NeoForgeAttachmentBackend(Supplier<AttachmentType<T>> type) { this.type = type; }

    static void register(Object eventBus) {
        if (!registered) {
            TYPES.register((IEventBus) eventBus);
            registered = true;
        }
    }

    static <O, T> AttachmentBackend<O, T> create(AttachmentKind kind, String namespace, String path,
            Supplier<T> factory, Codec<T> codec, boolean copyOnRespawn) {
        String registeredPath = namespace + "__" + kind.path + "__" + path.replace('/', '_');
        Supplier<AttachmentType<T>> type = TYPES.register(registeredPath, () -> {
            AttachmentType.Builder<T> builder = AttachmentType.builder(factory);
            serialize(builder, codec);
            if (copyOnRespawn) builder.copyOnDeath();
            return builder.build();
        });
        return new NeoForgeAttachmentBackend<>(type);
    }

    private static <T> void serialize(AttachmentType.Builder<T> builder, Codec<T> codec) {
        try {
            java.lang.reflect.Method method = java.util.Arrays.stream(builder.getClass().getMethods())
                    .filter(candidate -> candidate.getName().equals("serialize") && candidate.getParameterCount() == 1)
                    .filter(candidate -> candidate.getParameterTypes()[0].getName().equals(
                            "com.mojang.serialization.Codec"))
                    .findFirst().orElse(null);
            Object serializer = codec;
            if (method == null) {
                serializer = codec.fieldOf("value");
                method = java.util.Arrays.stream(builder.getClass().getMethods())
                        .filter(candidate -> candidate.getName().equals("serialize") && candidate.getParameterCount() == 1)
                        .filter(candidate -> candidate.getParameterTypes()[0].getName().equals(
                                "com.mojang.serialization.MapCodec"))
                        .findFirst().orElseThrow();
            }
            method.invoke(builder, serializer);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot configure NeoForge attachment persistence", exception);
        }
    }

    private IAttachmentHolder holder(O owner) {
        if (!(owner instanceof IAttachmentHolder holder)) {
            throw new IllegalArgumentException("Unsupported NeoForge attachment owner: " + owner.getClass().getName());
        }
        return holder;
    }

    public Optional<T> find(O owner) { return holder(owner).getExistingData(type.get()); }
    public void set(O owner, T value) { holder(owner).setData(type.get(), value); }
    public void remove(O owner) { holder(owner).removeData(type.get()); }
    public void markDirty(O owner) { find(owner).ifPresent(value -> holder(owner).setData(type.get(), value)); }
}
*///?}
