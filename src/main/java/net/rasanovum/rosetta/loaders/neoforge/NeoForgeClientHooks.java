package net.rasanovum.rosetta.loaders.neoforge;

//? if neoforge {
/*import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.rasanovum.rosetta.event.ClientHooks;
import net.rasanovum.rosetta.event.ClientRenderHooks;
import net.rasanovum.rosetta.entrypoint.RosettaEntrypoints;

@EventBusSubscriber(modid = "rosetta_library", value = Dist.CLIENT)
public final class NeoForgeClientHooks {
    private NeoForgeClientHooks() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        RosettaEntrypoints.initializeClients();
        ClientHooks.clientInitialized();
        NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::playerLogin);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::playerLogout);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::clientTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::gameStopping);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::renderHud);
        // 26.3 translucent stages run inside an active render pass; overlays upload at AfterLevel.
        //? if >=26.3
        /^NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterLevel renderEvent) -> renderWorldModern(renderEvent));^/
        //? if >=26.1 && <26.3
        /^NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterTranslucentBlocks renderEvent) -> renderWorldModern(renderEvent));^/
        //? if <26.1
        NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::renderWorld);
    }

    private static void playerLogin(ClientPlayerNetworkEvent.LoggingIn event) { ClientHooks.joined(event.getPlayer()); }
    private static void playerLogout(ClientPlayerNetworkEvent.LoggingOut event) { ClientHooks.disconnected(); }
    private static void clientTick(ClientTickEvent.Post event) { ClientHooks.endClientTick(); }
    private static void gameStopping(GameShuttingDownEvent event) { ClientHooks.clientStopping(); }
    private static void renderHud(RenderGuiEvent.Post event) {
        ClientRenderHooks.renderHud(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaTicks());
    }

    private static void renderWorld(RenderLevelStageEvent event) {
        //? if <26.1 {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
                ClientRenderHooks.renderWorld(event.getPoseStack(), minecraft.level, minecraft.player,
                        minecraft.getTimer().getGameTimeDeltaTicks(), event.getFrustum());
            }
        }
        //?}
    }

    //? if >=26.1
    /^private static void renderWorldModern(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            //? if >=26.3 {
            // LevelRenderer has already restored the model-view stack at AfterLevel.
            var modelView = com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            modelView.mul(event.getModelViewMatrix());
            try {
            //?}
                ClientRenderHooks.renderWorld(event.getPoseStack(), minecraft.level, minecraft.player,
                        minecraft.getDeltaTracker().getGameTimeDeltaTicks(), null);
            //? if >=26.3 {
            } finally {
                modelView.popMatrix();
            }
            //?}
        }
    }^/
}
*///?}
