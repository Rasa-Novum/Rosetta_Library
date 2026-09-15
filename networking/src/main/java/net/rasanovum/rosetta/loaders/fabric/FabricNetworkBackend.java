package net.rasanovum.rosetta.loaders.fabric;

//? if fabric {
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.rosetta.network.NetworkBackend;
import net.rasanovum.rosetta.network.PacketDefinition;
import net.rasanovum.rosetta.network.RosettaPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//? if >=1.21 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

public final class FabricNetworkBackend implements NetworkBackend {
    private final Map<Class<?>, PacketDefinition<?>> definitions = new ConcurrentHashMap<>();

    @Override
    public <T extends RosettaPacket> void registerServerbound(PacketDefinition<T> definition) {
        remember(definition);
        //? if >=1.21 {
        CustomPacketPayload.Type<T> payloadType = new CustomPacketPayload.Type<>(definition.id());
        var codec = CustomPacketPayload.codec(definition.writer()::accept, definition.reader()::apply);
        RosettaPacket.registerType(definition.type(), payloadType);
        //? if >=26.1
        /*PayloadTypeRegistry.serverboundPlay().register(payloadType, codec);*/
        //? if <26.1
        PayloadTypeRegistry.playC2S().register(payloadType, codec);
        ServerPlayNetworking.registerGlobalReceiver(payloadType, (packet, context) ->
                definition.handler().handle(packet, context.player().level(), context.player()));
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(definition.id(), (server, player, listener, buffer, responseSender) -> {
            T packet = definition.reader().apply(buffer);
            server.execute(() -> definition.handler().handle(packet, player.level(), player));
        });
        *///?}
    }

    @Override
    public <T extends RosettaPacket> void registerClientbound(PacketDefinition<T> definition) {
        remember(definition);
        //? if >=1.21 {
        CustomPacketPayload.Type<T> payloadType = new CustomPacketPayload.Type<>(definition.id());
        var codec = CustomPacketPayload.codec(definition.writer()::accept, definition.reader()::apply);
        RosettaPacket.registerType(definition.type(), payloadType);
        //? if >=26.1
        /*PayloadTypeRegistry.clientboundPlay().register(payloadType, codec);*/
        //? if <26.1
        PayloadTypeRegistry.playS2C().register(payloadType, codec);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Client.register(payloadType, definition);
        }
        //?} else {
        /*if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Client.register(definition);
        }
        *///?}
    }

    @Override
    public void sendToServer(RosettaPacket packet) {
        //? if >=1.21 {
        Client.send(packet);
        //?} else {
        /*PacketDefinition<RosettaPacket> definition = definition(packet);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        definition.writer().accept(packet, buffer);
        Client.send(definition, buffer);
        *///?}
    }

    @Override
    public void sendToPlayer(RosettaPacket packet, ServerPlayer player) {
        //? if >=1.21 {
        ServerPlayNetworking.send(player, packet);
        //?} else {
        /*PacketDefinition<RosettaPacket> definition = definition(packet);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        definition.writer().accept(packet, buffer);
        ServerPlayNetworking.send(player, definition.id(), buffer);
        *///?}
    }

    private void remember(PacketDefinition<?> definition) {
        if (definitions.putIfAbsent(definition.type(), definition) != null) {
            throw new IllegalArgumentException("Packet class was registered twice: " + definition.type().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private PacketDefinition<RosettaPacket> definition(RosettaPacket packet) {
        PacketDefinition<?> definition = definitions.get(packet.getClass());
        if (definition == null) throw new IllegalStateException("Packet was not registered: " + packet.getClass().getName());
        return (PacketDefinition<RosettaPacket>) definition;
    }

    private static final class Client {
        //? if >=1.21 {
        private static <T extends RosettaPacket> void register(
                CustomPacketPayload.Type<T> type, PacketDefinition<T> definition
        ) {
            ClientPlayNetworking.registerGlobalReceiver(type, (packet, context) ->
                    definition.handler().handle(packet, context.player().level(), context.player()));
        }

        private static void send(RosettaPacket packet) {
            ClientPlayNetworking.send(packet);
        }
        //?} else {
        /*private static <T extends RosettaPacket> void register(PacketDefinition<T> definition) {
            ClientPlayNetworking.registerGlobalReceiver(definition.id(), (client, listener, buffer, responseSender) -> {
                T packet = definition.reader().apply(buffer);
                client.execute(() -> {
                    if (client.level != null && client.player != null) {
                        definition.handler().handle(packet, client.level, client.player);
                    }
                });
            });
        }

        private static void send(PacketDefinition<?> definition, FriendlyByteBuf buffer) {
            ClientPlayNetworking.send(definition.id(), buffer);
        }
        *///?}
    }
}
//?}
