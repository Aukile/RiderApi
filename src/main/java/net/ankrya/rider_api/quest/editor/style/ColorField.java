package net.ankrya.rider_api.quest.editor.style;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** 一行颜色编辑：色块 + hex 输入 */
public class ColorField {

    private final Font font;
    private final String labelKey;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final EditBox hexBox;

    public int x, y;
    public final int width;
    private int baseY;

    public ColorField(Font font, int width, String labelKey,
                      IntSupplier getter, IntConsumer setter) {
        this.font = font;
        this.width = width;
        this.labelKey = labelKey;
        this.getter = getter;
        this.setter = setter;

        this.hexBox = new EditBox(font, 0, 0, 80, 14, Component.empty());
        this.hexBox.setMaxLength(8);
        this.hexBox.setValue(String.format("%08X", getter.getAsInt()));
        this.hexBox.setResponder(v -> {
            try {
                long parsed = Long.parseLong(v.trim(), 16);
                setter.accept((int) parsed);
            } catch (NumberFormatException ignored) {}
        });
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        hexBox.setX(x + width - 82);
        hexBox.setY(y);
    }

    public EditBox getHexBox() { return hexBox; }

    public void setBaseY(int y) { this.baseY = y; }
    public int getBaseY() { return baseY; }

    public int getColor() { return getter.getAsInt(); }

    public void setColor(int argb) { setter.accept(argb); }

    public void render(GuiGraphics g, int mx, int my) {
        // 标签
        g.drawString(font, Component.translatable(labelKey), x, y + 3, 0xFFCCCCCC);
        // 色块（含棋盘格底显示 alpha）
        int swX = x + width - 100;
        drawChecker(g, swX, y, 14, 14);
        g.fill(swX, y, swX + 14, y + 14, getter.getAsInt());
        g.renderOutline(swX, y, 14, 14, 0xFF000000);
        // hex 框
        hexBox.render(g, mx, my, 0);
    }

    /** 每次 tick 同步一下 hex 显示（外部改了颜色时） */
    public void syncHex() {
        String cur = String.format("%08X", getter.getAsInt());
        if (!hexBox.getValue().equalsIgnoreCase(cur) && !hexBox.isFocused()) {
            hexBox.setValue(cur);
        }
    }

    public boolean mouseClicked(double mxD, double myD, int btn) {
        return hexBox.mouseClicked(mxD, myD, btn);
    }
    public boolean keyPressed(int k, int s, int m) { return hexBox.keyPressed(k, s, m); }
    public boolean charTyped(char c, int m) { return hexBox.charTyped(c, m); }

    private static void drawChecker(GuiGraphics g, int x, int y, int w, int h) {
        int cell = 4;
        for (int iy = 0; iy < h; iy += cell) {
            for (int ix = 0; ix < w; ix += cell) {
                boolean dark = ((ix / cell) + (iy / cell)) % 2 == 0;
                g.fill(x + ix, y + iy,
                        Math.min(x + ix + cell, x + w),
                        Math.min(y + iy + cell, y + h),
                        dark ? 0xFF888888 : 0xFFCCCCCC);
            }
        }
    }
}