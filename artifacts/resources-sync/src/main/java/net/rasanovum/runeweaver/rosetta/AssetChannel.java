package net.rasanovum.runeweaver.rosetta;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.rosetta.util.ResourceCompat;
import net.minecraft.server.packs.resources.ResourceManager;
import net.rasanovum.rosetta.network.RosettaNetwork;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/** A small server-owned datapack asset channel carrying raw bytes. */
public final class AssetChannel {
    private final Identifier id;
    private final String folder;
    private final String extension;
    private final int maxAssetBytes;
    private final long maxTotalBytes;
    private final int maxAssets;
    private final BiConsumer<Identifier, Map<Identifier, byte[]>> clientConsumer;
    private final Map<MinecraftServer, List<AssetSnapshotS2C.Chunk>> serverSnapshots = new ConcurrentHashMap<>();
    private PendingSnapshot pendingSnapshot;

    private static final class PendingSnapshot {
        private final UUID transfer;
        private final int length;
        private final byte[][] parts;
        private int nextIndex;

        private PendingSnapshot(AssetSnapshotS2C.Chunk first) {
            transfer = first.transfer();
            length = first.length();
            parts = new byte[first.count()][];
        }

        private boolean expects(AssetSnapshotS2C.Chunk part) {
            return transfer.equals(part.transfer()) && length == part.length() && nextIndex == part.index();
        }
    }

    AssetChannel(Identifier id, String folder, String extension, int maxAssetBytes,
                         long maxTotalBytes, int maxAssets,
                         BiConsumer<Identifier, Map<Identifier, byte[]>> clientConsumer) {
        this.id = id;
        this.folder = folder;
        this.extension = extension;
        this.maxAssetBytes = maxAssetBytes;
        this.maxTotalBytes = maxTotalBytes;
        this.maxAssets = maxAssets;
        this.clientConsumer = clientConsumer;
    }

    Identifier id() {
        return id;
    }

    public static AssetChannel register(Identifier id, String folder, String extension,
                                        int maxAssetBytes, long maxTotalBytes, int maxAssets,
                                        BiConsumer<Identifier, Map<Identifier, byte[]>> clientConsumer) {
        if (folder.isBlank() || extension.isEmpty() || maxAssetBytes <= 0 || maxTotalBytes <= 0 || maxAssets <= 0 ||
                maxAssetBytes > AssetSnapshotS2C.MAX_WIRE_ASSET_BYTES ||
                maxTotalBytes > AssetSnapshotS2C.MAX_WIRE_TOTAL_BYTES ||
                maxAssets > AssetSnapshotS2C.MAX_WIRE_ASSETS) {
            throw new IllegalArgumentException("invalid asset channel configuration");
        }
        if (clientConsumer == null) throw new NullPointerException("clientConsumer");
        return AssetChannels.register(new AssetChannel(id, folder, extension, maxAssetBytes,
                maxTotalBytes, maxAssets, clientConsumer));
    }

    void refresh(MinecraftServer server) {
        List<AssetSnapshotS2C.Chunk> snapshot = load(server.getResourceManager());
        serverSnapshots.put(server, snapshot);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) send(player, snapshot);
    }

    void sync(ServerPlayer player) {
        MinecraftServer server = server(player);
        if (server == null) return;
        List<AssetSnapshotS2C.Chunk> snapshot = serverSnapshots.computeIfAbsent(server,
                active -> load(active.getResourceManager()));
        send(player, snapshot);
    }

    void clear(MinecraftServer server) {
        serverSnapshots.remove(server);
    }

    void receiveClientChunk(AssetSnapshotS2C.Chunk part) {
        if (part.index() == 0) pendingSnapshot = new PendingSnapshot(part);
        PendingSnapshot pending = pendingSnapshot;
        if (pending == null || !pending.expects(part)) return;
        pending.parts[pending.nextIndex++] = part.bytes();
        if (pending.nextIndex == pending.parts.length) {
            pendingSnapshot = null;
            List<AssetSnapshotS2C.Asset> assets;
            try {
                assets = AssetSnapshotS2C.decodeParts(pending.parts, maxAssets, maxAssetBytes, maxTotalBytes);
            } catch (RuntimeException e) {
                AssetChannels.LOGGER.warn("Ignoring malformed asset snapshot for {}", id, e);
                return;
            }
            receiveClientSnapshot(assets);
        }
    }

    void clearClientTransfer() {
        pendingSnapshot = null;
    }

    void receiveClientSnapshot(List<AssetSnapshotS2C.Asset> assets) {
        Map<Identifier, byte[]> snapshot = new LinkedHashMap<>();
        for (AssetSnapshotS2C.Asset asset : assets) snapshot.put(asset.id(), asset.bytes());
        clientConsumer.accept(id, Map.copyOf(snapshot));
    }

    private List<AssetSnapshotS2C.Chunk> load(ResourceManager manager) {
        List<Identifier> resources = ResourceCompat.list(manager, folder,
                location -> location.getPath().endsWith(extension));
        List<AssetSnapshotS2C.Asset> assets = new ArrayList<>(resources.size());
        long totalBytes = 0;
        long encodedBytes = 5;
        for (Identifier location : resources.stream().sorted().toList()) {
            if (assets.size() >= maxAssets) break;
            byte[] bytes = readLimited(manager, location, maxAssetBytes);
            if (bytes == null) continue;
            // Resource identifiers are ASCII. Reserve up to five bytes per VarInt length.
            long encodedAsset = (long) location.toString().length() + bytes.length + 10;
            if (totalBytes + bytes.length > maxTotalBytes
                    || encodedBytes + encodedAsset > AssetSnapshotS2C.MAX_ENCODED_ASSET_BYTES) break;
            assets.add(new AssetSnapshotS2C.Asset(location, bytes));
            totalBytes += bytes.length;
            encodedBytes += encodedAsset;
        }
        return new AssetSnapshotS2C(id, assets).chunks();
    }

    private static void send(ServerPlayer player, List<AssetSnapshotS2C.Chunk> snapshot) {
        for (AssetSnapshotS2C.Chunk chunk : snapshot) RosettaNetwork.sendToPlayer(chunk, player);
    }

    private static byte[] readLimited(ResourceManager manager, Identifier location, int maximum) {
        try (InputStream input = ResourceCompat.open(manager, location)) {
            byte[] bytes = input.readNBytes(maximum + 1);
            if (bytes.length <= maximum) return bytes;
            AssetChannels.LOGGER.warn("Skipping datapack asset {} because it exceeds {} bytes", location, maximum);
        } catch (IOException e) {
            AssetChannels.LOGGER.warn("Skipping unreadable datapack asset {}", location, e);
        }
        return null;
    }

    private static MinecraftServer server(ServerPlayer player) {
        //? if >=26.1 {
        return player.level().getServer();
        //?} else {
        /*return player.getServer();
        *///?}
    }

}
