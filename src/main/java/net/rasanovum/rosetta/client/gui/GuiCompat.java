package net.rasanovum.rosetta.client.gui;
import net.minecraft.client.Minecraft;
//? if <1.20
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
//? if <1.19.3 {
/*import com.mojang.math.Vector3f;
*///?} else {
import com.mojang.math.Axis;
//?}

import java.util.List;

/** GUI drawing helpers. */
public class GuiCompat {

    private GuiCompat() {}

    //? if <1.20 {
    /*public static void confirmLink(String url) {
        Minecraft minecraft = Minecraft.getInstance();
        var parent = minecraft.screen;
        minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmLinkScreen(confirmed -> {
            if (confirmed) net.minecraft.Util.getPlatform().openUri(url);
            minecraft.setScreen(parent);
        }, url, true));
    }

    public static void renderItem(GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
    }
    *///?}

    /** Moves a vanilla screen control without requiring a Rosetta widget subclass. */
    public static void moveWidget(net.minecraft.client.gui.screens.Screen screen, Component message, int y) {
        for (var child : screen.children()) {
            if (child instanceof AbstractWidget widget && widget.getMessage().equals(message)) {
                //? if <1.20 {
                /*widget.y = y;
                *///?} else {
                widget.setY(y);
                //?}
            }
        }
    }

    //? if <1.20 {
    /*private static final java.util.Deque<int[]> SCISSORS = new java.util.ArrayDeque<>();

    private static class LegacyGradient extends net.minecraft.client.gui.GuiComponent {
        static void draw(GuiGraphics pose, int x1, int y1, int x2, int y2, int top, int bottom) {
            new LegacyGradient().fillGradient(pose, x1, y1, x2, y2, top, bottom);
        }
    }
    *///?}

    public static void fill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        //? if <1.20 {
        /*net.minecraft.client.gui.GuiComponent.fill(graphics, x1, y1, x2, y2, color);
        *///?} else {
        graphics.fill(x1, y1, x2, y2, color);
        //?}
    }

    public static void fillGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int top, int bottom) {
        //? if <1.20 {
        /*LegacyGradient.draw(graphics, x1, y1, x2, y2, top, bottom);
        *///?} else {
        graphics.fillGradient(x1, y1, x2, y2, top, bottom);
        //?}
    }

    public static void flush(GuiGraphics graphics) {
        //? if <1.20 {
        /*Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        *///?} else if <26.1 {
        graphics.flush();
        //?}
    }

    public static void enableScissor(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        //? if <1.20 {
        /*if (!SCISSORS.isEmpty()) {
            int[] parent = SCISSORS.peek();
            x1 = Math.max(x1, parent[0]);
            y1 = Math.max(y1, parent[1]);
            x2 = Math.min(x2, parent[2]);
            y2 = Math.min(y2, parent[3]);
        }
        SCISSORS.push(new int[] {x1, y1, x2, y2});
        applyLegacyScissor();
        *///?} else {
        graphics.enableScissor(x1, y1, x2, y2);
        //?}
    }

    public static void disableScissor(GuiGraphics graphics) {
        //? if <1.20 {
        /*if (!SCISSORS.isEmpty()) {
            SCISSORS.pop();
        }
        applyLegacyScissor();
        *///?} else {
        graphics.disableScissor();
        //?}
    }

    //? if <1.20 {
    /*private static void applyLegacyScissor() {
        if (SCISSORS.isEmpty()) {
            RenderSystem.disableScissor();
            return;
        }
        int[] bounds = SCISSORS.peek();
        var window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        RenderSystem.enableScissor((int) (bounds[0] * scale), (int) (window.getHeight() - bounds[3] * scale),
                Math.max(0, (int) ((bounds[2] - bounds[0]) * scale)),
                Math.max(0, (int) ((bounds[3] - bounds[1]) * scale)));
    }
    *///?}

    public static void drawString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean shadow) {
        //? if >=26.1 {
        /*guiGraphics.text(font, text, x, y, opaque(color), shadow);
        *///?} else {
        //? if <1.20 {
        /*if (shadow) {
            font.drawShadow(guiGraphics, text, x, y, color);
        } else {
            font.draw(guiGraphics, text, x, y, color);
        }
        *///?} else {
        guiGraphics.drawString(font, text, x, y, color, shadow);
        //?}
        //?}
    }

    public static void drawString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, boolean shadow) {
        //? if >=26.1 {
        /*guiGraphics.text(font, text, x, y, opaque(color), shadow);
        *///?} else {
        //? if <1.20 {
        /*if (shadow) {
            font.drawShadow(guiGraphics, text, x, y, color);
        } else {
            font.draw(guiGraphics, text, x, y, color);
        }
        *///?} else {
        guiGraphics.drawString(font, text, x, y, color, shadow);
        //?}
        //?}
    }

    public static void drawWidgetString(GuiGraphics guiGraphics, AbstractWidget widget, Font font, Component text, int x, int y, int color, boolean shadow) {
        //? if >=26.1 {
        /*guiGraphics.text(font, text.copy().withStyle(style -> style.withColor(color & 0xFFFFFF)), x, y, opaque(color), shadow);
        *///?} else {
        //? if <1.20 {
        /*if (shadow) {
            font.drawShadow(guiGraphics, text, x, y, color);
        } else {
            font.draw(guiGraphics, text, x, y, color);
        }
        *///?} else {
        guiGraphics.drawString(font, text, x, y, color, shadow);
        //?}
        //?}
    }

    public static void drawWidgetString(GuiGraphics guiGraphics, AbstractWidget widget, Font font, String text, int x, int y, int color, boolean shadow) {
        //? if >=26.1 {
        /*guiGraphics.text(font, Component.literal(text).withStyle(style -> style.withColor(color & 0xFFFFFF)), x, y, opaque(color), shadow);
        *///?} else {
        //? if <1.20 {
        /*if (shadow) {
            font.drawShadow(guiGraphics, text, x, y, color);
        } else {
            font.draw(guiGraphics, text, x, y, color);
        }
        *///?} else {
        guiGraphics.drawString(font, text, x, y, color, shadow);
        //?}
        //?}
    }

    public static void drawString(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        //? if >=26.1 {
        /*guiGraphics.text(font, text, x, y, opaque(color), shadow);
        *///?} else {
        //? if <1.20 {
        /*if (shadow) {
            font.drawShadow(guiGraphics, text, x, y, color);
        } else {
            font.draw(guiGraphics, text, x, y, color);
        }
        *///?} else {
        guiGraphics.drawString(font, text, x, y, color, shadow);
        //?}
        //?}
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color) {
        //? if >=26.1 {
        /*guiGraphics.text(font, text, x - font.width(text) / 2, y, opaque(color), false);
        *///?} else {
        //? if <1.20 {
        /*net.minecraft.client.gui.GuiComponent.drawCenteredString(guiGraphics, font, text, x, y, color);
        *///?} else {
        guiGraphics.drawCenteredString(font, text, x, y, color);
        //?}
        //?}
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        //? if >=26.1 {
        /*guiGraphics.text(font, text, x - font.width(text) / 2, y, opaque(color), false);
        *///?} else {
        //? if <1.20 {
        /*net.minecraft.client.gui.GuiComponent.drawCenteredString(guiGraphics, font, text, x, y, color);
        *///?} else {
        guiGraphics.drawCenteredString(font, text, x, y, color);
        //?}
        //?}
    }

    public static void hLine(GuiGraphics guiGraphics, int x1, int x2, int y, int color) {
        //? if >=26.1 {
        /*guiGraphics.horizontalLine(x1, x2, y, color);
        *///?} else {
        //? if <1.20 {
        /*net.minecraft.client.gui.GuiComponent.fill(guiGraphics, Math.min(x1, x2), y, Math.max(x1, x2) + 1, y + 1, color);
        *///?} else {
        guiGraphics.hLine(x1, x2, y, color);
        //?}
        //?}
    }

    public static void vLine(GuiGraphics guiGraphics, int x, int y1, int y2, int color) {
        //? if >=26.1 {
        /*guiGraphics.verticalLine(x, y1, y2, color);
        *///?} else {
        //? if <1.20 {
        /*net.minecraft.client.gui.GuiComponent.fill(guiGraphics, x, Math.min(y1, y2) + 1, x + 1, Math.max(y1, y2), color);
        *///?} else {
        guiGraphics.vLine(x, y1, y2, color);
        //?}
        //?}
    }

    public static void renderOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        //? if >=26.1 {
        /*guiGraphics.outline(x, y, width, height, color);
        *///?} else {
        //? if <1.20 {
        /*hLine(guiGraphics, x, x + width - 1, y, color);
        hLine(guiGraphics, x, x + width - 1, y + height - 1, color);
        vLine(guiGraphics, x, y, y + height - 1, color);
        vLine(guiGraphics, x + width - 1, y, y + height - 1, color);
        *///?} else {
        guiGraphics.renderOutline(x, y, width, height, color);
        //?}
        //?}
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int texWidth, int texHeight) {
        //? if >=26.1 {
        /*guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texWidth, texHeight);
        *///?} else {
        //? if <1.20 {
        /*com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);
        net.minecraft.client.gui.GuiComponent.blit(guiGraphics, x, y, (float) u, (float) v, width, height, texWidth, texHeight);
        *///?} else {
        guiGraphics.blit(texture, x, y, u, v, width, height, texWidth, texHeight);
        //?}
        //?}
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int texWidth, int texHeight, int color) {
        //? if >=26.1 {
        /*guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texWidth, texHeight, color);
        *///?} else {
        applyShaderColor(color);
        //? if <1.20 {
        /*com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);
        net.minecraft.client.gui.GuiComponent.blit(guiGraphics, x, y, (float) u, (float) v, width, height, texWidth, texHeight);
        *///?} else {
        guiGraphics.blit(texture, x, y, u, v, width, height, texWidth, texHeight);
        //?}
        resetShaderColor();
        //?}
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight) {
        //? if >=26.1 {
        /*guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, uWidth, vHeight, texWidth, texHeight);
        *///?} else {
        //? if <1.20 {
        /*com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);
        net.minecraft.client.gui.GuiComponent.blit(guiGraphics, x, y, width, height, (float) u, (float) v, uWidth, vHeight, texWidth, texHeight);
        *///?} else {
        guiGraphics.blit(texture, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight);
        //?}
        //?}
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight, int color) {
        //? if >=26.1 {
        /*guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, uWidth, vHeight, texWidth, texHeight, color);
        *///?} else {
        applyShaderColor(color);
        //? if <1.20 {
        /*com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);
        net.minecraft.client.gui.GuiComponent.blit(guiGraphics, x, y, width, height, (float) u, (float) v, uWidth, vHeight, texWidth, texHeight);
        *///?} else {
        guiGraphics.blit(texture, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight);
        //?}
        resetShaderColor();
        //?}
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Font font, Component tooltip, int x, int y) {
        //? if >=26.1 {
        /*guiGraphics.setTooltipForNextFrame(font, tooltip, x, y);
        *///?} else {
        //? if <1.20 {
        /*legacyTooltipScreen().renderTooltip(guiGraphics, tooltip, x, y);
        *///?} else {
        guiGraphics.renderTooltip(font, tooltip, x, y);
        //?}
        //?}
    }

    public static void renderComponentTooltip(GuiGraphics guiGraphics, Font font, List<Component> tooltip, int x, int y) {
        //? if >=26.1 {
        /*guiGraphics.setComponentTooltipForNextFrame(font, tooltip, x, y);
        *///?} else {
        //? if <1.20 {
        /*legacyTooltipScreen().renderComponentTooltip(guiGraphics, tooltip, x, y);
        *///?} else {
        guiGraphics.renderComponentTooltip(font, tooltip, x, y);
        //?}
        //?}
    }

    public static void renderComponentHoverEffect(GuiGraphics guiGraphics, Font font, Style style, int x, int y) {
        if (style == null || style.getHoverEvent() == null) return;

        //? if >=26.1 {
        /*if (style.getHoverEvent() instanceof HoverEvent.ShowText showText) {
            guiGraphics.setTooltipForNextFrame(font, showText.value(), x, y);
        }
        *///?} else {
        //? if <1.20 {
        /*legacyTooltipScreen().hover(guiGraphics, style, x, y);
        *///?} else {
        guiGraphics.renderComponentHoverEffect(font, style, x, y);
        //?}
        //?}
    }

    public static Style firstHoverStyle(Component component) {
        for (Component sibling : component.toFlatList(net.minecraft.network.chat.Style.EMPTY)) {
            Style style = sibling.getStyle();
            if (style.getHoverEvent() != null) return style;
        }
        return null;
    }

    public static void pushPose(GuiGraphics guiGraphics) {
        //? if >=26.1 {
        /*guiGraphics.pose().pushMatrix();
        *///?} else {
        //? if <1.20 {
        /*guiGraphics.pushPose();
        *///?} else {
        guiGraphics.pose().pushPose();
        //?}
        //?}
    }

    public static void popPose(GuiGraphics guiGraphics) {
        //? if >=26.1 {
        /*guiGraphics.pose().popMatrix();
        *///?} else {
        //? if <1.20 {
        /*guiGraphics.popPose();
        *///?} else {
        guiGraphics.pose().popPose();
        //?}
        //?}
    }

    public static void translate(GuiGraphics guiGraphics, float x, float y) {
        //? if >=26.1 {
        /*guiGraphics.pose().translate(x, y);
        *///?} else {
        //? if <1.20 {
        /*guiGraphics.translate(x, y, 0);
        *///?} else {
        guiGraphics.pose().translate(x, y, 0);
        //?}
        //?}
    }

    public static void scale(GuiGraphics guiGraphics, float sx, float sy) {
        //? if >=26.1 {
        /*guiGraphics.pose().scale(sx, sy);
        *///?} else {
        //? if <1.20 {
        /*guiGraphics.scale(sx, sy, 1.0f);
        *///?} else {
        guiGraphics.pose().scale(sx, sy, 1.0f);
        //?}
        //?}
    }

    public static void rotateDegrees(GuiGraphics guiGraphics, float degrees) {
        //? if >=26.1 {
        /*guiGraphics.pose().rotate((float) Math.toRadians(degrees));
        *///?} else {
        //? if <1.20 {
        /*guiGraphics.mulPose(Vector3f.ZP.rotationDegrees(degrees));
        *///?} else {
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
        //?}
        //?}
    }

    public static void nextStratum(GuiGraphics guiGraphics) {
        //? if >=26.1
        /*guiGraphics.nextStratum();*/
    }

    public static void enableBlend() {
        //? if <26.1
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
    }

    public static void disableBlend() {
        //? if <26.1
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }

    public static void setShaderColor(float r, float g, float b, float a) {
        //? if <26.1
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void defaultBlendFunc() {
        //? if <26.1
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    }

    public static int argb(float r, float g, float b, float a) {
        int ai = Math.max(0, Math.min(255, Math.round(a * 255.0f)));
        int ri = Math.max(0, Math.min(255, Math.round(r * 255.0f)));
        int gi = Math.max(0, Math.min(255, Math.round(g * 255.0f)));
        int bi = Math.max(0, Math.min(255, Math.round(b * 255.0f)));
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    //? if <1.20 {
    /*private static LegacyTooltipScreen legacyTooltipScreen() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        var screen = new LegacyTooltipScreen();
        screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        return screen;
    }

    private static final class LegacyTooltipScreen extends net.minecraft.client.gui.screens.Screen {
        private LegacyTooltipScreen() {
            super(Component.empty());
        }

        private void hover(com.mojang.blaze3d.vertex.PoseStack pose, Style style, int x, int y) {
            renderComponentHoverEffect(pose, style, x, y);
        }
    }
    *///?}

    private static int withAlpha(int color, float alpha) {
        int sourceAlpha = (color >>> 24) & 0xFF;
        if (sourceAlpha == 0) sourceAlpha = 0xFF;
        int combinedAlpha = Math.max(0, Math.min(255, Math.round(sourceAlpha * alpha)));
        return (combinedAlpha << 24) | (color & 0xFFFFFF);
    }

    private static int opaque(int color) {
        return ((color >>> 24) & 0xFF) == 0 ? color | 0xFF000000 : color;
    }

    private static void applyShaderColor(int color) {
        float a = ((color >>> 24) & 0xFF) / 255.0f;
        float r = ((color >>> 16) & 0xFF) / 255.0f;
        float g = ((color >>> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        enableBlend();
        setShaderColor(r, g, b, a);
    }

    private static void resetShaderColor() {
        setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        disableBlend();
    }
    /** Presents a tooltip after the screen contents on legacy Forge. */
    public static void scheduleTooltip(GuiGraphics graphics, Font font, Component tooltip, int x, int y) {
        //? if forge && <1.20 {
        /*net.rasanovum.rosetta.client.gui.legacy.LegacyTooltips.schedule(tooltip, x, y);
        *///?} else {
        renderTooltip(graphics, font, tooltip, x, y);
        //?}
    }

    public static void renderWidget(net.minecraft.client.gui.components.AbstractWidget widget,
                                    GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if >=26.1 {
        /*widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        *///?} else {
        widget.render(graphics, mouseX, mouseY, partialTick);
        //?}
    }
}
