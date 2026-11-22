package net.rasanovum.rosetta.loaders.fabric;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.rasanovum.rosetta.util.RegistryCompat;
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
//?}
import net.minecraft.client.Minecraft;
import net.rasanovum.rosetta.event.ClientHooks;
import net.rasanovum.rosetta.event.ClientRenderHooks;
import net.rasanovum.rosetta.entrypoint.RosettaEntrypoints;
//? if <26.1
import net.rasanovum.rosetta.event.ClientShaderHooks;
//? if <26.1
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public final class RosettaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RosettaEntrypoints.initializeClients();
        ClientHooks.clientInitialized();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientHooks.joined(client.player));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientHooks.disconnected());
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientHooks.endClientTick());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ClientHooks.clientStopping());
        //? if <26.1 {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            try {
                ClientShaderHooks.registerShaders(context::register);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to register a Rosetta client shader", exception);
            }
        });
        //?}

        //? if >=26.1 {
        /*HudElementRegistry.addLast(RegistryCompat.getLocation("rosetta_library:callbacks"), (graphics, deltaTracker) ->
                ClientRenderHooks.renderHud(graphics, deltaTracker.getGameTimeDeltaPartialTick(false)));
        LevelRenderEvents.END_MAIN.register(context -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.level != null) {
                ClientRenderHooks.renderWorld(context.poseStack(), minecraft.level, minecraft.player,
                        minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), null);
            }
        });
        *///?} else {
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> {
            //? if <1.21 {
            /*float tickDelta = deltaTracker;
            *///?} else {
            float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
            //?}
            ClientRenderHooks.renderHud(graphics, tickDelta);
        });
        WorldRenderEvents.LAST.register(context -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.level != null) {
                //? if <1.21 {
                /*float tickDelta = context.tickDelta();
                *///?} else {
                float tickDelta = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
                //?}
                ClientRenderHooks.renderWorld(context.matrixStack(), minecraft.level, minecraft.player,
                        tickDelta, context.frustum());
            }
        });
        //?}
    }
}
//?}
