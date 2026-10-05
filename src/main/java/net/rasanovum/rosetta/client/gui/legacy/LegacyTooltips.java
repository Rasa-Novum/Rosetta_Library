package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.AbstractWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.WeakHashMap;
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
    private static final Map<AbstractWidget, Tooltip> attached = new WeakHashMap<>();
    private static Component tooltip;
    private static int x;
    private static int y;

    private LegacyTooltips() {}

    public static void setTooltip(AbstractWidget widget, Tooltip tooltip) {
        if (tooltip == null) attached.remove(widget);
        else attached.put(widget, tooltip);
    }

    public static void render(AbstractWidget widget, PoseStack pose, int mouseX, int mouseY, float delta) {
        widget.render(pose, mouseX, mouseY, delta);
        if (widget.visible && widget.isMouseOver(mouseX, mouseY)) {
            Tooltip attachedTooltip = attached.get(widget);
            if (attachedTooltip != null && !attachedTooltip.text().getString().isEmpty()) {
                schedule(attachedTooltip.text(), mouseX, mouseY);
            }
        }
    }

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
