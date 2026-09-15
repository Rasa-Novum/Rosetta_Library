package net.rasanovum.rosetta.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record PacketDefinition<T extends RosettaPacket>(
        ResourceLocation id,
        Class<T> type,
        BiConsumer<T, FriendlyByteBuf> writer,
        Function<FriendlyByteBuf, T> reader,
        RosettaNetwork.PacketHandler<T> handler
) {}
