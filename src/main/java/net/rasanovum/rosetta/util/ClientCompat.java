package net.rasanovum.rosetta.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Client-side helpers. */
public final class ClientCompat {
    private ClientCompat() {}

    public static Screen screen(Minecraft minecraft) {
        //? if >=26.2 {
        /*return minecraft.gui.screen();
        *///?} else {
        return minecraft.screen;
        //?}
    }

    public static void setScreen(Minecraft minecraft, Screen screen) {
        //? if >=26.2 {
        /*minecraft.gui.setScreen(screen);
        *///?} else {
        minecraft.setScreen(screen);
        //?}
    }

    public static Camera mainCamera(Minecraft minecraft) {
        //? if >=26.2 {
        /*return minecraft.gameRenderer.mainCamera();
        *///?} else {
        return minecraft.gameRenderer.getMainCamera();
        //?}
    }

    public static boolean isHudHidden(Minecraft minecraft) {
        //? if >=26.2 {
        /*return minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden;
        *///?} else {
        return minecraft.options.hideGui;
        //?}
    }

    public static ResourceLocation getPlayerSkin(Minecraft minecraft, Player player) {
        //? if <1.21 {
        /*return minecraft.getSkinManager().getInsecureSkinLocation(player.getGameProfile());
        *///?} else {
        //? if >=26.1 {
        /*return ((AbstractClientPlayer) player).getSkin().body().texturePath();
        *///?} else {
        return minecraft.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        //?}
        //?}
    }

    public static DynamicTexture dynamicTexture(String label, com.mojang.blaze3d.platform.NativeImage image) {
        //? if >=26.1 {
        /*return new DynamicTexture(() -> label, image);
        *///?} else {
        return new DynamicTexture(image);
        //?}
    }

    public static ResourceLocation registerTexture(TextureManager textureManager, String namespace, String label, DynamicTexture texture) {
        //? if >=26.1 {
        /*ResourceLocation id = RegistryCompat.getLocation(namespace, label);
        textureManager.register(id, texture);
        return id;
        *///?} else {
        return textureManager.register(label, texture);
        //?}
    }

    public static int nativeImageGetPixel(com.mojang.blaze3d.platform.NativeImage image, int x, int y) {
        //? if >=26.1 {
        /*return image.getPixel(x, y);
        *///?} else {
        return image.getPixelRGBA(x, y);
        //?}
    }

    public static void nativeImageSetPixel(com.mojang.blaze3d.platform.NativeImage image, int x, int y, int color) {
        //? if >=26.1 {
        /*image.setPixel(x, y, color);
        *///?} else {
        image.setPixelRGBA(x, y, color);
        //?}
    }

    public static long windowHandle(com.mojang.blaze3d.platform.Window window) {
        //? if >=26.1 {
        /*return window.handle();
        *///?} else {
        return window.getWindow();
        //?}
    }

    public static net.minecraft.world.phys.Vec3 cameraPosition(net.minecraft.client.Camera camera) {
        //? if >=26.1 {
        /*return camera.position();
        *///?} else {
        return camera.getPosition();
        //?}
    }

    public static void putVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int red, int green, int blue, int alpha) {
        //? if <1.21 {
        /*consumer.vertex(pose.pose(), x, y, z).color(red, green, blue, alpha).endVertex();
        *///?} else {
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
        //?}
    }
}
