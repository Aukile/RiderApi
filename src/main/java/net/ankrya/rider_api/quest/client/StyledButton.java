package net.ankrya.rider_api.quest.client;

import net.ankrya.rider_api.quest.client.style.GuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class StyledButton extends Button {
    private final GuiStyle style;

    public StyledButton(int x, int y, int w, int h, Component label,
                        OnPress onPress, GuiStyle style) {
        super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
        this.style = style;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mx, int my, float pt) {
        boolean hover = isHoveredOrFocused();
        var tex = hover ? style.dialogueButtonHoverTexture : style.dialogueButtonTexture;
        if (tex == null) tex = style.dialogueButtonTexture;

        if (tex != null) {
            g.blit(tex, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
            // 文字居中
            int tx = getX() + getWidth() / 2;
            int ty = getY() + (getHeight() - 8) / 2;
            g.drawCenteredString(Minecraft.getInstance().font,
                    getMessage(), tx, ty, 0xFFFFFF);
        } else {
            super.renderWidget(g, mx, my, pt); // 没贴图用原版外观
        }
    }
}
