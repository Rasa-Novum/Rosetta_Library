package net.rasanovum.rosetta.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
//? if <1.19 {
/*import net.minecraft.network.chat.TranslatableComponent;
*///?} else {
import net.minecraft.network.chat.contents.TranslatableContents;
//?}
import org.jetbrains.annotations.Nullable;

public final class TextCompat {
    private TextCompat() {}

    /** Returns the translation key of the component itself, or null for other text. */
    public static @Nullable String translationKey(Component component) {
        //? if <1.19 {
        /*return component instanceof TranslatableComponent text ? text.getKey() : null;
        *///?} else {
        return component.getContents() instanceof TranslatableContents text ? text.getKey() : null;
        //?}
    }
    public static HoverEvent showText(Component text) {
        //? if >=26.1 {
        /*return new HoverEvent.ShowText(text);
        *///?} else {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        //?}
    }
}
