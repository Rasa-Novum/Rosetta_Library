package net.rasanovum.rosetta.client.gui;

import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;

/** Opt-in vanilla adapter with stable subclass hooks. */
public abstract class CompatScreen extends net.minecraft.client.gui.screens.Screen {
    protected CompatScreen(Component title) { super(title); }

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
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        return onMouseDrag(new MouseInput(event, false), dragX, dragY, () -> super.mouseDragged(event, dragX, dragY));
    }
    *///?} else {
    @Override
    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        return onMouseDrag(new MouseInput(x, y, button), dragX, dragY, () -> super.mouseDragged(x, y, button, dragX, dragY));
    }
    //?}

    protected boolean onMouseDrag(MouseInput input, double dragX, double dragY, BooleanSupplier next) {
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
