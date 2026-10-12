package net.ankrya.rider_api.quest.editor.style;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DialogueLayoutEditorScreen extends Screen {

    private final Screen parent;
    private final StyleEditorData data;

    private enum Handle { NONE, PAD_LEFT, PAD_TOP, PAD_RIGHT, PAD_BOTTOM,
        BOX_W, BOX_H, BTN_W, BTN_H, NAME_Y, DIVIDER_Y, PROGRESS_XY }
    private Handle dragging = Handle.NONE;

    private int boxX, boxY;
    private static final int HIT = 6; // 手柄命中半径，放大点好点

    public DialogueLayoutEditorScreen(Screen parent, StyleEditorData data) {
        super(Component.translatable("gui.api.style.layout.title"));
        this.parent = parent;
        this.data = data;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 26, 100, 20).build());
    }

    private void updateBoxPos() {
        boxX = (width - data.dialogueBoxWidth) / 2;
        boxY = height - data.dialogueBoxHeight - 60;
    }

    private static ResourceLocation parseTex(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new ResourceLocation(s); } catch (Exception e) { return null; }
    }

    // ===== 统一计算所有手柄坐标，渲染和命中共用，保证一致 =====
    private int[] handlePos(Handle h) {
        int bw = data.dialogueBoxWidth, bh = data.dialogueBoxHeight;
        int padL = data.dialoguePaddingLeft, padT = data.dialoguePaddingTop;
        int padR = data.dialoguePaddingRight, padB = data.dialoguePaddingBottom;
        int innerL = boxX + padL, innerT = boxY + padT;
        int innerR = boxX + bw - padR, innerB = boxY + bh - padB;
        int btnW = data.dialogueButtonWidth, btnH = data.dialogueButtonHeight;
        int btnX = innerR - btnW, btnY = innerB - btnH;

        return switch (h) {
            case PAD_LEFT   -> new int[]{ innerL, boxY + bh / 2 };
            case PAD_TOP    -> new int[]{ boxX + bw / 2, innerT };
            case PAD_RIGHT  -> new int[]{ innerR, boxY + bh / 2 };
            case PAD_BOTTOM -> new int[]{ boxX + bw / 2, innerB };
            case BOX_W      -> new int[]{ boxX + bw, boxY + bh / 2 };
            case BOX_H      -> new int[]{ boxX + bw / 2, boxY + bh };
            case BTN_W      -> new int[]{ btnX, btnY + btnH / 2 };
            case BTN_H      -> new int[]{ btnX + btnW / 2, btnY };
            case NAME_Y     -> new int[]{ boxX + bw / 2, boxY + data.dialogueNameOffsetY };
            case DIVIDER_Y  -> new int[]{ boxX + bw / 2, boxY + data.dialogueDividerOffsetY };
            case PROGRESS_XY -> {
                String prog = "1/3";
                int px = boxX + bw - font.width(prog) - data.dialogueProgressOffsetX;
                int py = boxY + data.dialogueProgressOffsetY;
                yield new int[]{ px, py };
            }
            default -> new int[]{ -1000, -1000 };
        };
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        updateBoxPos();

        int bw = data.dialogueBoxWidth;
        int bh = data.dialogueBoxHeight;

        // 背景贴图
        ResourceLocation bgTex = parseTex(data.dialogueBgTexture);
        if (bgTex != null) {
            try { g.blit(bgTex, boxX, boxY, 0, 0, bw, bh, bw, bh); }
            catch (Exception e) { g.fill(boxX, boxY, boxX + bw, boxY + bh, data.dialogueBgColor); }
        } else {
            g.fill(boxX, boxY, boxX + bw, boxY + bh, data.dialogueBgColor);
        }
        g.renderOutline(boxX, boxY, bw, bh, data.dialogueBorderColor);

        int padL = data.dialoguePaddingLeft, padT = data.dialoguePaddingTop;
        int padR = data.dialoguePaddingRight, padB = data.dialoguePaddingBottom;
        int innerL = boxX + padL, innerT = boxY + padT;
        int innerR = boxX + bw - padR, innerB = boxY + bh - padB;

        g.renderOutline(innerL, innerT, innerR - innerL, innerB - innerT, 0x8800FF00);

        // 预览 NPC 名字
        g.drawString(font, Component.translatable("gui.api.style.sample.npc"),
                boxX + data.dialoguePaddingLeft,
                boxY + data.dialogueNameOffsetY,
                data.dialogueTitleColor);

        // 预览分割线
        g.fill(boxX + data.dialoguePaddingLeft,
                boxY + data.dialogueDividerOffsetY,
                boxX + bw - data.dialoguePaddingRight,
                boxY + data.dialogueDividerOffsetY + 1,
                data.dialogueDividerColor);

        // 预览页数
        String prog = "1/3";
        g.drawString(font, prog,
                boxX + bw - font.width(prog) - data.dialogueProgressOffsetX,
                boxY + data.dialogueProgressOffsetY,
                data.dialogueProgressColor);

        g.drawString(font, Component.translatable("gui.api.style.sample.line"),
                innerL, innerT, data.dialogueBodyColor);

        int btnW = data.dialogueButtonWidth, btnH = data.dialogueButtonHeight;
        int btnX = innerR - btnW, btnY = innerB - btnH;
        ResourceLocation btnTex = parseTex(data.dialogueButtonTexture);
        if (btnTex != null) {
            try { g.blit(btnTex, btnX, btnY, 0, 0, btnW, btnH, btnW, btnH); }
            catch (Exception e) { g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF338833); }
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF338833);
        }
        g.drawCenteredString(font, Component.translatable("gui.api.dialogue.next"),
                btnX + btnW / 2, btnY + btnH / 2 - 4, 0xFFFFFF);

        // 用统一坐标画手柄
        drawHandle(g, Handle.PAD_LEFT, 0xFFFFAA00);
        drawHandle(g, Handle.PAD_TOP, 0xFFFFAA00);
        drawHandle(g, Handle.PAD_RIGHT, 0xFFFFAA00);
        drawHandle(g, Handle.PAD_BOTTOM, 0xFFFFAA00);
        drawHandle(g, Handle.BOX_W, 0xFF00AAFF);
        drawHandle(g, Handle.BOX_H, 0xFF00AAFF);
        drawHandle(g, Handle.BTN_W, 0xFFFF00FF);
        drawHandle(g, Handle.BTN_H, 0xFFFF00FF);
        drawHandle(g, Handle.NAME_Y, 0xFF00FFAA);
        drawHandle(g, Handle.DIVIDER_Y, 0xFF00FFAA);
        drawHandle(g, Handle.PROGRESS_XY, 0xFFAA00FF);

        g.drawString(font, "Padding L:" + padL + " T:" + padT
                + " R:" + padR + " B:" + padB, 10, 20, 0xFFFFFF);
        g.drawString(font, "Box: " + bw + "x" + bh
                + "  Btn: " + btnW + "x" + btnH
                + "  NameY: " + data.dialogueNameOffsetY
                + "  DivY: " + data.dialogueDividerOffsetY
                + "  ProgX: " + data.dialogueProgressOffsetX
                + "  ProgY: " + data.dialogueProgressOffsetY, 10, 32, 0xFFFFFF);
        if (dragging != Handle.NONE)
            g.drawString(font, "Dragging: " + dragging, 10, 44, 0xFFFF00);

        super.render(g, mx, my, pt);
    }

    private void drawHandle(GuiGraphics g, Handle h, int color) {
        int[] p = handlePos(h);
        g.fill(p[0] - 3, p[1] - 3, p[0] + 3, p[1] + 3, color);
        g.renderOutline(p[0] - 3, p[1] - 3, 6, 6, 0xFF000000);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn); // 只左键拖

        updateBoxPos(); // 确保 boxX/boxY 是最新的

        // 遍历所有手柄，命中就开始拖
        for (Handle h : Handle.values()) {
            if (h == Handle.NONE) continue;
            int[] p = handlePos(h);
            if (Math.abs(mx - p[0]) <= HIT && Math.abs(my - p[1]) <= HIT) {
                dragging = h;
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging == Handle.NONE) return super.mouseDragged(mx, my, btn, dx, dy);

        int bw = data.dialogueBoxWidth, bh = data.dialogueBoxHeight;
        switch (dragging) {
            case PAD_LEFT   -> data.dialoguePaddingLeft   = clamp((int)(mx - boxX), 0, bw / 2);
            case PAD_TOP    -> data.dialoguePaddingTop    = clamp((int)(my - boxY), 0, bh / 2);
            case PAD_RIGHT  -> data.dialoguePaddingRight  = clamp((int)(boxX + bw - mx), 0, bw / 2);
            case PAD_BOTTOM -> data.dialoguePaddingBottom = clamp((int)(boxY + bh - my), 0, bh / 2);
            case BOX_W      -> data.dialogueBoxWidth  = clamp((int)(mx - boxX), 80, width - 40);
            case BOX_H      -> data.dialogueBoxHeight = clamp((int)(my - boxY), 40, height - 80);
            case BTN_W -> {
                int innerR = boxX + data.dialogueBoxWidth - data.dialoguePaddingRight;
                data.dialogueButtonWidth = clamp((int)(innerR - mx), 20, 200);
            }
            case BTN_H -> {
                int innerB = boxY + data.dialogueBoxHeight - data.dialoguePaddingBottom;
                data.dialogueButtonHeight = clamp((int)(innerB - my), 12, 60);
            }
            case NAME_Y    -> data.dialogueNameOffsetY    = clamp((int)(my - boxY), 0, bh - 4);
            case DIVIDER_Y -> data.dialogueDividerOffsetY = clamp((int)(my - boxY), 0, bh - 2);
            case PROGRESS_XY -> {
                data.dialogueProgressOffsetX = clamp((int)(boxX + bw - mx), 0, bw / 2);
                data.dialogueProgressOffsetY = clamp((int)(my - boxY), 0, bh / 2);
            }
            default -> {}
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (dragging != Handle.NONE) {
            dragging = Handle.NONE;
            return true;
        }
        return super.mouseReleased(mx, my, btn);
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override public boolean isPauseScreen() { return false; }
}