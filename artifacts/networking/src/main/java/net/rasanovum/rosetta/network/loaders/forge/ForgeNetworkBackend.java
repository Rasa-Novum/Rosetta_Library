package net.rasanovum.rosetta.network.loaders.forge;

//? if forge {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.rasanovum.rosetta.util.RegistryCompat;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.rasanovum.rosetta.network.NetworkBackend;
import net.rasanovum.rosetta.network.PacketDefinition;
import net.rasanovum.rosetta.network.RosettaPacket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.concurrent.ConcurrentHashMap;

public final class ForgeNetworkBackend implements NetworkBackend {
    private static final String PROTOCOL = "2";
    private static final Predicate<String> OPTIONAL_PACKET = NetworkRegistry.acceptMissingOr(PROTOCOL);
    private final Set<String> namespaces = new HashSet<>();
    private final Map<Class<?>, SimpleChannel> packetChannels = new ConcurrentHashMap<>();

    @Override
    public <T extends RosettaPacket> void registerServerbound(PacketDefinition<T> definition) {
        register(definition, NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    public <T extends RosettaPacket> void registerClientbound(PacketDefinition<T> definition) {
        register(definition, NetworkDirection.PLAY_TO_CLIENT);
    }

    private synchronized <T extends RosettaPacket> void register(PacketDefinition<T> definition, NetworkDirection direction) {
        if (packetChannels.containsKey(definition.type())) {
            throw new IllegalArgumentException("Packet class was registered twice: " + definition.type().getName());
        }
        String namespace = definition.id().getNamespace();
        if (!namespaces.contains(namespace)) {
            NetworkRegistry.newSimpleChannel(RegistryCompat.getLocation(namespace, "rosetta"),
                    () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
            namespaces.add(namespace);
        }
        // Forge scopes its byte-sized discriminator to a channel. The packet ID names that channel.
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                packetChannelId(definition.id()), () -> PROTOCOL, OPTIONAL_PACKET, OPTIONAL_PACKET);
        channel.messageBuilder(definition.type(), 0, direction)
                .encoder(RosettaPacket::write)
                .decoder(definition.reader())
                //? if <1.19 {
                .consumer((packet, contextSupplier) -> {
                //?} else {
                .consumerNetworkThread((packet, contextSupplier) -> {
                //?}
                    var context = contextSupplier.get();
                    context.enqueueWork(() -> {
                        if (direction == NetworkDirection.PLAY_TO_SERVER) {
                            ServerPlayer sender = context.getSender();
                            if (sender != null) definition.serverboundHandler().handle(packet, sender);
                        } else {
                            Client.handle(packet, definition);
                        }
                    });
                    context.setPacketHandled(true);
                })
                .add();
        packetChannels.put(definition.type(), channel);
    }

    static ResourceLocation packetChannelId(ResourceLocation id) {
        return RegistryCompat.getLocation(id.getNamespace(), "rosetta/" + id.getPath());
    }

    @Override
    public void sendToServer(RosettaPacket packet) {
        Client.send(channel(packet), packet);
    }

    @Override
    public void sendToPlayer(RosettaPacket packet, ServerPlayer player) {
        SimpleChannel channel = channel(packet);
        if (channel.isRemotePresent(player.connection.connection)) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    private SimpleChannel channel(RosettaPacket packet) {
        SimpleChannel channel = packetChannels.get(packet.getClass());
        if (channel == null) throw new IllegalStateException("Packet was not registered: " + packet.getClass().getName());
        return channel;
    }

    private static final class Client {
        private static void send(SimpleChannel channel, RosettaPacket packet) {
            var listener = net.minecraft.client.Minecraft.getInstance().getConnection();
            if (listener != null && channel.isRemotePresent(listener.getConnection())) {
                channel.sendToServer(packet);
            }
        }

        private static <T extends RosettaPacket> void handle(T packet, PacketDefinition<T> definition) {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
                definition.clientboundHandler().handle(packet, minecraft.player);
            }
        }
    }
}
*///?}
