package net.rasanovum.rosetta.network;

import net.minecraft.network.FriendlyByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** VarInt-sized collections, matching the former FriendlyByteBuf collection helpers. */
public final class PacketCollections {
    private PacketCollections() {}

    public static <T> void write(FriendlyByteBuf buffer, Collection<T> values, BiConsumer<FriendlyByteBuf, T> writer) {
        buffer.writeVarInt(values.size());
        for (T value : values) writer.accept(buffer, value);
    }

    public static <T> List<T> read(FriendlyByteBuf buffer, Function<FriendlyByteBuf, T> reader) {
        int size = buffer.readVarInt();
        if (size < 0) throw new IllegalArgumentException("Negative collection size: " + size);
        List<T> values = new ArrayList<>();
        for (int i = 0; i < size; i++) values.add(reader.apply(buffer));
        return values;
    }
}
