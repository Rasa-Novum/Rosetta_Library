package net.rasanovum.rosetta.loaders.neoforge;

//? if neoforge {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rasanovum.rosetta.network.NetworkBackend;
import net.rasanovum.rosetta.network.PacketDefinition;
import net.rasanovum.rosetta.network.RosettaPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NeoForgeNetworkBackend implements NetworkBackend {
    private final List<PendingRegistration<?>> pending = new ArrayList<>();
    private final Set<Class<?>> packetTypes = new HashSet<>();
    private boolean registrationClosed;

    @Override
    public synchronized <T extends RosettaPacket> void registerServerbound(PacketDefinition<T> definition) {
        queue(definition, true);
    }

    @Override
    public synchronized <T extends RosettaPacket> void registerClientbound(PacketDefinition<T> definition) {
        queue(definition, false);
    }

    private <T extends RosettaPacket> void queue(PacketDefinition<T> definition, boolean serverbound) {
        if (registrationClosed) throw new IllegalStateException("Packets must be declared before NeoForge registers payload handlers");
        if (!packetTypes.add(definition.type())) {
            throw new IllegalArgumentException("Packet class was registered twice: " + definition.type().getName());
        }
        CustomPacketPayload.Type<T> payloadType = new CustomPacketPayload.Type<>(definition.id());
        RosettaPacket.registerType(definition.type(), payloadType);
        pending.add(new PendingRegistration<>(definition, payloadType, serverbound));
    }

    public synchronized void registerPayloads(RegisterPayloadHandlersEvent event) {
        registrationClosed = true;
        PayloadRegistrar registrar = event.registrar("1");
        for (PendingRegistration<?> registration : pending) registration.register(registrar);
        pending.clear();
    }

    @Override
    public void sendToServer(RosettaPacket packet) {
        Client.send(packet);
    }

    @Override
    public void sendToPlayer(RosettaPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    private record PendingRegistration<T extends RosettaPacket>(
            PacketDefinition<T> definition, CustomPacketPayload.Type<T> payloadType, boolean serverbound
    ) {
        private void register(PayloadRegistrar registrar) {
            var codec = CustomPacketPayload.codec(definition.writer()::accept, definition.reader()::apply);
            if (serverbound) {
                registrar.playToServer(payloadType, codec, (packet, context) -> context.enqueueWork(() ->
                        definition.handler().handle(packet, context.player().level(), context.player())));
            } else {
                registrar.playToClient(payloadType, codec, (packet, context) -> context.enqueueWork(() ->
                        definition.handler().handle(packet, context.player().level(), context.player())));
            }
        }
    }

    private static final class Client {
        private static void send(RosettaPacket packet) {
            NeoForgeClientSender.send(packet);
        }
    }
}
*///?}
