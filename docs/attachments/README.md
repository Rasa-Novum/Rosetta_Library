# Attachments

`rosetta-attachments` requires core. See [setup](../setup.md).


Rosetta attachments provide persistent values on levels, entities, players, chunks, and block entities. Fabric and NeoForge use their native attachment facilities. On Forge 1.20.1, Rosetta stores the same codec-backed values as ordinary NBT through Forge's standard save/load hooks.

## Define an attachment value

```java
package net.example.data;

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

## Register an attachment

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

## Read and modify attachments

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
