package net.rasanovum.rosetta.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record PacketDefinition<T extends RosettaPacket>(
        ResourceLocation id,
        Class<T> type,
        Function<FriendlyByteBuf, T> reader,
        RosettaNetwork.ServerboundHandler<T> serverboundHandler,
        RosettaNetwork.ClientboundHandler<T> clientboundHandler
) {
    public PacketDefinition {
        if ((serverboundHandler == null) == (clientboundHandler == null)) {
            throw new IllegalArgumentException("Exactly one packet handler direction is required");
        }
    }

    public static <T extends RosettaPacket> PacketDefinition<T> serverbound(
            ResourceLocation id, Class<T> type, Function<FriendlyByteBuf, T> reader,
            RosettaNetwork.ServerboundHandler<T> handler
    ) {
        return new PacketDefinition<>(id, type, reader, handler, null);
    }

    public static <T extends RosettaPacket> PacketDefinition<T> clientbound(
            ResourceLocation id, Class<T> type, Function<FriendlyByteBuf, T> reader,
            RosettaNetwork.ClientboundHandler<T> handler
    ) {
        return new PacketDefinition<>(id, type, reader, null, handler);
    }
}
