package net.ankrya.rider_api.quest.editor.style;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/** 点击色块弹出的 ARGB 调色板，四条滑块 */
public class ColorPickerPopup {

    private final Font font;
    private final IntConsumer onChange;

    public int x, y;
    public final int width = 160;
    public final int height = 92;

    private int a, r, g, b;
    private int dragging = -1; // 0=A 1=R 2=G 3=B

    private static final int ROW_H = 18;
    private static final int SLIDER_X_OFF = 24;
    private static final int SLIDER_W = 110;

    public ColorPickerPopup(Font font, int initialArgb, IntConsumer onChange) {
        this.font = font;
        this.onChange = onChange;
        this.a = (initialArgb >> 24) & 0xFF;
        this.r = (initialArgb >> 16) & 0xFF;
        this.g = (initialArgb >> 8)  & 0xFF;
        this.b =  initialArgb        & 0xFF;
    }

    public void setPos(int x, int y) { this.x = x; this.y = y; }

    private int current() {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public void render(GuiGraphics gr, int mx, int my) {
        // 面板背景
        gr.fill(x, y, x + width, y + height, 0xF0202028);
        gr.renderOutline(x, y, width, height, 0xFFAAAAAA);

        drawSlider(gr, 0, "A", a);
        drawSlider(gr, 1, "R", r);
        drawSlider(gr, 2, "G", g);
        drawSlider(gr, 3, "B", b);

        // 右上角当前颜色预览
        int pvX = x + width - 18, pvY = y + 2;
        drawChecker(gr, pvX, pvY, 14, 14);
        gr.fill(pvX, pvY, pvX + 14, pvY + 14, current());
        gr.renderOutline(pvX, pvY, 14, 14, 0xFF000000);
    }

    private void drawSlider(GuiGraphics gr, int idx, String label, int val) {
        int ry = y + 4 + idx * ROW_H;
        int sx = x + SLIDER_X_OFF;
        gr.drawString(font, Component.literal(label), x + 6, ry + 2, 0xFFFFFFFF);
        // 轨道
        gr.fill(sx, ry + 4, sx + SLIDER_W, ry + 8, 0xFF555555);
        // 滑块位置
        int hx = sx + (int)((val / 255f) * SLIDER_W);
        gr.fill(hx - 2, ry, hx + 2, ry + 12, 0xFFFFFFFF);
        // 数值
        gr.drawString(font, Component.literal(String.valueOf(val)),
                sx + SLIDER_W + 4, ry + 2, 0xFFCCCCCC);
    }

    public boolean mouseClicked(double mxD, double myD, int btn) {
        int mx = (int) mxD, my = (int) myD;
        if (mx < x || mx > x + width || my < y || my > y + height) return false;
        for (int i = 0; i < 4; i++) {
            int ry = y + 4 + i * ROW_H;
            if (my >= ry && my <= ry + 12) {
                dragging = i;
                applyDrag(mx);
                return true;
            }
        }
        return true; // 点在面板内也吞掉，避免误触外部
    }

    public boolean mouseDragged(double mxD, double myD, int btn) {
        if (dragging >= 0) { applyDrag((int) mxD); return true; }
        return false;
    }

    public void mouseReleased() { dragging = -1; }

    private void applyDrag(int mx) {
        int sx = x + SLIDER_X_OFF;
        int val = Math.round(((mx - sx) / (float) SLIDER_W) * 255f);
        val = Math.max(0, Math.min(255, val));
        switch (dragging) {
            case 0 -> a = val;
            case 1 -> r = val;
            case 2 -> g = val;
            case 3 -> b = val;
        }
        onChange.accept(current());
    }

    /** 点击是否落在面板外（用于关闭） */
    public boolean isOutside(double mx, double my) {
        return mx < x || mx > x + width || my < y || my > y + height;
    }

    private static void drawChecker(GuiGraphics g, int x, int y, int w, int h) {
        int cell = 4;
        for (int iy = 0; iy < h; iy += cell)
            for (int ix = 0; ix < w; ix += cell) {
                boolean dark = ((ix / cell) + (iy / cell)) % 2 == 0;
                g.fill(x + ix, y + iy,
                        Math.min(x + ix + cell, x + w),
                        Math.min(y + iy + cell, y + h),
                        dark ? 0xFF888888 : 0xFFCCCCCC);
            }
    }
}