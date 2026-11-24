package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
//? if >=1.19.3 {
import net.minecraft.core.registries.Registries;
//?} else {
/*import net.minecraft.core.Registry;*/
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.rasanovum.rosetta.Rosetta;
import net.rasanovum.rosetta.event.ClientHooks;
import net.rasanovum.rosetta.event.ServerHooks;
import net.rasanovum.rosetta.loaders.Platform;
import net.rasanovum.rosetta.network.RosettaNetwork;
import net.rasanovum.rosetta.network.RosettaPacket;
import net.rasanovum.rosetta.util.EntityCompat;
import net.rasanovum.rosetta.util.RegistryCompat;
import net.rasanovum.rosetta.util.WorldCompat;

/**
 * Shared network channel, server watch hooks, and client cache coordinator for all opt-in
 * synced attachment kinds ({@code level}, {@code player}, {@code entity}, {@code chunk}, {@code blockEntity}).
 */
final class AttachmentSync {
    private static final int MAX_SYNC_BYTES = 262_144;
    private static final RosettaNetwork.Channel CHANNEL = RosettaNetwork.channel("rosetta_attachments");

    interface LevelSyncHandler {
        ResourceLocation id();
        void syncToPlayer(ServerPlayer player);
        void clearClient();
    }

    private static final Map<ResourceLocation, LevelSyncHandler> LEVEL_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ValueRegistration<?, ?>> VALUES = new ConcurrentHashMap<>();
    private static final Map<AttachmentKind, List<ValueRegistration<?, ?>>> VALUES_BY_KIND = new EnumMap<>(AttachmentKind.class);
    private static volatile boolean initialized;
    private static volatile boolean chunkHooksRegistered;

    static {
        for (AttachmentKind kind : AttachmentKind.values()) {
            VALUES_BY_KIND.put(kind, new CopyOnWriteArrayList<>());
        }
    }

    private AttachmentSync() {}

    static void initialize() {
        if (initialized) {
            return;
        }
        synchronized (AttachmentSync.class) {
            if (initialized) {
                return;
            }
            CHANNEL.serverbound(
                            "sync_request_c2s",
                            SyncRequestPacket.class,
                            SyncRequestPacket::new,
                            AttachmentSync::handleSyncRequest)
                    .clientbound(
                            "attachment_value_s2c",
                            ValuePacket.class,
                            ValuePacket::new,
                            AttachmentSync::handleValuePacket);
            if (Platform.INSTANCE.isClientSide()) {
                ClientBridge.register();
            }
            initialized = true;
        }
    }

    static void registerLevel(LevelSyncHandler handler) {
        initialize();
        if (LEVEL_HANDLERS.putIfAbsent(handler.id(), handler) != null) {
            throw new IllegalArgumentException("Duplicate synced level attachment: " + handler.id());
        }
    }

    static <O, T> ValueRegistration<O, T> registerValue(
            AttachmentKey<O, T> key,
            BiConsumer<FriendlyByteBuf, T> writer,
            Function<FriendlyByteBuf, T> reader) {
        initialize();
        Objects.requireNonNull(writer, "writer");
        Objects.requireNonNull(reader, "reader");
        ValueRegistration<O, T> registration = new ValueRegistration<>(key, writer, reader);
        if (VALUES.putIfAbsent(key.id(), registration) != null) {
            throw new IllegalArgumentException("Duplicate synced attachment: " + key.id());
        }
        VALUES_BY_KIND.get(key.kind()).add(registration);
        if (key.kind() == AttachmentKind.CHUNK
                || key.kind() == AttachmentKind.BLOCK_ENTITY
                || key.kind() == AttachmentKind.ENTITY) {
            ensureChunkHooks();
        }
        return registration;
    }

    private static void ensureChunkHooks() {
        if (chunkHooksRegistered) {
            return;
        }
        synchronized (AttachmentSync.class) {
            if (chunkHooksRegistered) {
                return;
            }
            ServerHooks.register(new ServerHooks.Callbacks() {
                @Override
                public void onChunkSent(ServerPlayer player, ServerLevel level, LevelChunk chunk) {
                    sendPresent(AttachmentKind.CHUNK, chunk, player);
                    List<ValueRegistration<?, ?>> beRegs = VALUES_BY_KIND.get(AttachmentKind.BLOCK_ENTITY);
                    if (!beRegs.isEmpty()) {
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            sendPresent(AttachmentKind.BLOCK_ENTITY, be, player);
                        }
                    }
                    List<ValueRegistration<?, ?>> entityRegs = VALUES_BY_KIND.get(AttachmentKind.ENTITY);
                    if (!entityRegs.isEmpty()) {
                        ChunkPos pos = chunk.getPos();
                        AABB bounds = new AABB(
                                pos.getMinBlockX(), WorldCompat.minBuildHeight(level), pos.getMinBlockZ(),
                                pos.getMaxBlockX() + 1, WorldCompat.maxBuildHeight(level), pos.getMaxBlockZ() + 1);
                        for (Entity entity : level.getEntities((Entity) null, bounds, e -> true)) {
                            sendPresent(AttachmentKind.ENTITY, entity, player);
                        }
                    }
                }

                @Override
                public void onChunkUnwatched(ServerPlayer player, ServerLevel level, ChunkPos pos) {
                    List<ValueRegistration<?, ?>> chunkRegs = VALUES_BY_KIND.get(AttachmentKind.CHUNK);
                    if (chunkRegs.isEmpty()) {
                        return;
                    }
                    long chunkKey = WorldCompat.chunkPosToLong(pos);
                    for (ValueRegistration<?, ?> reg : chunkRegs) {
                        RosettaNetwork.sendToPlayer(
                                new ValuePacket(reg.key.id(), level.dimension(), chunkKey, false, new byte[0]),
                                player);
                    }
                }
            });
            chunkHooksRegistered = true;
        }
    }

    private static void sendPresent(AttachmentKind kind, Object owner, ServerPlayer player) {
        for (ValueRegistration<?, ?> reg : VALUES_BY_KIND.get(kind)) {
            reg.sendIfPresent(owner, player);
        }
    }

    private static void handleSyncRequest(SyncRequestPacket packet, ServerPlayer sender) {
        ServerLevel level = EntityCompat.getPlayerServerLevel(sender);
        if (!level.dimension().equals(packet.dimension())) {
            return;
        }
        if (packet.includeLevel()) {
            for (LevelSyncHandler handler : LEVEL_HANDLERS.values()) {
                handler.syncToPlayer(sender);
            }
            sendPresent(AttachmentKind.LEVEL, level, sender);
        }
        sendPresent(AttachmentKind.PLAYER, sender, sender);
    }

    private static void handleValuePacket(ValuePacket packet, Player player) {
        Level level = entityLevel(player);
        if (level == null || !level.isClientSide() || !level.dimension().equals(packet.dimension())) {
            return;
        }
        ValueRegistration<?, ?> registration = VALUES.get(packet.attachmentId());
        if (registration != null) {
            ClientBridge.applyValue(registration, packet);
        }
    }

    static <T> void writeCodec(FriendlyByteBuf buf, Codec<T> codec, T value) {
        Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(Rosetta.LOGGER::error)
                .orElseThrow(() -> new IllegalStateException("Failed to encode synced attachment value"));
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("v", encoded);
        buf.writeNbt(wrapper);
    }

    static <T> T readCodec(FriendlyByteBuf buf, Codec<T> codec) {
        CompoundTag wrapper = buf.readNbt();
        if (wrapper == null || !wrapper.contains("v")) {
            throw new IllegalArgumentException("Missing synced attachment NBT payload");
        }
        return codec.parse(NbtOps.INSTANCE, wrapper.get("v"))
                .resultOrPartial(Rosetta.LOGGER::error)
                .orElseThrow(() -> new IllegalArgumentException("Failed to decode synced attachment value"));
    }

    private static long ownerKey(AttachmentKind kind, Object owner) {
        return switch (kind) {
            case LEVEL, PLAYER -> 0L;
            case ENTITY -> ((Entity) owner).getId();
            case CHUNK -> WorldCompat.chunkPosToLong(((ChunkAccess) owner).getPos());
            case BLOCK_ENTITY -> ((BlockEntity) owner).getBlockPos().asLong();
        };
    }

    private static ServerLevel serverLevelOf(AttachmentKind kind, Object owner) {
        return switch (kind) {
            case LEVEL -> owner instanceof ServerLevel sl ? sl : null;
            case PLAYER -> owner instanceof ServerPlayer sp ? EntityCompat.getPlayerServerLevel(sp) : null;
            case ENTITY -> owner instanceof Entity e && entityLevel(e) instanceof ServerLevel sl ? sl : null;
            case CHUNK -> owner instanceof LevelChunk lc && lc.getLevel() instanceof ServerLevel sl ? sl : null;
            case BLOCK_ENTITY -> owner instanceof BlockEntity be && be.getLevel() instanceof ServerLevel sl ? sl : null;
        };
    }

    private static List<ServerPlayer> recipients(AttachmentKind kind, Object owner, ServerLevel level) {
        return switch (kind) {
            case LEVEL -> level.players();
            case PLAYER -> List.of((ServerPlayer) owner);
            case ENTITY -> level.getChunkSource().chunkMap.getPlayers(
                    WorldCompat.chunkPosFromBlockPos(((Entity) owner).blockPosition()), false);
            case CHUNK -> level.getChunkSource().chunkMap.getPlayers(((ChunkAccess) owner).getPos(), false);
            case BLOCK_ENTITY -> level.getChunkSource().chunkMap.getPlayers(
                    WorldCompat.chunkPosFromBlockPos(((BlockEntity) owner).getBlockPos()), false);
        };
    }

    private static void clearAllClientState() {
        LEVEL_HANDLERS.values().forEach(LevelSyncHandler::clearClient);
        VALUES.values().forEach(ValueRegistration::clearClient);
    }

    private static Level entityLevel(Entity entity) {
        //? if <1.20 {
        /*return entity.level;
        *///?} else {
        return entity.level();
        //?}
    }

    private static ResourceKey<Level> dimensionKey(ResourceLocation id) {
        //? if >=1.19.3 {
        return ResourceKey.create(Registries.DIMENSION, id);
        //?} else {
        /*return ResourceKey.create(Registry.DIMENSION_REGISTRY, id);*/
        //?}
    }

    static final class ValueRegistration<O, T> {
        private final AttachmentKey<O, T> key;
        private final BiConsumer<FriendlyByteBuf, T> writer;
        private final Function<FriendlyByteBuf, T> reader;
        private final Map<Long, T> clientValues = new ConcurrentHashMap<>();
        private volatile ResourceKey<Level> clientDimension;

        ValueRegistration(
                AttachmentKey<O, T> key,
                BiConsumer<FriendlyByteBuf, T> writer,
                Function<FriendlyByteBuf, T> reader) {
            this.key = key;
            this.writer = writer;
            this.reader = reader;
        }

        Optional<T> findClient(Object owner) {
            return Optional.ofNullable(clientValues.get(ownerKey(key.kind(), owner)));
        }

        void broadcast(O owner) {
            ServerLevel level = serverLevelOf(key.kind(), owner);
            if (level != null) {
                send(owner, level.dimension(), recipients(key.kind(), owner, level), key.find(owner).orElse(null));
            }
        }

        void sendTo(O owner, ServerPlayer player) {
            ServerLevel level = serverLevelOf(key.kind(), owner);
            if (level != null) {
                send(owner, level.dimension(), List.of(player), key.find(owner).orElse(null));
            }
        }

        @SuppressWarnings("unchecked")
        void sendIfPresent(Object owner, ServerPlayer player) {
            Optional<T> value = key.find((O) owner);
            if (value.isPresent()) {
                ServerLevel level = serverLevelOf(key.kind(), owner);
                if (level != null) {
                    send(owner, level.dimension(), List.of(player), value.get());
                }
            }
        }

        private void send(Object owner, ResourceKey<Level> dimension, Collection<ServerPlayer> targets, T value) {
            if (targets.isEmpty()) {
                return;
            }
            long targetKey = ownerKey(key.kind(), owner);
            byte[] bytes = value == null ? new byte[0] : encode(value);
            ValuePacket packet = new ValuePacket(key.id(), dimension, targetKey, value != null, bytes);
            for (ServerPlayer player : targets) {
                RosettaNetwork.sendToPlayer(packet, player);
            }
        }

        private byte[] encode(T value) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            try {
                writer.accept(buf, value);
                if (buf.readableBytes() > MAX_SYNC_BYTES) {
                    throw new IllegalArgumentException(
                            "Synced attachment " + key.id() + " exceeds " + MAX_SYNC_BYTES + " bytes");
                }
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                return bytes;
            } finally {
                buf.release();
            }
        }

        void applyClient(ValuePacket packet) {
            if (!Objects.equals(clientDimension, packet.dimension())) {
                clientValues.clear();
                clientDimension = packet.dimension();
            }
            if (!packet.present()) {
                clientValues.remove(packet.targetKey());
                return;
            }
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.payload()));
            try {
                T decoded = reader.apply(buf);
                if (decoded != null) {
                    clientValues.put(packet.targetKey(), decoded);
                }
            } catch (RuntimeException error) {
                Rosetta.LOGGER.warn("Failed to decode synced attachment {}", key.id(), error);
            } finally {
                buf.release();
            }
        }

        void clearClient() {
            clientValues.clear();
            clientDimension = null;
        }
    }

    private record SyncRequestPacket(ResourceKey<Level> dimension, boolean includeLevel) implements RosettaPacket {
        SyncRequestPacket(FriendlyByteBuf buf) {
            this(dimensionKey(buf.readResourceLocation()), buf.readBoolean());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeResourceLocation(RegistryCompat.keyLocation(dimension));
            buf.writeBoolean(includeLevel);
        }
    }

    private record ValuePacket(
            ResourceLocation attachmentId,
            ResourceKey<Level> dimension,
            long targetKey,
            boolean present,
            byte[] payload) implements RosettaPacket {
        ValuePacket(FriendlyByteBuf buf) {
            this(
                    buf.readResourceLocation(),
                    dimensionKey(buf.readResourceLocation()),
                    buf.readLong(),
                    buf.readBoolean(),
                    buf.readByteArray(MAX_SYNC_BYTES));
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeResourceLocation(attachmentId);
            buf.writeResourceLocation(RegistryCompat.keyLocation(dimension));
            buf.writeLong(targetKey);
            buf.writeBoolean(present);
            buf.writeByteArray(payload);
        }
    }

    private static final class ClientBridge {
        private static net.minecraft.client.multiplayer.ClientLevel syncedLevel;
        private static Player syncedPlayer;

        private static void register() {
            ClientHooks.register(new ClientHooks.Callbacks() {
                @Override
                public void onDisconnect() {
                    syncedLevel = null;
                    syncedPlayer = null;
                    clearAllClientState();
                }

                @Override
                public void onEndClientTick() {
                    net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                    net.minecraft.client.multiplayer.ClientLevel level =
                            minecraft.getConnection() != null ? minecraft.level : null;
                    Player player = level != null ? minecraft.player : null;
                    boolean levelChanged = syncedLevel != level;
                    boolean playerChanged = syncedPlayer != player;
                    if (!levelChanged && !playerChanged) {
                        return;
                    }
                    syncedLevel = level;
                    syncedPlayer = player;
                    if (levelChanged) {
                        clearAllClientState();
                    }
                    if (level == null) {
                        return;
                    }
                    boolean hasLevel = !LEVEL_HANDLERS.isEmpty() || !VALUES_BY_KIND.get(AttachmentKind.LEVEL).isEmpty();
                    boolean hasPlayer = !VALUES_BY_KIND.get(AttachmentKind.PLAYER).isEmpty();
                    if ((levelChanged && (hasLevel || hasPlayer)) || (playerChanged && hasPlayer)) {
                        RosettaNetwork.sendToServer(new SyncRequestPacket(level.dimension(), levelChanged));
                    }
                }
            });
        }

        private static void applyValue(ValueRegistration<?, ?> registration, ValuePacket packet) {
            net.minecraft.client.Minecraft.getInstance().execute(() -> registration.applyClient(packet));
        }
    }
}
