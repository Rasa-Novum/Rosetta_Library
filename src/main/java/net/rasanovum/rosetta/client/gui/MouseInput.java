package net.rasanovum.rosetta.client.gui;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.rasanovum.rosetta.util.ClientCompat;
//? if >=26.1 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}

/** A mouse event with stable button indices; forwarding retains the original native event. */
public final class MouseInput {
    private final double x;
    private final double y;
    private final int button;
    private final int modifiers;
    private final boolean doubled;
    //? if >=26.1 {
    /*private final MouseButtonEvent event;

    public MouseInput(MouseButtonEvent event, boolean doubled) {
        this.event = event;
        this.x = event.x();
        this.y = event.y();
        this.button = ClientCompat.mouseButtonIndex(event.button());
        this.modifiers = event.modifiers();
        this.doubled = doubled;
    }
    *///?} else {
    public MouseInput(double x, double y, int button) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.modifiers = (net.minecraft.client.gui.screens.Screen.hasShiftDown() ? 1 : 0)
                | (net.minecraft.client.gui.screens.Screen.hasControlDown() ? 2 : 0)
                | (net.minecraft.client.gui.screens.Screen.hasAltDown() ? 4 : 0);
        this.doubled = false;
    }
    //?}

    public double x() { return x; }
    public double y() { return y; }
    public int button() { return button; }
    public int modifiers() { return modifiers; }
    public boolean doubled() { return doubled; }

    public boolean click(GuiEventListener listener) {
        //? if >=26.1 {
        /*return listener.mouseClicked(event, doubled);
        *///?} else {
        return listener.mouseClicked(x, y, button);
        //?}
    }
}
