package net.ankrya.rider_api.quest.editor;

import net.ankrya.rider_api.quest.editor.common.Checkboxes;
import net.ankrya.rider_api.quest.editor.common.SuggestListEditorScreen;
import net.ankrya.rider_api.quest.editor.translation.TranslationEditorScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BasicInfoTab extends GridLayoutTab {

    public BasicInfoTab(Screen screen, QuestEditorData data) {
        super(Component.translatable("gui.api.editor.tab.basic"));
        var font = screen.getMinecraft().font;
        GridLayout.RowHelper rows = this.layout.rowSpacing(4).createRowHelper(2);

        // Title：左文本框 + 右"设置键"按钮
        rows.addChild(new StringWidget(Component.translatable("gui.api.editor.field.title"), font));
        rows.addChild(keyRow(screen, font, data.title, v -> data.title = v));

        // Description
        rows.addChild(new StringWidget(Component.translatable("gui.api.editor.field.desc"), font));
        rows.addChild(keyRow(screen, font, data.description, v -> data.description = v));

        // NPC ID + 选择实体
        rows.addChild(new StringWidget(Component.translatable("gui.api.editor.field.npc_id"), font));
        EditBox npcBox = new EditBox(font, 0, 0, 160, 20, Component.empty());
        npcBox.setValue(data.npcId != null ? data.npcId : "");
        npcBox.setResponder(v -> data.npcId = v.isEmpty() ? null : v);
        Button npcPick = Button.builder(Component.literal("..."),
                b -> screen.getMinecraft().setScreen(new EntityPickerScreen(screen, id -> {
                    data.npcId = id;
                    npcBox.setValue(id);
                }))).width(20).build();
        // 1.20 的 LinearLayout 没有 horizontal()/spacing()，用单行 GridLayout 代替
        GridLayout npcRow = new GridLayout().columnSpacing(4);
        npcRow.addChild(npcBox, 0, 0);
        npcRow.addChild(npcPick, 0, 1);
        rows.addChild(npcRow);

        // Require NPC Dialogue
        rows.addChild(new StringWidget(Component.empty(), font));
        rows.addChild(Checkboxes.create(0, 0, Component.translatable("gui.api.editor.field.require_npc"), font, data.requireNpcDialogue, val -> data.requireNpcDialogue = val));

        // Auto Start
        rows.addChild(new StringWidget(Component.empty(), font));
        rows.addChild(Checkboxes.create(0, 0, Component.translatable("gui.api.editor.field.auto_start"), font, data.autoStart, val -> data.autoStart = val));

        // Repeatable
        rows.addChild(new StringWidget(Component.empty(), font));
        rows.addChild(Checkboxes.create(0, 0, Component.translatable("gui.api.editor.field.repeatable"), font, data.repeatable, val -> data.repeatable = val));

        // 父任务
        rows.addChild(new StringWidget(Component.empty(), font));
        rows.addChild(Button.builder(Component.translatable("gui.api.editor.btn.edit_parents"),
                b -> screen.getMinecraft().setScreen(
                        new SuggestListEditorScreen(screen,
                                Component.translatable("gui.api.editor.list.parents"),
                                data.parents,
                                QuestSuggestionSource.allQuestIds()))
        ).width(200).build());
    }

    /**
     * 生成"文本框(显示/编辑键) + 设置键按钮"的横向布局。
     * 文本框直接编辑键名，按钮打开翻译弹窗编辑键的内容。
     */
    private GridLayout keyRow(Screen screen, net.minecraft.client.gui.Font font,
                                String initial, java.util.function.Consumer<String> setter) {
        // 左：显示并可直接编辑键名
        EditBox keyBox = new EditBox(font, 0, 0, 150, 20, Component.empty());
        keyBox.setValue(initial == null ? "" : initial);
        keyBox.setResponder(v -> setter.accept(v));

        // 右：打开翻译弹窗设置该键的内容
        Button setBtn = Button.builder(Component.translatable("gui.api.editor.set_key"),
                b -> screen.getMinecraft().setScreen(
                        new TranslationEditorScreen(screen, keyBox.getValue(), key -> {
                            keyBox.setValue(key);   // 弹窗保存后回填键名
                            setter.accept(key);
                        }))).width(46).build();

        GridLayout row = new GridLayout().columnSpacing(4);
        row.addChild(keyBox, 0, 0);
        row.addChild(setBtn, 0, 1);
        return row;
    }
}