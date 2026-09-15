package net.rasanovum.rosetta.loaders.fabric;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
*///?} else {
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
//?}
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.rasanovum.rosetta.event.ServerHooks;

public final class RosettaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerHooks.playerJoined(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ServerHooks.playerLeft(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getAllLevels().forEach(ServerHooks::serverLevelTick));
        ServerLifecycleEvents.SERVER_STARTING.register(ServerHooks::serverStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(ServerHooks::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerHooks::serverStopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> ServerHooks.dataPackReloaded(server));
        //? if >=26.1 {
        /*ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, level) ->
                ServerHooks.playerChangedDimension(player));
        *///?} else {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, level) ->
                ServerHooks.playerChangedDimension(player));
        //?}
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !(player instanceof ServerPlayer serverPlayer) || ServerHooks.beforeBlockBreak(world, pos, serverPlayer));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ServerHooks.registerCommands(dispatcher));
    }
}
//?}
