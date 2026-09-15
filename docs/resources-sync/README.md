# Resources Sync

`rosetta-resources-sync` sends server datapack files to clients as byte snapshots. It requires core, networking, and resources; see [setup](../setup.md).

## Asset channels

Register an `AssetChannel` from `net.rasanovum.runeweaver.rosetta` during initialization on both sides. Supply:

- A unique channel identifier.
- A resource folder and file extension.
- Maximum bytes per asset, total bytes, and asset count.
- A client callback receiving the channel identifier and a map of resource identifiers to bytes.

The channel loads its snapshot on server startup and datapack reload, broadcasts it to connected players, and sends it to players as they join. The callback receives a complete snapshot; replace the previous client data when applying it.

An oversized individual asset fails loading. The total-byte and asset-count limits stop collection, so choose limits large enough for the expected pack. The channel transports bytes; your callback handles decoding and application.

See [AssetChannel.register](../../artifacts/resources-sync/src/main/java/net/rasanovum/runeweaver/rosetta/AssetChannel.java) for the signature and [AssetChannels](../../artifacts/resources-sync/src/main/java/net/rasanovum/runeweaver/rosetta/AssetChannels.java) for lifecycle handling.
