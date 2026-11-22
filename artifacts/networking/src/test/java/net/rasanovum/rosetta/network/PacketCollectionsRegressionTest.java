package net.rasanovum.rosetta.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacketCollectionsRegressionTest {
    @Test
    void rejectsInvalidLengthsBeforeReadingElements() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (int invalid : new int[] {-1, 4}) {
                buffer.clear();
                buffer.writeVarInt(invalid);
                assertThrows(IllegalArgumentException.class, () -> PacketCollections.read(buffer, 3, b -> fail("read element")));
            }
            buffer.clear();
            buffer.writeVarInt(65_537);
            assertThrows(IllegalArgumentException.class, () -> PacketCollections.read(buffer, FriendlyByteBuf::readVarInt));
            buffer.clear();
            PacketCollections.write(buffer, List.of(1, 2, 3), FriendlyByteBuf::writeVarInt);
            assertEquals(List.of(1, 2, 3), PacketCollections.read(buffer, 3, FriendlyByteBuf::readVarInt));
        } finally {
            buffer.release();
        }
    }
}
