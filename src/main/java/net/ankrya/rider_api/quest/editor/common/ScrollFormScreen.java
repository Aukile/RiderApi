package net.ankrya.rider_api.quest.editor.common;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/** 可滚动的"标签 + 字段"表单基类，供波次/怪物详情编辑复用 */
public abstract class ScrollFormScreen extends Screen {

    protected final Screen parent;

    private record Row(AbstractWidget widget, int baseY) {}
    private record Lbl(Component text, int baseY) {}

    private final List<Row> rows = new ArrayList<>();
    private final List<Lbl> labels = new ArrayList<>();

    protected int scroll = 0;
    protected int contentHeight = 0;
    protected int areaTop, areaBottom;
    /** 当前行游标 */
    protected int cursorY = 0;

    protected static final int STEP = 24;
    protected static final int LABEL_X = 30;
    protected static final int FIELD_X = 210;
    protected static final int FIELD_W = 180;

    protected ScrollFormScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    /** 子类在这里调用 label()/editBox()/checkbox()/button() 构建表单 */
    protected abstract void buildForm();

    @Override
    protected void init() {
        rows.forEach(r -> removeWidget(r.widget()));
        rows.clear();
        labels.clear();
        areaTop = 40;
        areaBottom = height - 44;
        cursorY = 0;

        buildForm();
        contentHeight = cursorY + 10;

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 30, 100, 20).build());

        reposition();
    }

    // ===== 表单构建辅助 =====

    protected int absY(int baseY) { return areaTop + baseY - scroll; }

    protected void label(String key) {
        labels.add(new Lbl(Component.translatable(key), cursorY));
    }

    protected EditBox editBox(String value, Consumer<String> resp) {
        EditBox box = new EditBox(font, FIELD_X, absY(cursorY), FIELD_W, 20, Component.empty());
        box.setMaxLength(2048);
        box.setValue(value == null ? "" : value);
        box.setResponder(resp);
        addRow(box);
        return box;
    }

    protected void editBoxInt(int value, IntConsumer resp) {
        EditBox box = new EditBox(font, FIELD_X, absY(cursorY), FIELD_W, 20, Component.empty());
        box.setValue(String.valueOf(value));
        box.setResponder(v -> { try { resp.accept(Integer.parseInt(v.trim())); } catch (Exception ignored) {} });
        addRow(box);
    }

    /** 空 = -1（不修改） */
    protected void editBoxDouble(double value, DoubleConsumer resp) {
        EditBox box = new EditBox(font, FIELD_X, absY(cursorY), FIELD_W, 20, Component.empty());
        box.setValue(value < 0 ? "" : String.valueOf(value));
        box.setHint(Component.translatable("gui.api.wave.unchanged"));
        box.setResponder(v -> {
            if (v.trim().isEmpty()) { resp.accept(-1); return; }
            try { resp.accept(Double.parseDouble(v.trim())); } catch (Exception ignored) {}
        });
        addRow(box);
    }

    protected void checkbox(String key, boolean selected, Consumer<Boolean> resp) {
        addRow(Checkboxes.create(FIELD_X, absY(cursorY), Component.translatable(key), font, selected, v -> resp.accept(v)));
    }

    protected void button(String key, Button.OnPress onPress) {
        addRow(Button.builder(Component.translatable(key), onPress)
                .bounds(FIELD_X, absY(cursorY), FIELD_W, 20).build());
    }

    protected void button(Component text, Button.OnPress onPress) {
        addRow(Button.builder(text, onPress)
                .bounds(FIELD_X, absY(cursorY), FIELD_W, 20).build());
    }

    /** 同一行再加一个窄组件（不换行） */
    protected void sameLineButton(Component text, int x, int w, Button.OnPress onPress) {
        rows.add(new Row(Button.builder(text, onPress)
                .bounds(x, absY(cursorY - STEP), w, 20).build(), cursorY - STEP));
        addRenderableWidget(rows.get(rows.size() - 1).widget());
    }

    private void addRow(AbstractWidget w) {
        rows.add(new Row(w, cursorY));
        addRenderableWidget(w);
        cursorY += STEP;
    }

    private void reposition() {
        for (Row r : rows) {
            r.widget().setY(areaTop + r.baseY() - scroll);
            boolean vis = r.widget().getY() + 20 >= areaTop && r.widget().getY() <= areaBottom;
            r.widget().visible = vis;
            r.widget().active = vis;
        }
    }

    private boolean isContent(AbstractWidget w) {
        for (Row r : rows) if (r.widget() == w) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dy) {
        if (my >= areaTop && my <= areaBottom) {
            int visH = areaBottom - areaTop;
            int maxScroll = Math.max(0, contentHeight - visH);
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - dy * 14));
            reposition();
            return true;
        }
        return super.mouseScrolled(mx, my, dy);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        for (var child : children())
            if (child instanceof AbstractWidget w && !isContent(w)) w.render(g, mx, my, pt);

        g.enableScissor(0, areaTop, width, areaBottom);
        for (Lbl l : labels) {
            int ly = areaTop + l.baseY() - scroll;
            if (ly + 12 >= areaTop && ly <= areaBottom)
                g.drawString(font, l.text(), LABEL_X, ly + 6, 0xFFCCCCCC);
        }
        for (Row r : rows) if (r.widget().visible) r.widget().render(g, mx, my, pt);
        g.disableScissor();

        int visH = areaBottom - areaTop;
        if (contentHeight > visH) {
            int trackX = width - 8;
            int thumbH = Math.max(16, visH * visH / contentHeight);
            int thumbY = areaTop + (int)((float) scroll / (contentHeight - visH) * (visH - thumbH));
            g.fill(trackX, areaTop, trackX + 4, areaBottom, 0x33FFFFFF);
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}