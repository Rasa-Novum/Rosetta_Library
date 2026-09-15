# Networking

`rosetta-networking` requires core. See [setup](../setup.md).


Declare packets in common code. Rosetta handles registration and transport for each loader.

## Define a packet

A packet implements `RosettaPacket`. The packet may be a record and does not need to expose loader-specific payload types or codecs.

```java
package net.example.network;

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

Readers and writers use `FriendlyByteBuf` on every target.

## Register packets

Create one channel for the consuming mod and register each packet direction explicitly:

```java
package net.example.network;

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


## Send packets

Send a client-to-server packet from client code:

```java
RosettaNetwork.sendToServer(new RenameRequestC2S("New name"));
```

Send a server-to-client packet to a specific player:

```java
RosettaNetwork.sendToPlayer(new RenameResultS2C(true), serverPlayer);
```
