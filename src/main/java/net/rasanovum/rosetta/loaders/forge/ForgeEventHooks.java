package net.rasanovum.rosetta.loaders.forge;

//? if forge {
/*import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.rasanovum.rosetta.event.ServerHooks;

@Mod.EventBusSubscriber(modid = "rosetta_library")
public final class ForgeEventHooks {
    private static MinecraftServer server;

    private ForgeEventHooks() {}

    @SubscribeEvent
    public static void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ServerHooks.playerJoined(player);
    }

    @SubscribeEvent
    public static void playerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ServerHooks.playerLeft(player);
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) ServerHooks.serverLevelTick(level);
    }

    @SubscribeEvent
    public static void serverStarting(ServerStartingEvent event) {
        server = event.getServer();
        ServerHooks.serverStarting(server);
    }

    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event) {
        ServerHooks.serverStopping(event.getServer());
        server = null;
    }

    @SubscribeEvent
    public static void dataPackReloaded(TagsUpdatedEvent event) {
        if (server != null && event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            MinecraftServer currentServer = server;
            currentServer.execute(() -> {
                if (server == currentServer) {
                    ServerHooks.dataPackReloaded(currentServer);
                }
            });
        }
    }

    @SubscribeEvent
    public static void dimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ServerHooks.playerChangedDimension(player);
    }

    @SubscribeEvent
    public static void chunkWatch(ChunkWatchEvent.Watch event) {
        ServerHooks.chunkSent(event.getPlayer(), event.getLevel(), event.getChunk());
    }

    @SubscribeEvent
    public static void chunkUnwatch(ChunkWatchEvent.UnWatch event) {
        ServerHooks.chunkUnwatched(event.getPlayer(), event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player &&
                !ServerHooks.beforeBlockBreak(event.getLevel(), event.getPos(), player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ServerHooks.registerCommands(event.getDispatcher());
    }
}
*///?}
