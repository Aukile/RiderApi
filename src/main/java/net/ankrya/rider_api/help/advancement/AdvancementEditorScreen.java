package net.ankrya.rider_api.help.advancement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ankrya.rider_api.quest.editor.ItemPickerScreen;
import net.ankrya.rider_api.quest.editor.common.Checkboxes;
import net.ankrya.rider_api.quest.editor.common.SuggestListEditorScreen;
import net.ankrya.rider_api.quest.editor.style.TexturePickerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class AdvancementEditorScreen extends Screen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private enum Page { DISPLAY, CRITERIA, REWARDS }

    private final AdvancementEditorData data = new AdvancementEditorData();
    private Page page = Page.DISPLAY;

    // 内容区所有组件 + 各自逻辑Y（未滚动时的相对Y）
    private record Row(AbstractWidget widget, int baseY) {}
    private final List<Row> rows = new ArrayList<>();
    // 只画文字不交互的标签
    private record Label(Component text, int x, int baseY) {}
    private final List<Label> labels = new ArrayList<>();

    private int scroll = 0;
    private int contentHeight = 0;

    private int areaTop, areaBottom;
    private static final int STEP = 24;
    private static final int LABEL_X = 30;
    private static final int FIELD_X = 170;
    private static final int FIELD_W = 200;

    public AdvancementEditorScreen() {
        super(Component.translatable("gui.api.adv.title"));
    }

    @Override
    protected void init() {
        areaTop = 48;
        areaBottom = height - 60;

        // 顶部页签按钮
        addPageButton(width / 2 - 180, Page.DISPLAY,  "gui.api.adv.tab.display");
        addPageButton(width / 2 - 60,  Page.CRITERIA, "gui.api.adv.tab.criteria");
        addPageButton(width / 2 + 60,  Page.REWARDS,  "gui.api.adv.tab.rewards");

        // 底部按钮
//        addRenderableWidget(Button.builder(
//                Component.translatable("gui.api.editor.import"),
//                b -> minecraft.setScreen(new AdvImportScreen(this, data))
//        ).bounds(width / 2 - 205, height - 30, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.api.editor.export"),
                b -> exportJson()
        ).bounds(width / 2 - 100, height - 30, 200, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.api.editor.btn.done"),
                b -> onClose()
        ).bounds(width / 2 + 105, height - 30, 100, 20).build());

        rebuildContent();
    }

    private void addPageButton(int x, Page p, String key) {
        addRenderableWidget(Button.builder(Component.translatable(key), b -> {
            page = p;
            scroll = 0;
            rebuildContent();
        }).bounds(x, 20, 120, 20).build());
    }

    /** 切页时重建内容区组件 */
    private void rebuildContent() {
        // 先移除旧内容组件
        for (Row r : rows) removeWidget(r.widget());
        rows.clear();
        labels.clear();
        scroll = 0;

        int y = 0;
        switch (page) {
            case DISPLAY  -> y = buildDisplay();
            case CRITERIA -> y = buildCriteria();
            case REWARDS  -> y = buildRewards();
        }
        contentHeight = y;
        reposition();
    }

    // ===== Display 页 =====
    private int buildDisplay() {
        int y = 0;
        var f = font;

        label("gui.api.adv.icon", y);
        EditBox iconBox = editBox(y, data.iconItem, v -> data.iconItem = v);
        y += STEP;
        button(y, "gui.api.adv.pick_icon", b ->
                minecraft.setScreen(new ItemPickerScreen(this, id -> {
                    data.iconItem = id; iconBox.setValue(id);
                })));
        y += STEP;

        label("gui.api.adv.title_field", y);
        editBox(y, data.titleKey, v -> data.titleKey = v);
        y += STEP;

        label("gui.api.adv.desc_field", y);
        editBox(y, data.descKey, v -> data.descKey = v);
        y += STEP;

        label("gui.api.adv.frame", y);
        addRow(CycleButton.<AdvancementEditorData.Frame>builder(
                        fr -> Component.translatable("gui.api.adv.frame." + fr.name().toLowerCase()))
                .withValues(AdvancementEditorData.Frame.values())
                .withInitialValue(data.frame)
                .create(FIELD_X, absY(y), FIELD_W, 20,
                        Component.translatable("gui.api.adv.frame"),
                        (btn, val) -> data.frame = val), y);
        y += STEP;

        label("gui.api.adv.parent", y);
        editBox(y, data.parent == null ? "" : data.parent,
                v -> data.parent = v.isEmpty() ? null : v);
        y += STEP;

        label("gui.api.adv.background", y);
        EditBox bgBox = editBox(y, data.backgroundTexture == null ? "" : data.backgroundTexture,
                v -> data.backgroundTexture = v.isEmpty() ? null : v);
        y += STEP;
        button(y, "gui.api.adv.pick_bg", b ->
                minecraft.setScreen(new TexturePickerScreen(this, id -> {
                    data.backgroundTexture = id; bgBox.setValue(id);
                })));
        y += STEP;

        addRow(Checkboxes.create(FIELD_X, absY(y), Component.translatable("gui.api.adv.show_toast"), f, data.showToast, v -> data.showToast = v), y);
        y += STEP;
        addRow(Checkboxes.create(FIELD_X, absY(y), Component.translatable("gui.api.adv.announce"), f, data.announceToChat, v -> data.announceToChat = v), y);
        y += STEP;
        addRow(Checkboxes.create(FIELD_X, absY(y), Component.translatable("gui.api.adv.hidden"), f, data.hidden, v -> data.hidden = v), y);
        y += STEP;

        return y + 10;
    }

    // ===== Criteria 页 =====
    private int buildCriteria() {
        int y = 0;
        button(y, FIELD_W + 20, "gui.api.adv.edit_criteria_n", b ->
                        minecraft.setScreen(new CriteriaListScreen(this, data)),
                data.criteria.size());
        y += STEP;
        button(y, FIELD_W + 20, "gui.api.adv.edit_requirements_n", b ->
                        minecraft.setScreen(new RequirementsScreen(this, data)),
                data.requirements.size());
        y += STEP;
        return y + 10;
    }

    // ===== Rewards 页 =====
    private int buildRewards() {
        int y = 0;

        label("gui.api.adv.reward_exp", y);
        editBox(y, String.valueOf(data.rewardExperience), v -> {
            try { data.rewardExperience = Integer.parseInt(v); } catch (Exception ignored) {}
        });
        y += STEP;

        label("gui.api.adv.reward_func", y);
        editBox(y, data.rewardFunction == null ? "" : data.rewardFunction,
                v -> data.rewardFunction = v.isEmpty() ? null : v);
        y += STEP;

        // 配方：带补全选择
        button(y, FIELD_W + 20, "gui.api.adv.reward_recipes_n", b ->
                        minecraft.setScreen(new SuggestListEditorScreen(this,
                                Component.translatable("gui.api.adv.reward_recipes_title"),
                                data.rewardRecipes, SuggestionSources.recipes())),
                data.rewardRecipes.size());
        y += STEP;

        // 战利品表：带补全选择
        button(y, FIELD_W + 20, "gui.api.adv.reward_loot_n", b ->
                        minecraft.setScreen(new SuggestListEditorScreen(this,
                                Component.translatable("gui.api.adv.reward_loot_title"),
                                data.rewardLoot, SuggestionSources.lootTables())),
                data.rewardLoot.size());
        y += STEP;

        return y + 10;
    }

    // ===== 组件构造辅助 =====

    private int absY(int baseY) { return areaTop + baseY - scroll; }

    private void label(String key, int baseY) {
        labels.add(new Label(Component.translatable(key), LABEL_X, baseY));
    }

    private EditBox editBox(int baseY, String value, java.util.function.Consumer<String> resp) {
        EditBox box = new EditBox(font, FIELD_X, absY(baseY), FIELD_W, 20, Component.empty());
        box.setValue(value);
        box.setResponder(resp);
        addRow(box, baseY);
        return box;
    }

    private void button(int baseY, String key, Button.OnPress onPress) {
        button(baseY, FIELD_W, key, onPress);
    }

    private void button(int baseY, int w, String key, Button.OnPress onPress) {
        addRow(Button.builder(Component.translatable(key), onPress)
                .bounds(FIELD_X, absY(baseY), w, 20).build(), baseY);
    }

    private void button(int baseY, int w, String key, Button.OnPress onPress, Object arg) {
        addRow(Button.builder(Component.translatable(key, arg), onPress)
                .bounds(FIELD_X, absY(baseY), w, 20).build(), baseY);
    }

    private void addRow(AbstractWidget w, int baseY) {
        rows.add(new Row(w, baseY));
        addRenderableWidget(w);
    }

    /** 滚动时重新定位所有内容组件的 Y */
    private void reposition() {
        for (Row r : rows) {
            r.widget().setY(areaTop + r.baseY() - scroll);
            // 超出可视区的组件禁用可见（避免它们越界响应/绘制）
            boolean vis = r.widget().getY() + 20 >= areaTop
                    && r.widget().getY() <= areaBottom;
            r.widget().visible = vis;
            r.widget().active = vis;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        g.drawCenteredString(font, title, width / 2, 6, 0xFFFFFF);

        // 1. 先画非内容组件（页签、底部按钮），不裁剪
        for (var child : children()) {
            if (child instanceof AbstractWidget w && !isContentWidget(w)) {
                w.render(g, mx, my, pt);
            }
        }

        // 2. 裁剪区内画内容组件 + 标签
        g.enableScissor(0, areaTop, width, areaBottom);
        for (Label l : labels) {
            int ly = areaTop + l.baseY() - scroll;
            if (ly + 12 >= areaTop && ly <= areaBottom)
                g.drawString(font, l.text(), l.x(), ly + 5, 0xFFCCCCCC);
        }
        for (Row r : rows) {
            if (r.widget().visible) r.widget().render(g, mx, my, pt);
        }
        g.disableScissor();

        // 3. 滚动条
        int visH = areaBottom - areaTop;
        if (contentHeight > visH) {
            int trackX = width - 8;
            int thumbH = Math.max(16, visH * visH / contentHeight);
            int thumbY = areaTop + (int)((float) scroll / (contentHeight - visH) * (visH - thumbH));
            g.fill(trackX, areaTop, trackX + 4, areaBottom, 0x33FFFFFF);
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    private boolean isContentWidget(AbstractWidget w) {
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

    private void exportJson() {
        try {
            var root = data.toJson();
            File file = new File("advancement_export_" + System.currentTimeMillis() + ".json");
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