package net.rasanovum.rosetta.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
//? if >=26.1 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
*///?} else {
import net.minecraft.client.renderer.RenderType;
//?}
//? if >=26.2 {
/*import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.StagedVertexBuffer;
*///?} else {
import net.minecraft.client.Minecraft;
//?}

/** Immediate overlays. Owns staged storage on 26.2; borrows vanilla's buffer source on older versions.
 * All operations run on the render thread. close releases storage; the adapter can be reused after reconnect.
 */
public final class GeometryBuffers implements AutoCloseable {
    private final String label;
    //? if >=26.2 {
    /*private StagedVertexBuffer buffer;
    private RenderType activeType;
    private StagedVertexBuffer.Draw draw;
    *///?}

    public GeometryBuffers(String label) {
        this.label = label;
    }

    public VertexConsumer getBuffer(RenderType type) {
        RenderSystem.assertOnRenderThread();
        //? if >=26.2 {
        /*if (buffer == null) buffer = new StagedVertexBuffer(() -> label, 4096);
        if (activeType != type) {
            endBatch();
            activeType = type;
            draw = buffer.appendDraw(type.format(), type.primitiveTopology(),
                    type.sortOnUpload() ? VertexSorting.DISTANCE_TO_ORIGIN : null);
        }
        return buffer.getVertexBuilder(draw);
        *///?} else {
        return Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(type);
        //?}
    }

    public void endBatch(RenderType type) {
        RenderSystem.assertOnRenderThread();
        //? if >=26.2 {
        /*if (activeType == type) endBatch();
        *///?} else {
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(type);
        //?}
    }

    public void endBatch() {
        RenderSystem.assertOnRenderThread();
        //? if >=26.2 {
        /*if (activeType == null) return;
        buffer.upload();
        var info = buffer.getExecuteInfo(draw);
        //? if >=26.3 {
        if (info != null) {
            var target = net.minecraft.client.Minecraft.getInstance().gameRenderer.mainRenderTarget();
            var prepared = activeType.prepare();
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            try (var pass = GpuCompat.renderPass(encoder, () -> label,
                    target.getColorTextureView(), target.getDepthTextureView())) {
                prepared.drawFromBuffer(info, pass);
            }
            encoder.submit();
        }
        //?} else {
        if (info != null) activeType.prepare().drawFromBuffer(info);
        //?}
        buffer.endDraw();
        activeType = null;
        draw = null;
        *///?} else {
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        //?}
    }

    public void endFrame() {
        RenderSystem.assertOnRenderThread();
        //? if >=26.2 {
        /*if (buffer != null) {
            endBatch();
            buffer.endFrame();
        }
        *///?}
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        //? if >=26.2 {
        /*if (buffer != null) {
            buffer.close();
            buffer = null;
            activeType = null;
            draw = null;
        }
        *///?}
    }
}
