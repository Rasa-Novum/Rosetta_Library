# Using Rosetta Library

## Networking

Rosetta packets use the same declaration on every supported target. Rosetta translates that declaration to legacy Fabric networking, modern Fabric payloads, Forge `SimpleChannel`, or NeoForge payload registration.

### Define a packet

A packet implements `RosettaPacket`. The packet may be a record and does not need to expose loader-specific payload types or codecs.

```java
package com.example.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.rasanovum.rosetta.network.RosettaPacket;

public record RenameRequestC2S(String name) implements RosettaPacket {
    public RenameRequestC2S(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(name);
    }

    public static void handle(RenameRequestC2S packet, Level level, Player player) {
        // This packet is serverbound, so validate the request and modify server state here.
    }
}
```

Packet readers and writers use `FriendlyByteBuf` on every target. Rosetta creates the required modern `CustomPacketPayload.Type` and stream codec internally on Minecraft 1.21 and newer.

### Register packets

Create one channel for the consuming mod and register each packet direction explicitly:

```java
package com.example.network;

import net.rasanovum.rosetta.network.RosettaNetwork;

public final class ExamplePackets {
    private static final RosettaNetwork.Channel CHANNEL =
            RosettaNetwork.channel("example_mod");

    private ExamplePackets() {}

    public static void register() {
        CHANNEL.serverbound(
                "rename_request",
                RenameRequestC2S.class,
                RenameRequestC2S::write,
                RenameRequestC2S::new,
                RenameRequestC2S::handle
        );

        CHANNEL.clientbound(
                "rename_result",
                RenameResultS2C.class,
                RenameResultS2C::write,
                RenameResultS2C::new,
                RenameResultS2C::handle
        );
    }
}
```

Call `ExamplePackets.register()` during the consuming mod's initialization.


### Send packets

Send a client-to-server packet from client code:

```java
RosettaNetwork.sendToServer(new RenameRequestC2S("New name"));
```

Send a server-to-client packet to a specific player:

```java
RosettaNetwork.sendToPlayer(new RenameResultS2C(true), serverPlayer);
```

## Persistent data attachments

Rosetta attachments provide persistent values on levels, entities, players, chunks, and block entities. Fabric and NeoForge use their native attachment facilities. On Forge 1.20.1, Rosetta stores the same codec-backed values as ordinary NBT through Forge's standard save/load hooks.

### Define an attachment value

```java
package com.example.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VisitData(int visits, String lastDestination) {
    public static final Codec<VisitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("visits").forGetter(VisitData::visits),
            Codec.STRING.fieldOf("last_destination").forGetter(VisitData::lastDestination)
    ).apply(instance, VisitData::new));

    public static VisitData empty() {
        return new VisitData(0, "");
    }
}
```

### Register an attachment

```java
import net.rasanovum.rosetta.attachment.LevelAttachmentKey;
import net.rasanovum.rosetta.attachment.RosettaAttachments;

public static final LevelAttachmentKey<VisitData> VISITS =
        RosettaAttachments.level("example_mod").persistent(
                "visits",
                VisitData::empty,
                VisitData.CODEC
        );
```

Available holder builders are:

```java
RosettaAttachments.level("example_mod");
RosettaAttachments.entity("example_mod");
RosettaAttachments.player("example_mod");
RosettaAttachments.chunk("example_mod");
RosettaAttachments.blockEntity("example_mod");
```

Chunk attachments use `ChunkAccess`, so the same key works with proto-chunks and fully generated chunks.

Player attachments copy their value when a player is recreated after death by default. Disable that behavior when declaring the key:

```java
PlayerAttachmentKey<VisitData> TEMPORARY =
        RosettaAttachments.player("example_mod").persistent(
                "temporary_visits",
                false,
                VisitData::empty,
                VisitData.CODEC
        );
```

### Read and modify attachments

`find` returns an empty `Optional` when no value has been attached:

```java
Optional<VisitData> existing = VISITS.find(serverLevel);
```

`getOrCreate` installs the declared default when no value exists:

```java
VisitData current = VISITS.getOrCreate(serverLevel);
```

Prefer immutable attachment values and replace them through `set`:

```java
VisitData current = VISITS.getOrCreate(serverLevel);
VISITS.set(serverLevel, new VisitData(current.visits() + 1, "spawn"));
```

Remove a value and restore the absent state with:

```java
VISITS.remove(serverLevel);
```

Mutable values are supported, but the loader cannot observe an in-place change. Call `markDirty` after mutation so the value is persisted:

```java
MutableGraph graph = GRAPH.getOrCreate(serverLevel);
graph.addNode(node);
GRAPH.markDirty(serverLevel);
```

## Data migration

Rosetta contains a migration seam that runs before `getOrCreate` installs a default attachment value. The Data Anchor importer is a work in progress.

## Other compatibility helpers

Rosetta also contains compatibility facades for NBT access, registries, worlds, biomes, attributes, entities, GUI rendering, client utilities, gamerules, and loader/platform queries. These helpers centralize ordinary API replacements, while Stonecutter remains appropriate for method signatures, overrides, mixin targets, and other source-shape changes that cannot be hidden behind a method call.

Consult the classes under `net.rasanovum.rosetta.nbt`, `net.rasanovum.rosetta.util`, `net.rasanovum.rosetta.client`, and `net.rasanovum.rosetta.loaders` for the currently available facade methods.


## Client lifecycle and rendering compatibility

`ClientHooks.onDisconnect` callbacks run on the Minecraft client thread, even when a loader reports the disconnect from a networking thread. `onClientStopping` runs synchronously on the render thread before graphics teardown; do not defer GPU cleanup from that callback. Callbacks that release resources should tolerate a disconnect followed by shutdown.

`ClientCompat.screen`, `setScreen`, `mainCamera`, and `isHudHidden` hide the 26.2 client GUI and camera API changes. These helpers do not schedule calls: screen changes still belong on the client thread.

`GeometryBuffers` provides `getBuffer`, `endBatch`, `endFrame`, and `close` for immediate world overlays across all supported versions. Keep an adapter in the consumer, flush each completed render type, call `endFrame` after the overlay pass, and call `close` on disconnect and shutdown. Operations require the render thread. Storage is allocated lazily and can be recreated after closing on reconnect. Before 26.2 it borrows vanilla's buffer source; on 26.2 it owns staged GPU storage. Do not close or flush it from a networking callback outside Rosetta's lifecycle dispatch.

For 26.1 and newer, `GpuCompat` adapts buffer writes, render passes that preserve existing attachments, vertex slot zero, non-instanced indexed draws, uniform-buffer/sampler bindings, and quad pipeline layout. The consumer owns textures, buffers, pipeline policy, and render-pass lifetime. A `writeBuffer` callback must not retain its mapped byte buffer. These helpers neither submit the frame nor free caller-owned resources.
