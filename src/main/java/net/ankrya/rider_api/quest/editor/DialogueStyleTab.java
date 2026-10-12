package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DialogueStyleTab extends GridLayoutTab {

    public DialogueStyleTab(Screen screen, QuestEditorData data) {
        super(Component.translatable("gui.api.editor.tab.dialogue"));
        var font = screen.getMinecraft().font;

        GridLayout.RowHelper rows = this.layout.rowSpacing(4).createRowHelper(2);

        // 对话
        rows.addChild(new StringWidget(Component.empty(), font));
        rows.addChild(Button.builder(
                Component.translatable("gui.api.editor.btn.edit_dialogues", data.dialogues.size()),
                b -> screen.getMinecraft().setScreen(
                        new DialogueListEditorScreen(screen, data.dialogues))
        ).width(200).build());

        // 打招呼
        rows.addChild(new StringWidget(Component.empty(), font));
        rows.addChild(Button.builder(
                Component.translatable("gui.api.editor.btn.edit_greetings", data.greetings.size()),
                b -> screen.getMinecraft().setScreen(
                        new StringListEditorScreen(screen, "Greetings", data.greetings))
        ).width(200).build());

        // GUI Style ID
        rows.addChild(new StringWidget(Component.translatable("gui.api.editor.field.gui_style"), font));
        EditBox styleBox = new EditBox(font, 0, 0, 200, 20, Component.literal("gui_style"));
        styleBox.setValue(data.guiStyleId != null ? data.guiStyleId : "");
        styleBox.setResponder(v -> data.guiStyleId = v.isEmpty() ? null : v);
        rows.addChild(styleBox);

        // Accept Message Key
        rows.addChild(new StringWidget(Component.translatable("gui.api.editor.field.accept_msg"), font));
        EditBox acceptBox = new EditBox(font, 0, 0, 200, 20, Component.literal("accept_message"));
        acceptBox.setValue(data.acceptMessageKey != null ? data.acceptMessageKey : "");
        acceptBox.setResponder(v -> data.acceptMessageKey = v.isEmpty() ? null : v);
        rows.addChild(acceptBox);

        // Complete Message Key
        rows.addChild(new StringWidget(Component.translatable("gui.api.editor.field.complete_msg"), font));
        EditBox completeBox = new EditBox(font, 0, 0, 200, 20, Component.literal("complete_message"));
        completeBox.setValue(data.completeMessageKey != null ? data.completeMessageKey : "");
        completeBox.setResponder(v -> data.completeMessageKey = v.isEmpty() ? null : v);
        rows.addChild(completeBox);
    }
}