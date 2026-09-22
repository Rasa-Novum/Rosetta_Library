package net.rasanovum.rosetta.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
//? if <1.19.3 {
/*import com.mojang.math.Matrix4f;
*///?} else {
import org.joml.Matrix4f;
//?}
//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3x2f;
*///?}

/** Emits textured quads with normalized RGBA colors, preserving each GUI backend's winding. */
public final class TexturedQuadEmitter {
    private TexturedQuadEmitter() {}

    public static void emit(BufferBuilder buffer, Matrix4f matrix,
                            float x0, float y0, float x1, float y1, float z,
                            float u0, float v0, float u1, float v1,
                            float red, float green, float blue, float alpha) {
        vertex(buffer, matrix, x0, y1, z, u0, v1, red, green, blue, alpha);
        vertex(buffer, matrix, x1, y1, z, u1, v1, red, green, blue, alpha);
        vertex(buffer, matrix, x1, y0, z, u1, v0, red, green, blue, alpha);
        vertex(buffer, matrix, x0, y0, z, u0, v0, red, green, blue, alpha);
    }

    //? if >=26.1 {
    /*public static void emit(VertexConsumer vertices, Matrix3x2f pose,
                            float x0, float y0, float x1, float y1,
                            float u0, float v0, float u1, float v1,
                            float red, float green, float blue, float alpha) {
        vertices.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(red, green, blue, alpha);
        vertices.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(red, green, blue, alpha);
        vertices.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(red, green, blue, alpha);
        vertices.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(red, green, blue, alpha);
    }
    *///?}

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float u, float v,
                               float red, float green, float blue, float alpha) {
        //? if >1.20.1 {
        buffer.addVertex(matrix, x, y, z).setUv(u, v).setColor(red, green, blue, alpha);
        //?} else {
        /*buffer.vertex(matrix, x, y, z).uv(u, v).color(red, green, blue, alpha).endVertex();
        *///?}
    }
}
