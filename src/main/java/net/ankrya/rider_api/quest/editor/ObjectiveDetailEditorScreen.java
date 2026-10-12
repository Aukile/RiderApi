package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ObjectiveDetailEditorScreen extends Screen {

    private final Screen parent;
    private final QuestEditorData.ObjectiveData obj;

    public ObjectiveDetailEditorScreen(Screen parent, QuestEditorData.ObjectiveData obj) {
        super(Component.translatable("gui.api.editor.objective.title"));
        this.parent = parent;
        this.obj = obj;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 40;

        // ID
        EditBox idBox = new EditBox(font, cx - 100, y, 200, 20, Component.literal("id"));
        idBox.setValue(obj.id);
        idBox.setResponder(v -> obj.id = v);
        addRenderableWidget(idBox);
        y += 26;

        // Label (翻译键)
        EditBox labelBox = new EditBox(font, cx - 100, y, 200, 20, Component.literal("label"));
        labelBox.setValue(obj.label);
        labelBox.setResponder(v -> obj.label = v);
        addRenderableWidget(labelBox);
        y += 26;

        // Target：根据类型不同，用不同的选择器
        EditBox targetBox = new EditBox(font, cx - 100, y, 160, 20, Component.literal("target"));
        targetBox.setValue(obj.target);
        targetBox.setResponder(v -> obj.target = v);
        addRenderableWidget(targetBox);

        // 打开选择器按钮
        addRenderableWidget(Button.builder(Component.literal("..."),
                b -> {
                    if (obj.type.equals("collect")) {
                        // 物品选择器（带图标）
                        minecraft.setScreen(new ItemPickerScreen(this, id -> {
                            obj.target = id;
                            targetBox.setValue(id);
                        }));
                    } else {
                        // 实体/NPC 选择器
                        minecraft.setScreen(new EntityPickerScreen(this, id -> {
                            obj.target = id;
                            targetBox.setValue(id);
                        }));
                    }
                }).bounds(cx + 65, y, 35, 20).build());
        y += 26;

        // Count（对话类型固定为 1，不显示）
        if (!obj.type.equals("dialogue")) {
            EditBox countBox = new EditBox(font, cx - 100, y, 200, 20, Component.literal("count"));
            countBox.setValue(String.valueOf(obj.count));
            countBox.setResponder(v -> {
                try { obj.count = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(countBox);
        }

        // Done
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(cx - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        int cx = width / 2;
        g.drawCenteredString(font, Component.translatable("gui.api.editor.field.type").getString() + ": " + obj.type, cx, 20, 0xFFD700);
        g.drawString(font, Component.translatable("gui.api.editor.field.id").getString(),     cx - 145, 46, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.api.editor.field.label").getString(),  cx - 145, 72, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.api.editor.field.target").getString(), cx - 145, 98, 0xAAAAAA);
        if (!obj.type.equals("dialogue"))
            g.drawString(font, Component.translatable("gui.api.editor.field.count").getString(), cx - 145, 124, 0xAAAAAA);
    }
}