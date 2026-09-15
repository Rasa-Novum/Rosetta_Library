package net.rasanovum.rosetta.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.rasanovum.rosetta.util.RegistryCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** Packet registration and transport. */
public final class RosettaNetwork {
    private RosettaNetwork() {}

    public static Channel channel(String namespace) {
        return new Channel(namespace);
    }

    public static void sendToServer(RosettaPacket packet) {
        NetworkBackend.INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(RosettaPacket packet, ServerPlayer player) {
        NetworkBackend.INSTANCE.sendToPlayer(packet, player);
    }

    @FunctionalInterface
    public interface PacketHandler<T> {
        void handle(T packet, Level level, Player player);
    }

    public static final class Channel {
        private final String namespace;
        private final Set<String> ids = new HashSet<>();

        private Channel(String namespace) {
            this.namespace = namespace;
        }

        public <T extends RosettaPacket> Channel serverbound(
                String id, Class<T> type, BiConsumer<T, FriendlyByteBuf> writer,
                Function<FriendlyByteBuf, T> reader, PacketHandler<T> handler
        ) {
            register(id, type, writer, reader, handler, true);
            return this;
        }

        public <T extends RosettaPacket> Channel clientbound(
                String id, Class<T> type, BiConsumer<T, FriendlyByteBuf> writer,
                Function<FriendlyByteBuf, T> reader, PacketHandler<T> handler
        ) {
            register(id, type, writer, reader, handler, false);
            return this;
        }

        private <T extends RosettaPacket> void register(
                String id, Class<T> type, BiConsumer<T, FriendlyByteBuf> writer,
                Function<FriendlyByteBuf, T> reader, PacketHandler<T> handler, boolean serverbound
        ) {
            if (!ids.add(id)) {
                throw new IllegalArgumentException("Duplicate packet id: " + namespace + ":" + id);
            }
            PacketDefinition<T> definition = new PacketDefinition<>(
                    RegistryCompat.getLocation(namespace, id), type, writer, reader, handler
            );
            if (serverbound) NetworkBackend.INSTANCE.registerServerbound(definition);
            else NetworkBackend.INSTANCE.registerClientbound(definition);
        }
    }
}
