package net.rasanovum.rosetta.client.gui.legacy;

//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.rasanovum.rosetta.client.gui.GuiCompat;
import java.util.List;

public final class MultiLineTextWidget extends AbstractWidget {
    private final Font font;
    private boolean centered;
    private List<FormattedCharSequence> lines;

    public MultiLineTextWidget(int x, int y, Component text, Font font) {
        super(x, y, font.width(text), font.lineHeight, text);
        this.font = font;
        lines = List.of(text.getVisualOrderText());
    }
    public MultiLineTextWidget setCentered(boolean centered) { this.centered = centered; return this; }
    public void setTooltip(Tooltip tooltip) { LegacyTooltips.setTooltip(this, tooltip); }
    public void setMaxWidth(int width) {
        lines = font.split(getMessage(), Math.max(1, width));
        this.width = lines.stream().mapToInt(font::width).max().orElse(0);
        this.height = lines.size() * font.lineHeight;
    }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            GuiCompat.drawString(pose, font, line, x + (centered ? (width - font.width(line)) / 2 : 0),
                    y + i * font.lineHeight, 0xFFFFFFFF, true);
        }
    }
    @Override
    public void updateNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
*///?}
