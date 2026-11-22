package net.rasanovum.rosetta.mixin.client;

//? if >=26.1 {
/*//? if >=26.3 {
import com.mojang.renderpearl.api.textures.GpuSampler;
//?} else {
import com.mojang.blaze3d.textures.GpuSampler;
//?}
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractTexture.class)
public interface AbstractTextureSamplerAccessor {
    @Accessor("sampler")
    void rosetta$setSampler(GpuSampler sampler);
}
*///?}
