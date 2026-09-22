package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
public abstract class AbstractWidget extends net.minecraft.client.gui.components.AbstractWidget {
    protected AbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }
    public abstract void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick);

    private Tooltip tooltip;

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public boolean isHovered() { return isHovered; }
    public void setTooltip(Tooltip tooltip) { this.tooltip = tooltip; }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderWidget(pose, mouseX, mouseY, partialTick);
        if (tooltip != null && isHovered) {
            LegacyTooltips.schedule(tooltip.text(), mouseX, mouseY);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        updateWidgetNarration(output);
    }

    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
*///?}
