package net.rasanovum.rosetta.client.render;

//? if >=26.1 {
/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Adapts the modern GPU API. Callers retain resource ownership and render-pass lifetime.
public final class GpuCompat {
    private GpuCompat() {}

    public static com.mojang.blaze3d.pipeline.TextureTarget createTextureTarget(String label, int width, int height, boolean depth) {
        //? if >=26.3 {
        return new com.mojang.blaze3d.pipeline.TextureTarget(label, width, height,
                com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM, depth ? com.mojang.renderpearl.api.GpuFormat.D32_FLOAT : null);
        //?} else if >=26.2 {
        return new com.mojang.blaze3d.pipeline.TextureTarget(label, width, height, depth, com.mojang.blaze3d.GpuFormat.RGBA8_UNORM);
        //?} else {
        return new com.mojang.blaze3d.pipeline.TextureTarget(label, width, height, depth);
        //?}
    }

    public static void writeBuffer(CommandEncoder encoder, GpuBufferSlice buffer, Consumer<ByteBuffer> writer) {
        //? if >=26.2 {
        try (var mapped = buffer.map(false, true)) {
        //?} else {
        try (var mapped = encoder.mapBuffer(buffer, false, true)) {
        //?}
            writer.accept(mapped.data());
        }
    }

    public static RenderPass renderPass(CommandEncoder encoder, Supplier<String> label,
            GpuTextureView color, GpuTextureView depth) {
        //? if >=26.2 {
        return encoder.createRenderPass(label, color, Optional.empty(), depth, OptionalDouble.empty());
        //?} else {
        return encoder.createRenderPass(label, color, OptionalInt.empty(), depth, OptionalDouble.empty());
        //?}
    }

    public static void setPipeline(RenderPass pass, RenderPipeline pipeline) {
        //? if >=26.3 {
        pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        //?} else {
        pass.setPipeline(pipeline);
        //?}
    }

    public static void bindTexture(RenderPass pass, String name, GpuTextureView texture, GpuSampler sampler) {
        //? if >=26.3 {
        pass.setUniform(name, texture, sampler);
        //?} else {
        pass.bindTexture(name, texture, sampler);
        //?}
    }

    public static void submit(CommandEncoder encoder) {
        //? if >=26.3 {
        encoder.submit();
        //?}
    }

    public static void vertexBuffer(RenderPass pass, GpuBuffer buffer) {
        //? if >=26.2 {
        pass.setVertexBuffer(0, buffer.slice());
        //?} else {
        pass.setVertexBuffer(0, buffer);
        //?}
    }

    public static void drawIndexed(RenderPass pass, int indexCount) {
        //? if >=26.2 {
        pass.drawIndexed(indexCount, 1, 0, 0, 0);
        //?} else {
        pass.drawIndexed(0, 0, indexCount, 1);
        //?}
    }

    public static RenderPipeline.Builder bindings(RenderPipeline.Builder builder, String[] samplers, String... uniforms) {
        //? if >=26.2 {
        var layout = com.mojang.blaze3d.pipeline.BindGroupLayout.builder();
        //? if >=26.3 {
        for (String sampler : samplers) layout.withUniform(sampler, UniformType.COMBINED_IMAGE_SAMPLER);
        //?} else {
        for (String sampler : samplers) layout.withSampler(sampler);
        //?}
        for (String uniform : uniforms) layout.withUniform(uniform, UniformType.UNIFORM_BUFFER);
        return builder.withBindGroupLayout(layout.build());
        //?} else {
        for (String sampler : samplers) builder.withSampler(sampler);
        for (String uniform : uniforms) builder.withUniform(uniform, UniformType.UNIFORM_BUFFER);
        return builder;
        //?}
    }

    public static RenderPipeline.Builder quads(RenderPipeline.Builder builder, VertexFormat format) {
        //? if >=26.2 {
        return builder.withVertexBinding(0, format).withPrimitiveTopology(com.mojang.blaze3d.PrimitiveTopology.QUADS);
        //?} else {
        return builder.withVertexFormat(format, VertexFormat.Mode.QUADS);
        //?}
    }
}
*///?}
