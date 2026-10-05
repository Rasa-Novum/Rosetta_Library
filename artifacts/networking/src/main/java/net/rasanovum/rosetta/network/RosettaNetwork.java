package net.rasanovum.rosetta.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.rasanovum.rosetta.util.RegistryCompat;

import java.util.HashSet;
import java.util.Set;
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
    public interface ServerboundHandler<T> {
        void handle(T packet, ServerPlayer sender);
    }

    @FunctionalInterface
    public interface ClientboundHandler<T> {
        void handle(T packet, Player player);
    }

    public static final class Channel {
        private final String namespace;
        private final Set<String> ids = new HashSet<>();

        private Channel(String namespace) {
            this.namespace = namespace;
        }

        public <T extends RosettaPacket> Channel serverbound(
                String id, Class<T> type, Function<FriendlyByteBuf, T> reader, ServerboundHandler<T> handler
        ) {
            NetworkBackend.INSTANCE.registerServerbound(PacketDefinition.serverbound(packetId(id), type, reader, handler));
            return this;
        }

        public <T extends RosettaPacket> Channel clientbound(
                String id, Class<T> type, Function<FriendlyByteBuf, T> reader, ClientboundHandler<T> handler
        ) {
            NetworkBackend.INSTANCE.registerClientbound(PacketDefinition.clientbound(packetId(id), type, reader, handler));
            return this;
        }

        private ResourceLocation packetId(String id) {
            if (!ids.add(id)) {
                throw new IllegalArgumentException("Duplicate packet id: " + namespace + ":" + id);
            }
            return RegistryCompat.getLocation(namespace, id);
        }
    }
}
