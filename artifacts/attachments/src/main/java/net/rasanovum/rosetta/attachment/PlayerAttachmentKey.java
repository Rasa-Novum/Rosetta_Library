package net.rasanovum.rosetta.attachment;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerAttachmentKey<T> extends AttachmentKey<ServerPlayer, T> {
    PlayerAttachmentKey(String namespace, String path, boolean copyOnRespawn, Supplier<T> factory, Codec<T> codec) {
        super(AttachmentKind.PLAYER, namespace, path, factory, codec, copyOnRespawn);
    }

    @Override
    public PlayerAttachmentKey<T> synced() {
        super.synced();
        return this;
    }

    @Override
    public PlayerAttachmentKey<T> synced(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        super.synced(writer, reader);
        return this;
    }

    public Optional<T> findClient(Player player) {
        return findClientOwner(player);
    }
}
