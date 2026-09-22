package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
//? if forge {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?}
import net.rasanovum.rosetta.client.gui.GuiCompat;

//? if forge
@Mod.EventBusSubscriber(modid = "rosetta_library", value = Dist.CLIENT)
public final class LegacyTooltips {
    private static Component tooltip;
    private static int x;
    private static int y;

    private LegacyTooltips() {}

    public static void schedule(Component text, int mouseX, int mouseY) {
        tooltip = text;
        x = mouseX;
        y = mouseY;
    }

    //? if forge {
    @SubscribeEvent
    public static void beforeScreen(ScreenEvent.Render.Pre event) {
        tooltip = null;
    }

    @SubscribeEvent
    public static void afterScreen(ScreenEvent.Render.Post event) {
        if (tooltip != null) {
            GuiCompat.renderTooltip(event.getPoseStack(), Minecraft.getInstance().font, tooltip, x, y);
            tooltip = null;
        }
    }
    //?}
}
*///?}
