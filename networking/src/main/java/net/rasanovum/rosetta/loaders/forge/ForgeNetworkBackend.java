package net.rasanovum.rosetta.loaders.forge;

//? if forge {
/*import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.rasanovum.rosetta.network.NetworkBackend;
import net.rasanovum.rosetta.network.PacketDefinition;
import net.rasanovum.rosetta.network.RosettaPacket;
import net.rasanovum.rosetta.util.RegistryCompat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ForgeNetworkBackend implements NetworkBackend {
    private static final String PROTOCOL = "1";
    private final Map<String, ChannelState> channels = new ConcurrentHashMap<>();
    private final Map<Class<?>, SimpleChannel> packetChannels = new ConcurrentHashMap<>();

    @Override
    public <T extends RosettaPacket> void registerServerbound(PacketDefinition<T> definition) {
        register(definition, NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    public <T extends RosettaPacket> void registerClientbound(PacketDefinition<T> definition) {
        register(definition, NetworkDirection.PLAY_TO_CLIENT);
    }

    private <T extends RosettaPacket> void register(PacketDefinition<T> definition, NetworkDirection direction) {
        ChannelState state = channels.computeIfAbsent(definition.id().getNamespace(), this::createChannel);
        if (packetChannels.putIfAbsent(definition.type(), state.channel) != null) {
            throw new IllegalArgumentException("Packet class was registered twice: " + definition.type().getName());
        }
        state.channel.messageBuilder(definition.type(), state.discriminator.getAndIncrement(), direction)
                .encoder(definition.writer())
                .decoder(definition.reader())
                .consumerMainThread((packet, contextSupplier) -> {
                    var context = contextSupplier.get();
                    if (direction == NetworkDirection.PLAY_TO_SERVER) {
                        ServerPlayer sender = context.getSender();
                        if (sender != null) definition.handler().handle(packet, sender.level(), sender);
                    } else {
                        Client.handle(packet, definition);
                    }
                    context.setPacketHandled(true);
                })
                .add();
    }

    private ChannelState createChannel(String namespace) {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                RegistryCompat.getLocation(namespace, "rosetta"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
        );
        return new ChannelState(channel);
    }

    @Override
    public void sendToServer(RosettaPacket packet) {
        channel(packet).sendToServer(packet);
    }

    @Override
    public void sendToPlayer(RosettaPacket packet, ServerPlayer player) {
        channel(packet).send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private SimpleChannel channel(RosettaPacket packet) {
        SimpleChannel channel = packetChannels.get(packet.getClass());
        if (channel == null) throw new IllegalStateException("Packet was not registered: " + packet.getClass().getName());
        return channel;
    }

    private static final class ChannelState {
        private final SimpleChannel channel;
        private final AtomicInteger discriminator = new AtomicInteger();

        private ChannelState(SimpleChannel channel) {
            this.channel = channel;
        }
    }

    private static final class Client {
        private static <T extends RosettaPacket> void handle(T packet, PacketDefinition<T> definition) {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
                definition.handler().handle(packet, minecraft.level, minecraft.player);
            }
        }
    }
}
*///?}
