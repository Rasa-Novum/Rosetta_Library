package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.rasanovum.rosetta.util.EntityCompat;

public final class LevelAttachmentKey<T> extends AttachmentKey<ServerLevel, T> {
    private volatile CustomSync<T> customSync;

    LevelAttachmentKey(String namespace, String path, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.LEVEL, namespace, path, factory, codec, false);
    }

    @Override
    public LevelAttachmentKey<T> synced() {
        super.synced();
        return this;
    }

    @Override
    public LevelAttachmentKey<T> synced(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        super.synced(writer, reader);
        return this;
    }

    /**
     * Opts this level attachment into custom server-to-client sync (such as chunked snapshots and deltas)
     * while reusing Rosetta's dimension-change handshake, disconnect cleanup, and client revision tracking.
     */
    public LevelAttachmentKey<T> customSynced(
            Consumer<T> clearClient, BiConsumer<ServerLevel, ServerPlayer> syncToPlayer) {
        if (isSynced() || customSync != null) {
            throw new IllegalStateException("Sync already enabled for attachment " + id());
        }
        Objects.requireNonNull(clearClient, "clearClient");
        Objects.requireNonNull(syncToPlayer, "syncToPlayer");
        CustomSync<T> state = new CustomSync<>(id(), newDefault(), clearClient, syncToPlayer);
        AttachmentSync.registerLevel(state);
        this.customSync = state;
        return this;
    }

    public T get(Level level) {
        return level instanceof ServerLevel serverLevel ? getOrCreate(serverLevel) : client();
    }

    public Optional<T> findClient(Level level) {
        CustomSync<T> custom = this.customSync;
        return custom != null ? Optional.of(custom.client) : findClientOwner(level);
    }

    public T client() {
        return requireCustomSync().client;
    }

    public long clientRevision() {
        return requireCustomSync().clientRevision;
    }

    public void syncToPlayer(ServerPlayer player) {
        CustomSync<T> custom = this.customSync;
        if (custom != null) {
            custom.syncToPlayer(player);
        } else if (player != null) {
            syncTo(EntityCompat.getPlayerServerLevel(player), player);
        }
    }

    public void applyClientSnapshot(ResourceKey<Level> dimension, Consumer<T> mutator) {
        requireCustomSync().applySnapshot(dimension, mutator);
    }

    public boolean applyClientDelta(ResourceKey<Level> dimension, Consumer<T> mutator) {
        return requireCustomSync().applyDelta(dimension, mutator);
    }

    public void clearClient() {
        requireCustomSync().clearClient();
    }

    private CustomSync<T> requireCustomSync() {
        CustomSync<T> custom = this.customSync;
        if (custom == null) {
            throw new IllegalStateException("customSynced() is not enabled for attachment " + id());
        }
        return custom;
    }

    private static final class CustomSync<T> implements AttachmentSync.LevelSyncHandler {
        private final ResourceLocation id;
        private final T client;
        private final Consumer<T> clearClient;
        private final BiConsumer<ServerLevel, ServerPlayer> syncSender;
        private volatile ResourceKey<Level> clientDimension;
        private volatile long clientRevision;

        CustomSync(
                ResourceLocation id,
                T client,
                Consumer<T> clearClient,
                BiConsumer<ServerLevel, ServerPlayer> syncSender) {
            this.id = id;
            this.client = client;
            this.clearClient = clearClient;
            this.syncSender = syncSender;
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public void syncToPlayer(ServerPlayer player) {
            if (player != null) {
                syncSender.accept(EntityCompat.getPlayerServerLevel(player), player);
            }
        }

        void applySnapshot(ResourceKey<Level> dimension, Consumer<T> mutator) {
            clientDimension = Objects.requireNonNull(dimension, "dimension");
            mutator.accept(client);
            clientRevision++;
        }

        boolean applyDelta(ResourceKey<Level> dimension, Consumer<T> mutator) {
            if (!Objects.equals(clientDimension, dimension)) {
                return false;
            }
            mutator.accept(client);
            clientRevision++;
            return true;
        }

        @Override
        public void clearClient() {
            clearClient.accept(client);
            clientDimension = null;
            clientRevision++;
        }
    }
}
