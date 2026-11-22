package net.rasanovum.rosetta.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Loader-neutral server lifecycle hooks. Register callbacks during mod initialization. */
public final class ServerHooks {
    private static final List<Callbacks> CALLBACKS = new CopyOnWriteArrayList<>();

    private static final List<Callbacks> CHUNK_SENT_CALLBACKS = new CopyOnWriteArrayList<>();

    private ServerHooks() {}

    public static void register(Callbacks callbacks) {
        if (callbacks == null) throw new IllegalArgumentException("callbacks cannot be null");
        CALLBACKS.add(callbacks);
        try {
            if (callbacks.getClass().getMethod("onChunkSent", ServerPlayer.class, ServerLevel.class, LevelChunk.class)
                    .getDeclaringClass() != Callbacks.class) {
                CHUNK_SENT_CALLBACKS.add(callbacks);
            }
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static void playerJoined(ServerPlayer player) { CALLBACKS.forEach(c -> c.onPlayerJoin(player)); }
    public static void playerLeft(ServerPlayer player) { CALLBACKS.forEach(c -> c.onPlayerLeave(player)); }
    public static void serverLevelTick(ServerLevel level) { CALLBACKS.forEach(c -> c.onServerLevelTick(level)); }
    public static void serverStarting(MinecraftServer server) { CALLBACKS.forEach(c -> c.onServerStarting(server)); }
    public static void serverStarted(MinecraftServer server) { CALLBACKS.forEach(c -> c.onServerStarted(server)); }
    public static void serverStopping(MinecraftServer server) { CALLBACKS.forEach(c -> c.onServerStopping(server)); }
    public static void dataPackReloaded(MinecraftServer server) { CALLBACKS.forEach(c -> c.onDataPackReload(server)); }
    public static void playerChangedDimension(ServerPlayer player) { CALLBACKS.forEach(c -> c.onPlayerChangedDimension(player)); }
    public static boolean hasChunkSentListeners() { return !CHUNK_SENT_CALLBACKS.isEmpty(); }
    public static void chunkSent(ServerPlayer player, ServerLevel level, LevelChunk chunk) {
        CHUNK_SENT_CALLBACKS.forEach(c -> c.onChunkSent(player, level, chunk));
    }
    public static void chunkUnwatched(ServerPlayer player, ServerLevel level, ChunkPos pos) { CALLBACKS.forEach(c -> c.onChunkUnwatched(player, level, pos)); }
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) { CALLBACKS.forEach(c -> c.registerCommands(dispatcher)); }

    public static boolean beforeBlockBreak(LevelAccessor level, BlockPos pos, ServerPlayer player) {
        boolean allowed = true;
        for (Callbacks callbacks : CALLBACKS) allowed &= callbacks.beforeBlockBreak(level, pos, player);
        return allowed;
    }

    public interface Callbacks {
        default void onPlayerJoin(ServerPlayer player) {}
        default void onPlayerLeave(ServerPlayer player) {}
        default void onServerLevelTick(ServerLevel level) {}
        default void onServerStarting(MinecraftServer server) {}
        default void onServerStarted(MinecraftServer server) {}
        default void onServerStopping(MinecraftServer server) {}
        default void onDataPackReload(MinecraftServer server) {}
        default void onPlayerChangedDimension(ServerPlayer player) {}
        default void onChunkSent(ServerPlayer player, ServerLevel level, LevelChunk chunk) {}
        default void onChunkUnwatched(ServerPlayer player, ServerLevel level, ChunkPos pos) {}
        default boolean beforeBlockBreak(LevelAccessor level, BlockPos pos, ServerPlayer player) { return true; }
        default void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {}
    }
}
