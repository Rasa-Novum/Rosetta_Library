package net.rasanovum.rosetta.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
//? if <1.20 {
/*import net.rasanovum.rosetta.client.gui.legacy.AbstractButton;
*///?} else {
import net.minecraft.client.gui.components.AbstractButton;
//?}
import java.util.function.BooleanSupplier;

/** Opt-in vanilla adapter with stable subclass hooks. */
public abstract class CompatAbstractButton extends AbstractButton {
    protected CompatAbstractButton(int x, int y, int width, int height, Component text) { super(x, y, width, height, text); }

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

    //? if >=26.1 {
    /*@Override
    public void onPress(net.minecraft.client.input.InputWithModifiers input) {
        onPress();
    }
    *///?}

    public abstract void onPress();

    //? if >=26.1 {
    /*@Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        onMouseActivate(() -> super.onClick(event, doubled));
    }
    *///?} else {
    @Override
    public void onClick(double x, double y) {
        onMouseActivate(() -> super.onClick(x, y));
    }
    //?}

    protected void onMouseActivate(Runnable next) {
        next.run();
    }

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

    //? if >=26.1 {
    /*@Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        return onMouseRelease(new MouseInput(event, false), () -> super.mouseReleased(event));
    }
    *///?} else {
    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return onMouseRelease(new MouseInput(x, y, button), () -> super.mouseReleased(x, y, button));
    }
    //?}

    protected boolean onMouseRelease(MouseInput input, BooleanSupplier next) {
        return next.getAsBoolean();
    }

}
