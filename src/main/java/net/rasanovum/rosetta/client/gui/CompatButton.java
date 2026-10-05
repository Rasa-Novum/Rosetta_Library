package net.rasanovum.rosetta.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
//? if <1.20 {
/*import net.rasanovum.rosetta.client.gui.legacy.Button;
*///?} else {
import net.minecraft.client.gui.components.Button;
//?}

/** Opt-in vanilla adapter with stable subclass hooks. */
public abstract class CompatButton extends Button {
    protected CompatButton(int x, int y, int width, int height, Component text, OnPress press) {
        super(x, y, width, height, text, press, DEFAULT_NARRATION);
    }

    //? if >=26.1 {
    /*@Override
    public void extractContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
