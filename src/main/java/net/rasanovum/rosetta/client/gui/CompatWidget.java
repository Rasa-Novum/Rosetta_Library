package net.rasanovum.rosetta.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
//? if <1.20 {
/*import net.rasanovum.rosetta.client.gui.legacy.AbstractWidget;
*///?} else {
import net.minecraft.client.gui.components.AbstractWidget;
//?}
import java.util.function.BooleanSupplier;

/** Opt-in vanilla adapter with stable subclass hooks. */
public abstract class CompatWidget extends AbstractWidget {
    protected CompatWidget(int x, int y, int width, int height, Component text) { super(x, y, width, height, text); }

    //? if >=26.1 {
    /*@Override
    public void extractWidgetRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawWidget(graphics, mouseX, mouseY, partialTick);
    }
    *///?} else {
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawWidget(graphics, mouseX, mouseY, partialTick);
    }
    //?}

    protected abstract void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    //? if >=26.1 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        return onMouseClick(new MouseInput(event, doubled), () -> super.mouseClicked(event, doubled));
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return onMouseClick(new MouseInput(x, y, button), () -> super.mouseClicked(x, y, button));
    }
    //?}

    protected boolean onMouseClick(MouseInput input, BooleanSupplier next) {
        return next.getAsBoolean();
    }

    //? if <1.20.2 {
    /*@Override
    public boolean mouseScrolled(double x, double y, double scrollY) {
        return onMouseScroll(x, y, 0, scrollY, () -> super.mouseScrolled(x, y, scrollY));
    }
    *///?} else {
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return onMouseScroll(x, y, scrollX, scrollY, () -> super.mouseScrolled(x, y, scrollX, scrollY));
    }
    //?}

    protected boolean onMouseScroll(double x, double y, double scrollX, double scrollY, BooleanSupplier next) {
        return next.getAsBoolean();
    }
}
