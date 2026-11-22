package net.rasanovum.runeweaver.rosetta;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.rasanovum.runeweaver.util.VersionUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetChunkRegressionTest {
    @Test
    void roundTripsOneAssetLargerThanMinecraftPayloadLimit() {
        var channel = VersionUtils.id("test", "channel");
        var id = VersionUtils.id("test", "large.png");
        byte[] bytes = new byte[1_200_000];
        Arrays.fill(bytes, (byte) 42);
        bytes[AssetSnapshotS2C.PART_BYTES] = -12;
        bytes[bytes.length - 1] = 23;
        var chunked = new AssetSnapshotS2C(channel, List.of(new AssetSnapshotS2C.Asset(id, bytes))).chunks();
        assertEquals(3, chunked.size());

        byte[][] parts = new byte[chunked.size()][];
        for (int index = 0; index < chunked.size(); index++) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                chunked.get(index).write(buffer);
                assertTrue(buffer.readableBytes() <= 512 * 1024);
                var decoded = new AssetSnapshotS2C.Chunk(buffer);
                assertEquals(channel, decoded.channel());
                assertEquals(chunked.get(0).transfer(), decoded.transfer());
                assertEquals(index, decoded.index());
                assertEquals(parts.length, decoded.count());
                assertFalse(buffer.isReadable());
                parts[index] = decoded.bytes();
            } finally {
                buffer.release();
            }
        }

        var assets = AssetSnapshotS2C.decodeParts(parts, 1, bytes.length, bytes.length);
        assertEquals(id, assets.get(0).id());
        assertArrayEquals(bytes, assets.get(0).bytes());
        assertThrows(RuntimeException.class, () -> AssetSnapshotS2C.decodeParts(parts, 1, bytes.length - 1, bytes.length));
    }

    @Test
    void publishesOnlyCompleteTransfersAndDiscardsOldParts() {
        var channelId = VersionUtils.id("test", "assembled");
        var assetId = VersionUtils.id("test", "asset");
        byte[] bytes = new byte[AssetSnapshotS2C.PART_BYTES];
        var publications = new ArrayList<byte[]>();
        var channel = new AssetChannel(channelId, "assets", ".png", bytes.length, bytes.length, 1,
                (id, assets) -> publications.add(assets.get(assetId)));
        var snapshot = new AssetSnapshotS2C(channelId, List.of(new AssetSnapshotS2C.Asset(assetId, bytes)));
        var first = snapshot.chunks();
        var second = snapshot.chunks();
        channel.receiveClientChunk(first.get(0));
        channel.receiveClientChunk(second.get(0));
        channel.receiveClientChunk(first.get(1));
        assertTrue(publications.isEmpty());
        channel.receiveClientChunk(second.get(1));
        assertEquals(1, publications.size());
        assertArrayEquals(bytes, publications.get(0));

        channel.receiveClientChunk(first.get(0));
        channel.clearClientTransfer();
        channel.receiveClientChunk(first.get(1));
        assertEquals(1, publications.size());
    }

    @Test
    void emptySnapshotStillHasOneChunkAndRejectsIncompleteFraming() {
        var channel = VersionUtils.id("test", "channel");
        var empty = new AssetSnapshotS2C(channel, List.of()).chunks();
        assertEquals(1, empty.size());
        var chunk = empty.get(0);
        assertTrue(AssetSnapshotS2C.decodeParts(new byte[][] {chunk.bytes()}, 1, 1, 1).isEmpty());
        assertThrows(IllegalArgumentException.class, () ->
                new AssetSnapshotS2C.Chunk(channel, chunk.transfer(), 1, 1, chunk.length(), chunk.bytes()));
    }
}
