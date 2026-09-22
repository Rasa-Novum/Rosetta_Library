package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
public class Button extends net.minecraft.client.gui.components.Button {
    protected static final Object DEFAULT_NARRATION = new Object();

    public Button(int x, int y, int width, int height, Component message, OnPress action, Object narration) {
        this(x, y, width, height, message, action);
    }

    public Button(int x, int y, int width, int height, Component message, OnPress action) {
        super(x, y, width, height, message, action);
    }

    public void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        super.renderButton(pose, mouseX, mouseY, partialTick);
    }

    public static Builder builder(Component message, OnPress action) {
        return new Builder(message, action);
    }

    public static class Builder {
        private final Component message;
        private final OnPress action;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private Tooltip tooltip;

        public Builder(Component message, OnPress action) {
            this.message = message;
            this.action = action;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Button build() {
            Button button = new Button(x, y, width, height, message, action, DEFAULT_NARRATION);
            button.setTooltip(tooltip);
            return button;
        }
    }

    private Tooltip tooltip;

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

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
