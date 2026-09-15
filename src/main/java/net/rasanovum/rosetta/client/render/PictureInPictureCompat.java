package net.rasanovum.rosetta.client.render;

//? if >=26.1 {
/*import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;

public final class PictureInPictureCompat {
    private PictureInPictureCompat() {}

    // Borrowed targets owned by Minecraft; valid only during renderToTexture.
    public record Targets(GpuTextureView color, GpuTextureView depth) {}

    public static Targets targets(PictureInPictureRenderer<?> renderer) {
        //? if >=26.3 {
        var accessor = (net.rasanovum.rosetta.loaders.fabric.mixin.PictureInPictureRendererAccessor) renderer;
        return new Targets(accessor.rosetta$getColorTarget(), accessor.rosetta$getDepthTarget());
        //?} else {
        //? if >=26.2 {
        var main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        //?} else {
        var main = Minecraft.getInstance().getMainRenderTarget();
        //?}
        return new Targets(
                RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : main.getColorTextureView(),
                RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : main.getDepthTextureView());
        //?}
    }
}
*///?}
