package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.rasanovum.rosetta.client.gui.GuiCompat;

public final class TextAndImageButton extends Button {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int imageWidth;
    private final int imageHeight;
    private final int offsetX;
    private final int offsetY;

    private TextAndImageButton(Builder builder) {
        super(0, 0, 20, 20, builder.text, builder.action);
        texture = new ResourceLocation(builder.texture.getNamespace(), "textures/gui/sprites/" + builder.texture.getPath());
        textureWidth = builder.textureWidth;
        textureHeight = builder.textureHeight;
        imageWidth = builder.imageWidth;
        imageHeight = builder.imageHeight;
        offsetX = builder.offsetX;
        offsetY = builder.offsetY;
    }

    public static Builder builder(Component text, ResourceLocation texture, OnPress action) {
        return new Builder(text, texture, action);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        Component label = getMessage();
        setMessage(Component.nullToEmpty(""));
        super.renderButton(pose, mouseX, mouseY, delta);
        setMessage(label);
        GuiCompat.blit(pose, texture, x + (width - imageWidth) / 2 + offsetX, y + offsetY,
                0, 0, imageWidth, imageHeight, textureWidth, textureHeight);
    }

    public static final class Builder {
        private final Component text;
        private final ResourceLocation texture;
        private final OnPress action;
        private int textureWidth = 16;
        private int textureHeight = 16;
        private int imageWidth = 16;
        private int imageHeight = 16;
        private int offsetX;
        private int offsetY;
        private Builder(Component text, ResourceLocation texture, OnPress action) {
            this.text = text;
            this.texture = texture;
            this.action = action;
        }
        public Builder textureSize(int width, int height) {
            textureWidth = width;
            textureHeight = height;
            return this;
        }
        public Builder usedTextureSize(int width, int height) { imageWidth = width; imageHeight = height; return this; }
        public Builder offset(int x, int y) { offsetX = x; offsetY = y; return this; }
        public TextAndImageButton build() { return new TextAndImageButton(this); }
    }
}
*///?}
