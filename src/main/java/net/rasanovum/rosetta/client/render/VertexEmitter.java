package net.rasanovum.rosetta.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
//? if >=1.21 {
import net.minecraft.client.renderer.texture.OverlayTexture;
//?}

/** Emits full-bright textured vertices with an explicit normal. */
public final class VertexEmitter {
    private static final int FULL_BRIGHT = 0xF000F0;

    private VertexEmitter() {}

    public static void fullBright(PoseStack.Pose pose, VertexConsumer consumer,
                                  float x, float y, float z, float red, float green, float blue, float alpha,
                                  float u, float v, float normalX, float normalY, float normalZ) {
        //? if <1.21 {
        /*consumer.vertex(pose.pose(), x, y, z).color(red, green, blue, alpha).uv(u, v)
                .overlayCoords(0).uv2(FULL_BRIGHT).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
        *///?} else {
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, normalX, normalY, normalZ);
        //?}
    }

    public static void fullBright(VertexConsumer consumer,
                                  float x, float y, float z, float red, float green, float blue, float alpha,
                                  float u, float v, float normalX, float normalY, float normalZ) {
        //? if <1.21 {
        /*consumer.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v)
                .overlayCoords(0).uv2(FULL_BRIGHT).normal(normalX, normalY, normalZ).endVertex();
        *///?} else {
        consumer.addVertex(x, y, z).setColor(red, green, blue, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(normalX, normalY, normalZ);
        //?}
    }
}
