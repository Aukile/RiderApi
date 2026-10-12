package net.ankrya.rider_api.quest.editor.style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ankrya.rider_api.quest.client.style.GuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class StyleEditorScreen extends Screen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final StyleEditorData data = new StyleEditorData();

    // 统一的滚动内容项
    private record Row(AbstractWidget widget, int baseY) {}
    private record Label(String key, int x, int baseY) {}
    private final List<Row> rows = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final List<ColorField> colorFields = new ArrayList<>();

    private int scrollY = 0;
    private int contentHeight = 0;
    private int areaTop, areaBottom;
    private int previewMode = 0;
    @Nullable private ColorPickerPopup activePopup = null;

    private static final int LEFT_X = 10;

    public StyleEditorScreen() {
        super(Component.translatable("gui.api.style.title"));
    }

    @Override
    protected void init() {
        areaTop = 30;
        areaBottom = height - 40;

        // 底部按钮（固定，不滚动）
        addRenderableWidget(Button.builder(Component.translatable("gui.api.style.edit_layout"),
                b -> minecraft.setScreen(new DialogueLayoutEditorScreen(this, data))
        ).bounds(width / 2 + 10, height - 30, 120, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.api.style.preview_toggle"),
                b -> previewMode = 1 - previewMode
        ).bounds(width / 2 + 135, height - 30, 90, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.export"),
                b -> exportJson()
        ).bounds(width / 2 - 100, height - 30, 100, 20).build());

        buildContent();
    }

    private void buildContent() {
        // 清理旧的
        rows.forEach(r -> removeWidget(r.widget()));
        rows.clear();
        labels.clear();
        colorFields.clear();

        int fieldW = width / 2 - 30; // 留出右侧滚动条空间
        int y = 0;

        // ===== 基本 =====
        y = section(y, "gui.api.style.section.basic");
        y = texRow(y, fieldW, "gui.api.style.field.id",
                data.styleId, v -> data.styleId = v, false);

        // ===== 对话框贴图 =====
        y = section(y, "gui.api.style.section.dialogue_tex");
        y = texRow(y, fieldW, "gui.api.style.field.dialogue_bg",
                data.dialogueBgTexture, v -> data.dialogueBgTexture = nz(v), true);
        y = texRow(y, fieldW, "gui.api.style.field.dlg_button_tex",
                data.dialogueButtonTexture, v -> data.dialogueButtonTexture = nz(v), true);
        y = texRow(y, fieldW, "gui.api.style.field.dlg_button_hover_tex",
                data.dialogueButtonHoverTexture, v -> data.dialogueButtonHoverTexture = nz(v), true);

        // ===== 对话框颜色 =====
        y = section(y, "gui.api.style.section.dialogue_color");
        y = colorRow(y, fieldW, "gui.api.style.color.dlg_bg", () -> data.dialogueBgColor, v -> data.dialogueBgColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.dlg_border", () -> data.dialogueBorderColor, v -> data.dialogueBorderColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.dlg_title", () -> data.dialogueTitleColor, v -> data.dialogueTitleColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.dlg_body", () -> data.dialogueBodyColor, v -> data.dialogueBodyColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.dlg_divider", () -> data.dialogueDividerColor, v -> data.dialogueDividerColor = v);

        // ===== 日志贴图 =====
        y = section(y, "gui.api.style.section.journal_tex");
        y = texRow(y, fieldW, "gui.api.style.field.journal_bg",
                data.journalBgTexture, v -> data.journalBgTexture = nz(v), true);
        y = texRow(y, fieldW, "gui.api.style.field.journal_header",
                data.journalHeaderTexture, v -> data.journalHeaderTexture = nz(v), true);
        y = texRow(y, fieldW, "gui.api.style.field.journal_item",
                data.journalItemTexture, v -> data.journalItemTexture = nz(v), true);
        y = texRow(y, fieldW, "gui.api.style.field.button_tex",
                data.journalButtonTexture, v -> data.journalButtonTexture = nz(v), true);
        y = texRow(y, fieldW, "gui.api.style.field.button_hover_tex",
                data.journalButtonHoverTexture, v -> data.journalButtonHoverTexture = nz(v), true);

        // ===== 日志颜色 =====
        y = section(y, "gui.api.style.section.journal_color");
        y = colorRow(y, fieldW, "gui.api.style.color.jn_frame", () -> data.journalFrameColor, v -> data.journalFrameColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_screen", () -> data.journalScreenColor, v -> data.journalScreenColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_header_bg", () -> data.journalHeaderBgColor, v -> data.journalHeaderBgColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_header_tx", () -> data.journalHeaderTextColor, v -> data.journalHeaderTextColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_item", () -> data.journalItemBgColor, v -> data.journalItemBgColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_text", () -> data.journalTextColor, v -> data.journalTextColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_desc", () -> data.journalDescColor, v -> data.journalDescColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_dot_act", () -> data.journalDotActiveColor, v -> data.journalDotActiveColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_dot_done", () -> data.journalDotCompletableColor, v -> data.journalDotCompletableColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_track", () -> data.journalScrollTrackColor, v -> data.journalScrollTrackColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_thumb", () -> data.journalScrollThumbColor, v -> data.journalScrollThumbColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_btn", () -> data.journalButtonColor, v -> data.journalButtonColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_btn_hover", () -> data.journalButtonHoverColor, v -> data.journalButtonHoverColor = v);
        y = colorRow(y, fieldW, "gui.api.style.color.jn_btn_text", () -> data.journalButtonTextColor, v -> data.journalButtonTextColor = v);

        contentHeight = y + 10;
        reposition();
    }

    // ===== 行构造 =====

    private int section(int y, String key) {
        labels.add(new Label(key, LEFT_X, y));
        return y + 14;
    }

    private int texRow(int y, int fieldW, String labelKey, String value,
                       java.util.function.Consumer<String> setter, boolean withPicker) {
        labels.add(new Label(labelKey, LEFT_X, y));
        int boxW = withPicker ? fieldW - 40 : fieldW;
        EditBox box = new EditBox(font, LEFT_X, areaTop + y + 10, boxW, 18, Component.empty());
        box.setValue(value == null ? "" : value);
        box.setResponder(setter);
        addRow(box, y + 10);
        if (withPicker) {
            Button pick = Button.builder(Component.literal("..."),
                    b -> minecraft.setScreen(new TexturePickerScreen(this, id -> {
                        box.setValue(id);
                        setter.accept(id);
                    }))).bounds(LEFT_X + fieldW - 36, areaTop + y + 10, 36, 18).build();
            addRow(pick, y + 10);
        }
        return y + 32;
    }

    private int colorRow(int y, int fieldW, String key,
                         java.util.function.IntSupplier get, java.util.function.IntConsumer set) {
        ColorField cf = new ColorField(font, fieldW, key, get, set);
        cf.setBaseY(y);
        colorFields.add(cf);
        return y + 20;
    }

    private void addRow(AbstractWidget w, int baseY) {
        rows.add(new Row(w, baseY));
        addRenderableWidget(w);
    }

    private static String nz(String v) { return v.isEmpty() ? null : v; }

    private void reposition() {
        for (Row r : rows) {
            int y = areaTop + r.baseY() - scrollY;
            r.widget().setY(y);
            boolean vis = y + 18 >= areaTop && y <= areaBottom;
            r.widget().visible = vis;
            r.widget().active = vis;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);

        // 底部按钮等非滚动组件
        for (var c : children()) {
            if (c instanceof AbstractWidget w && !isRowWidget(w)) {
                w.render(g, mx, my, pt);
            }
        }

        // 滚动内容裁剪
        g.enableScissor(0, areaTop, width / 2, areaBottom);

        reposition();

        // 标签
        for (Label l : labels) {
            int ly = areaTop + l.baseY() - scrollY;
            if (ly + 10 >= areaTop && ly <= areaBottom) {
                boolean isSection = l.key().contains(".section.");
                g.drawString(font, Component.translatable(l.key()),
                        l.x(), ly, isSection ? 0xFFFFD700 : 0xFFCCCCCC);
            }
        }
        // 贴图行组件
        for (Row r : rows) if (r.widget().visible) r.widget().render(g, mx, my, pt);
        // 颜色行
        for (ColorField f : colorFields) {
            int fy = areaTop + f.getBaseY() - scrollY;
            f.setPos(LEFT_X, fy);
            f.syncHex();
            if (fy + 18 >= areaTop && fy <= areaBottom) f.render(g, mx, my);
        }

        g.disableScissor();

        // 滚动条
        int visH = areaBottom - areaTop;
        if (contentHeight > visH) {
            int trackX = width / 2 - 6;
            int thumbH = Math.max(16, visH * visH / contentHeight);
            int thumbY = areaTop + (int)((float) scrollY / (contentHeight - visH) * (visH - thumbH));
            g.fill(trackX, areaTop, trackX + 4, areaBottom, 0x33FFFFFF);
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }

        renderPreview(g, mx, my);

        // 调色盘弹层画在最上面
        if (activePopup != null) {
            activePopup.render(g, mx, my);
        }

        super.render(g, mx, my, pt);
    }

    private boolean isRowWidget(AbstractWidget w) {
        for (Row r : rows) if (r.widget() == w) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dy) {
        if (mx < width / 2 && my >= areaTop && my <= areaBottom) {
            int visH = areaBottom - areaTop;
            int maxScroll = Math.max(0, contentHeight - visH);
            scrollY = (int) Math.max(0, Math.min(maxScroll, scrollY - dy * 14));
            reposition();
            return true;
        }
        return super.mouseScrolled(mx, my, dy);
    }

    // 颜色框点击/键盘转发（只对可见的）
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // 优先处理调色盘弹窗
        if (activePopup != null) {
            if (activePopup.isOutside(mx, my)) { activePopup = null; return true; }
            if (activePopup.mouseClicked(mx, my, btn)) return true;
        }
        if (mx < width / 2 && my >= areaTop && my <= areaBottom) {
            for (ColorField f : colorFields) {
                int fy = areaTop + f.getBaseY() - scrollY;
                if (fy + 18 < areaTop || fy > areaBottom) continue;

                // 色块区域：ColorField 内部是 width-100 到 width-100+14
                int swX = f.x + f.width - 100;
                if (mx >= swX && mx <= swX + 14 && my >= fy && my <= fy + 14) {
                    ColorPickerPopup popup = new ColorPickerPopup(font, f.getColor(), f::setColor);
                    int popX = Math.min(swX + 20, width / 2 - popup.width - 4);
                    int popY = Math.min(fy, height - popup.height - 4);
                    popup.setPos(popX, popY);
                    activePopup = popup;
                    return true;
                }

                // hex 框点击
                if (f.mouseClicked(mx, my, btn)) {
                    for (ColorField o : colorFields) o.getHexBox().setFocused(o == f);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (activePopup != null && activePopup.mouseDragged(mx, my, btn)) return true;
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (activePopup != null) {
            if (activePopup.isOutside(mx, my)) activePopup = null;
            else activePopup.mouseReleased();
            return true;
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        for (ColorField f : colorFields)
            if (f.getHexBox().isFocused() && f.keyPressed(k, s, m)) return true;
        return super.keyPressed(k, s, m);
    }

    @Override
    public boolean charTyped(char c, int m) {
        for (ColorField f : colorFields)
            if (f.getHexBox().isFocused() && f.charTyped(c, m)) return true;
        return super.charTyped(c, m);
    }

    // ===== 预览 =====

    private void renderPreview(GuiGraphics g, int mx, int my) {
        int px = width / 2 + 10;
        int pTop = 24;
        int pw = width / 2 - 20;
        int ph = height - 70;

        g.fillGradient(px, pTop, px + pw, pTop + ph, 0xFF3A5F7A, 0xFF1A2F3A);
        g.renderOutline(px, pTop, pw, ph, 0xFF000000);
        g.drawString(font, Component.translatable("gui.api.style.section.preview"),
                px, 12, 0xFFFFFF);

        GuiStyle style = data.toGuiStyle();
        int cx = px + pw / 2;
        int cy = pTop + ph / 2;

        if (previewMode == 0) {
            renderDialoguePreview(g, style, cx, cy);
        } else {
            renderJournalPreview(g, style, cx, cy);
        }
    }

    private void renderDialoguePreview(GuiGraphics g, GuiStyle s, int cx, int cy) {
        int bw = Math.min(s.dialogueBoxWidth, width / 2 - 40);
        int bh = s.dialogueBoxHeight;
        int bx = cx - bw / 2;
        int by = cy - bh / 2;

        if (s.dialogueBgTexture != null) {
            try { g.blit(s.dialogueBgTexture, bx, by, 0, 0, bw, bh, bw, bh); }
            catch (Exception e) { g.fill(bx, by, bx + bw, by + bh, s.dialogueBgColor); }
        } else {
            g.fill(bx, by, bx + bw, by + bh, s.dialogueBgColor);
        }
        g.renderOutline(bx - 1, by - 1, bw + 2, bh + 2, s.dialogueBorderColor);
        // NPC 名字用配置偏移
        g.drawString(font, Component.translatable("gui.api.style.sample.npc"),
                bx + s.dialoguePaddingLeft, by + s.dialogueNameOffsetY, s.dialogueTitleColor);
        // 分割线用配置偏移
        g.fill(bx + s.dialoguePaddingLeft, by + s.dialogueDividerOffsetY,
                bx + bw - s.dialoguePaddingRight, by + s.dialogueDividerOffsetY + 1, s.dialogueDividerColor);
        g.drawString(font, Component.translatable("gui.api.style.sample.line"),
                bx + s.dialoguePaddingLeft, by + s.dialoguePaddingTop, s.dialogueBodyColor);
        String prog = "1/3";
        g.drawString(font, prog, bx + bw - font.width(prog) - s.dialogueProgressOffsetX,
                by + s.dialogueProgressOffsetY, s.dialogueProgressColor);
    }

    private void renderJournalPreview(GuiGraphics g, GuiStyle s, int cx, int cy) {
        int W = 140, H = 200;
        int px = cx - W / 2, py = cy - H / 2;

        if (s.journalBgTexture != null) {
            try { g.blit(s.journalBgTexture, px - 4, py - 6, 0, 0, W + 8, H + 12, W + 8, H + 12); }
            catch (Exception e) {
                g.fill(px - 4, py - 6, px + W + 4, py + H + 6, s.journalFrameColor);
                g.fill(px, py, px + W, py + H, s.journalScreenColor);
            }
        } else {
            g.fill(px - 4, py - 6, px + W + 4, py + H + 6, s.journalFrameColor);
            g.fill(px, py, px + W, py + H, s.journalScreenColor);
        }
        g.fill(px, py, px + W, py + 16, s.journalHeaderBgColor);
        if (s.journalHeaderTexture != null) {
            g.blit(s.journalHeaderTexture, px, py, 0, 0, W, 16, W, 16);
        } else {
            g.fill(px, py, px + W, py + 16, s.journalHeaderBgColor);
        }
        g.drawCenteredString(font, Component.translatable("gui.api.journal.header"),
                px + W / 2, py + 4, s.journalHeaderTextColor);

        int iy = py + 20;
        if (s.journalItemTexture != null) {
            g.blit(s.journalItemTexture, px + 2, iy, 0, 0, W - 4, 22, W - 4, 22);
        } else {
            g.fill(px + 2, iy, px + W - 2, iy + 22, s.journalItemBgColor);
        }
        g.fill(px + 5, iy + 8, px + 10, iy + 13, s.journalDotActiveColor);
        g.drawString(font, Component.translatable("gui.api.style.sample.quest1"),
                px + 14, iy + 7, s.journalTextColor);
        iy += 24;
        if (s.journalItemTexture != null) {
            g.blit(s.journalItemTexture, px + 2, iy, 0, 0, W - 4, 22, W - 4, 22);
        } else {
            g.fill(px + 2, iy, px + W - 2, iy + 22, s.journalItemBgColor);
        }
        g.fill(px + 2, iy, px + W - 2, iy + 22, 0x22FFFFFF);
        g.fill(px + 5, iy + 8, px + 10, iy + 13, s.journalDotCompletableColor);
        g.drawString(font, Component.translatable("gui.api.style.sample.quest2"),
                px + 14, iy + 7, s.journalTextColor);
        iy += 24;
        g.drawString(font, Component.translatable("gui.api.style.sample.desc"),
                px + 8, iy, s.journalDescColor);
        iy += 12;
        g.drawString(font, Component.literal("✔ ").append(
                        Component.translatable("gui.api.style.sample.obj")),
                px + 10, iy, s.journalDotCompletableColor);

        g.fill(px + W - 3, py + 20, px + W - 1, py + H - 4, s.journalScrollTrackColor);
        g.fill(px + W - 3, py + 40, px + W - 1, py + 80, s.journalScrollThumbColor);

        int btnY = py + H - 18;
        if (s.journalButtonTexture != null) {
            try { g.blit(s.journalButtonTexture, px + 2, btnY, 0, 0, W - 4, 16, W - 4, 16); }
            catch (Exception e) { g.fill(px + 2, btnY, px + W - 2, btnY + 16, s.journalButtonColor); }
        } else {
            g.fill(px + 2, btnY, px + W - 2, btnY + 16, s.journalButtonColor);
        }
        g.drawCenteredString(font, Component.translatable("gui.api.journal.view_repeatable"),
                px + W / 2, btnY + 4, s.journalButtonTextColor);
    }

    private void exportJson() {
        try {
            var root = data.toJson();
            File file = new File("style_export_" + System.currentTimeMillis() + ".json");
            try (FileWriter w = new FileWriter(file)) { GSON.toJson(root, w); }
            minecraft.player.sendSystemMessage(
                    Component.translatable("gui.api.editor.export_success", file.getAbsolutePath()));
            onClose();
        } catch (Exception ex) {
            minecraft.player.sendSystemMessage(
                    Component.translatable("gui.api.editor.export_failed", ex.getMessage()));
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}
