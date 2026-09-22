package net.rasanovum.rosetta.loaders.forge;

//? if forge {
/*import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
//? if >=1.19
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.rasanovum.rosetta.event.ClientHooks;
import net.rasanovum.rosetta.event.ClientRenderHooks;
import net.rasanovum.rosetta.entrypoint.RosettaEntrypoints;

@Mod.EventBusSubscriber(modid = "rosetta_library", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeClientHooks {
    private ForgeClientHooks() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        RosettaEntrypoints.initializeClients();
        ClientHooks.clientInitialized();
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::playerLogin);
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::playerLogout);
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::clientTick);
        //? if >=1.19
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::gameStopping);
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::renderHud);
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHooks::renderWorld);
    }

    private static void playerLogin(ClientPlayerNetworkEvent.LoggingIn event) { ClientHooks.joined(event.getPlayer()); }
    private static void playerLogout(ClientPlayerNetworkEvent.LoggingOut event) { ClientHooks.disconnected(); }
    private static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) ClientHooks.endClientTick();
    }
    //? if >=1.19
    private static void gameStopping(GameShuttingDownEvent event) { ClientHooks.clientStopping(); }
    private static void renderHud(RenderGuiEvent.Post event) {
        //? if <1.19
        if (event.getType() != net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL) return;
        //? if <1.19 {
        ClientRenderHooks.renderHud(event.getMatrixStack(), event.getPartialTicks());
        //?} else {
        ClientRenderHooks.renderHud(event.getGuiGraphics(), Minecraft.getInstance().getFrameTime());
        //?}
    }
    private static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
                ClientRenderHooks.renderWorld(event.getPoseStack(), minecraft.level, minecraft.player,
                        minecraft.getFrameTime(), event.getFrustum());
            }
        }
    }
}
*///?}
