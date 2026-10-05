package net.rasanovum.rosetta.mixin.client;

//? if >=26.3 {
/*import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PictureInPictureRenderer.class)
public interface PictureInPictureRendererAccessor {
    @Accessor("textureView") GpuTextureView rosetta$getColorTarget();
    @Accessor("depthTextureView") GpuTextureView rosetta$getDepthTarget();
}
*///?}
