package net.rasanovum.rosetta.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
//? if >=26.1 {
/*import net.rasanovum.rosetta.mixin.client.AbstractTextureSamplerAccessor;
//? if >=26.3 {
import com.mojang.renderpearl.api.textures.FilterMode;
//?} else {
import com.mojang.blaze3d.textures.FilterMode;
//?}
*///?} else {
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
//?}

/** Filtering and addressing for uploaded textures. */
public final class TextureCompat {
    private TextureCompat() {}

    public static void configure(AbstractTexture texture, boolean linear, boolean repeat) {
        RenderSystem.assertOnRenderThread();
        //? if >=26.1 {
        /*FilterMode filter = linear ? FilterMode.LINEAR : FilterMode.NEAREST;
        var samplers = RenderSystem.getSamplerCache();
        var sampler = repeat ? samplers.getRepeat(filter) : samplers.getClampToEdge(filter);
        ((AbstractTextureSamplerAccessor) texture).rosetta$setSampler(sampler);
        *///?} else {
        RenderSystem.bindTexture(texture.getId());
        int filter = linear ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        int wrap = repeat ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
        //?}
    }
}
