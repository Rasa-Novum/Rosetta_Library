package net.rasanovum.runeweaver.rosetta;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.rasanovum.rosetta.network.RosettaPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One complete server-owned snapshot for one registered asset channel. */
public record AssetSnapshotS2C(Identifier channel, List<Asset> assets) implements RosettaPacket {
    static final int MAX_WIRE_ASSET_BYTES = 16 * 1024 * 1024;
    static final int MAX_WIRE_ASSETS = 4096;
    static final long MAX_WIRE_TOTAL_BYTES = 32L * 1024 * 1024;
    static final int MAX_ENCODED_ASSET_BYTES = 40 * 1024 * 1024;
    // Leave space under 512 KiB for the identifiers, UUID, and packet framing.
    static final int PART_BYTES = 448 * 1024;

    public AssetSnapshotS2C {
        assets = assets == null ? List.of() : List.copyOf(assets);
        if (assets.size() > MAX_WIRE_ASSETS) throw new IllegalArgumentException("Too many assets: " + assets.size());
        long totalBytes = 0;
        for (Asset asset : assets) {
            if (asset.bytes.length > MAX_WIRE_ASSET_BYTES) throw new IllegalArgumentException("Asset is too large: " + asset.id());
            totalBytes += asset.bytes.length;
            if (totalBytes > MAX_WIRE_TOTAL_BYTES) throw new IllegalArgumentException("Asset snapshot is too large");
        }
    }

    public AssetSnapshotS2C(FriendlyByteBuf buf) {
        this(buf.readIdentifier(), readAssets(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(channel);
        writeAssets(buf);
    }

    private void writeAssets(FriendlyByteBuf buf) {
        buf.writeVarInt(assets.size());
        for (Asset asset : assets) {
            buf.writeIdentifier(asset.id());
            buf.writeByteArray(asset.bytes);
        }
    }

    List<Chunk> chunks() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(PART_BYTES, MAX_ENCODED_ASSET_BYTES));
        try {
            writeAssets(buffer);
            int length = buffer.readableBytes();
            int count = partCount(length);
            UUID transfer = UUID.randomUUID();
            List<Chunk> chunks = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                byte[] bytes = new byte[Math.min(PART_BYTES, buffer.readableBytes())];
                buffer.readBytes(bytes);
                chunks.add(new Chunk(channel, transfer, index, count, length, bytes));
            }
            return List.copyOf(chunks);
        } finally {
            buffer.release();
        }
    }

    static List<Asset> decodeParts(byte[][] parts, int maxAssets, int maxAssetBytes, long maxTotalBytes) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(parts.length, parts));
        try {
            List<Asset> assets = readAssets(buffer, maxAssets, maxAssetBytes, maxTotalBytes);
            if (buffer.isReadable()) throw new IllegalArgumentException("Trailing bytes in asset snapshot");
            return assets;
        } finally {
            buffer.release();
        }
    }

    private static int partCount(int length) {
        return (length + PART_BYTES - 1) / PART_BYTES;
    }

    record Chunk(Identifier channel, UUID transfer, int index, int count, int length, byte[] bytes)
            implements RosettaPacket {
        Chunk {
            if (length <= 0 || length > MAX_ENCODED_ASSET_BYTES || count != partCount(length)
                    || index < 0 || index >= count || bytes.length != Math.min(PART_BYTES, length - index * PART_BYTES)) {
                throw new IllegalArgumentException("Invalid asset snapshot chunk");
            }
        }

        Chunk(FriendlyByteBuf buf) {
            this(buf.readIdentifier(), buf.readUUID(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readByteArray(PART_BYTES));
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeIdentifier(channel);
            buf.writeUUID(transfer);
            buf.writeVarInt(index);
            buf.writeVarInt(count);
            buf.writeVarInt(length);
            buf.writeByteArray(bytes);
        }
    }

    public static void handle(AssetSnapshotS2C packet, Player player) {
        AssetChannels.handleClientSnapshot(packet);
    }

    private static List<Asset> readAssets(FriendlyByteBuf buf) {
        return readAssets(buf, MAX_WIRE_ASSETS, MAX_WIRE_ASSET_BYTES, MAX_WIRE_TOTAL_BYTES);
    }

    private static List<Asset> readAssets(FriendlyByteBuf buf, int maxAssets, int maxAssetBytes, long maxTotalBytes) {
        int count = buf.readVarInt();
        if (count < 0 || count > maxAssets) throw new IllegalArgumentException("Invalid asset count: " + count);
        List<Asset> assets = new ArrayList<>(Math.min(count, 1024));
        long remaining = maxTotalBytes;
        for (int i = 0; i < count; i++) {
            Identifier id = buf.readIdentifier();
            byte[] bytes = buf.readByteArray((int) Math.min(remaining, maxAssetBytes));
            remaining -= bytes.length;
            assets.add(new Asset(id, bytes));
        }
        return assets;
    }

    public record Asset(Identifier id, byte[] bytes) {
        public Asset {
            bytes = bytes.clone();
        }

        @Override public byte[] bytes() { return bytes.clone(); }
    }
}
