package net.rasanovum.rosetta.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Loader-neutral client connection and lifecycle hooks. */
public final class ClientHooks {
    private static final List<Callbacks> CALLBACKS = new CopyOnWriteArrayList<>();
    private static volatile boolean initialized;

    private ClientHooks() {}

    public static void register(Callbacks callbacks) {
        if (callbacks == null) throw new IllegalArgumentException("callbacks cannot be null");
        CALLBACKS.add(callbacks);
        if (initialized) callbacks.onClientInitialize();
    }

    public static void clientInitialized() {
        initialized = true;
        CALLBACKS.forEach(Callbacks::onClientInitialize);
    }
    public static void joined(Player player) {
        CALLBACKS.forEach(c -> c.onJoin(player));
    }

    public static void disconnected() {
        Minecraft.getInstance().execute(() -> CALLBACKS.forEach(Callbacks::onDisconnect));
    }
    public static void endClientTick() {
        CALLBACKS.forEach(Callbacks::onEndClientTick);
    }

    public static void clientStopping() {
        RenderSystem.assertOnRenderThread();
        CALLBACKS.forEach(Callbacks::onClientStopping);
    }

    public interface Callbacks {
        default void onClientInitialize() {}
        default void onJoin(Player player) {}
        default void onDisconnect() {}
        default void onEndClientTick() {}
        default void onClientStopping() {}
    }
}
