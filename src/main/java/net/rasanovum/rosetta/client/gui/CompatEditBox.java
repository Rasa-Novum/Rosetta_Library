package net.rasanovum.rosetta.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
//? if <1.20 {
/*import net.rasanovum.rosetta.client.gui.legacy.EditBox;
*///?} else {
import net.minecraft.client.gui.components.EditBox;
//?}

/** Opt-in vanilla adapter with stable subclass hooks. */
public abstract class CompatEditBox extends EditBox {
    protected CompatEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height, Component text) { super(font, x, y, width, height, text); }

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

}
