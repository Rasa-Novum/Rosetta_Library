package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;

/** Creates persistent, loader-native attachment keys. */
public final class RosettaAttachments {
    private RosettaAttachments() {}

    public static LevelAttachments level(String namespace) { return new LevelAttachments(namespace(namespace)); }
    public static EntityAttachments entity(String namespace) { return new EntityAttachments(namespace(namespace)); }
    public static PlayerAttachments player(String namespace) { return new PlayerAttachments(namespace(namespace)); }
    public static ChunkAttachments chunk(String namespace) { return new ChunkAttachments(namespace(namespace)); }
    public static BlockEntityAttachments blockEntity(String namespace) { return new BlockEntityAttachments(namespace(namespace)); }

    private static String namespace(String namespace) {
        if (namespace == null || namespace.isBlank()) throw new IllegalArgumentException("Attachment namespace cannot be blank");
        return namespace;
    }

    private static String path(String path) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("Attachment path cannot be blank");
        return path;
    }

    public record LevelAttachments(String namespace) {
        public <T> LevelAttachmentKey<T> persistent(String path, Supplier<T> factory, Codec<T> codec) {
            return new LevelAttachmentKey<>(namespace, RosettaAttachments.path(path), factory, codec);
        }
    }
    public record EntityAttachments(String namespace) {
        public <T> EntityAttachmentKey<T> persistent(String path, Supplier<T> factory, Codec<T> codec) {
            return new EntityAttachmentKey<>(namespace, RosettaAttachments.path(path), factory, codec);
        }
    }
    public record PlayerAttachments(String namespace) {
        public <T> PlayerAttachmentKey<T> persistent(String path, Supplier<T> factory, Codec<T> codec) {
            return persistent(path, true, factory, codec);
        }
        public <T> PlayerAttachmentKey<T> persistent(String path, boolean copyOnRespawn, Supplier<T> factory, Codec<T> codec) {
            return new PlayerAttachmentKey<>(namespace, RosettaAttachments.path(path), copyOnRespawn, factory, codec);
        }
    }
    /** Chunk keys work for both proto-chunks and fully generated chunks. */
    public record ChunkAttachments(String namespace) {
        public <T> ChunkAttachmentKey<T> persistent(String path, Supplier<T> factory, Codec<T> codec) {
            return new ChunkAttachmentKey<>(namespace, RosettaAttachments.path(path), factory, codec);
        }
    }
    public record BlockEntityAttachments(String namespace) {
        public <T> BlockEntityAttachmentKey<T> persistent(String path, Supplier<T> factory, Codec<T> codec) {
            return new BlockEntityAttachmentKey<>(namespace, RosettaAttachments.path(path), factory, codec);
        }
    }
}
