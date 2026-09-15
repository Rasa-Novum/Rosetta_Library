package net.rasanovum.rosetta.network;

//? if >=1.21 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//?}

/** Marker interface for a packet registered through {@link RosettaNetwork}. */
public interface RosettaPacket
        //? if >=1.21
        extends CustomPacketPayload
{
    //? if >=1.21 {
    Map<Class<?>, Type<?>> TYPE_CACHE = new ConcurrentHashMap<>();

    @Override
    default Type<? extends CustomPacketPayload> type() {
        Type<?> type = TYPE_CACHE.get(getClass());
        if (type == null) {
            throw new IllegalStateException("Packet type was not registered: " + getClass().getName());
        }
        return type;
    }

    static <T extends RosettaPacket> void registerType(Class<T> clazz, Type<T> type) {
        Type<?> previous = TYPE_CACHE.putIfAbsent(clazz, type);
        if (previous != null && !previous.equals(type)) {
            throw new IllegalArgumentException("Packet class was registered twice: " + clazz.getName());
        }
    }
    //?}
}
