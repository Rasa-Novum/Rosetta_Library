package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class EditBox extends net.minecraft.client.gui.components.EditBox {
    public EditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        renderWidget(pose, mouseX, mouseY, delta);
    }

    public void renderWidget(PoseStack pose, int mouseX, int mouseY, float delta) {
        super.renderButton(pose, mouseX, mouseY, delta);
    }
}
*///?}
