package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ObjectiveListEditorScreen extends Screen {

    private final Screen parent;
    private final List<QuestEditorData.ObjectiveData> list;
    private ObjListWidget listWidget;

    public ObjectiveListEditorScreen(Screen parent, List<QuestEditorData.ObjectiveData> list) {
        super(Component.translatable("gui.api.editor.objectives.title"));
        this.parent = parent;
        this.list = list;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.objectives.add_kill"),
                b -> {
                    var obj = new QuestEditorData.ObjectiveData();
                    obj.type = "kill";
                    obj.id = "kill_" + System.currentTimeMillis();
                    obj.label = "Kill something";
                    obj.target = "minecraft:zombie";
                    obj.count = 5;
                    list.add(obj);
                    listWidget.refresh();
                }).bounds(width / 2 - 210, 30, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.objectives.add_collect"),
                b -> {
                    var obj = new QuestEditorData.ObjectiveData();
                    obj.type = "collect";
                    obj.id = "collect_" + System.currentTimeMillis();
                    obj.label = "Collect something";
                    obj.target = "minecraft:diamond";
                    obj.count = 3;
                    list.add(obj);
                    listWidget.refresh();
                }).bounds(width / 2 - 105, 30, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.objectives.add_dialogue"),
                b -> {
                    var obj = new QuestEditorData.ObjectiveData();
                    obj.type = "dialogue";
                    obj.id = "dialogue_" + System.currentTimeMillis();
                    obj.label = "Talk to NPC";
                    obj.target = "yourmod:npc_id";
                    obj.count = 1;
                    list.add(obj);
                    listWidget.refresh();
                }).bounds(width / 2, 30, 100, 20).build());

        listWidget = new ObjListWidget();
        addRenderableWidget(listWidget);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
    }

    private class ObjListWidget extends ObjectSelectionList<ObjListWidget.Entry> {
        ObjListWidget() {
            super(ObjectiveListEditorScreen.this.minecraft, ObjectiveListEditorScreen.this.width, ObjectiveListEditorScreen.this.height - 100, 60, (60) + (ObjectiveListEditorScreen.this.height - 100), 30);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            list.forEach(o -> addEntry(new Entry(o)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final QuestEditorData.ObjectiveData obj;
            Entry(QuestEditorData.ObjectiveData o) { obj = o; }

            @Override
            public Component getNarration() { return Component.literal(obj.type); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                String line = obj.type + ": " + obj.label + " [" + obj.target + "] x" + obj.count;
                g.drawString(font, line, left + 4, top + 6, 0xFFFFFF);
                if (hover && mx > left + w - 100 && mx < left + w - 50) {
                    g.fill(left + w - 100, top, left + w - 50, top + h, 0x5544FF44);
                    g.drawCenteredString(font, Component.translatable("gui.api.editor.btn.edit").getString(), left + w - 75, top + 11, 0xFFFFFF);
                }
                if (hover && mx > left + w - 50) {
                    g.fill(left + w - 50, top, left + w, top + h, 0x55FF4444);
                    g.drawCenteredString(font, Component.translatable("gui.api.editor.btn.remove").getString(), left + w - 25, top + 11, 0xFFFFFF);
                }
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                if (mx > getRowRight() - 50) {
                    ObjectiveListEditorScreen.this.list.remove(obj);
                    refresh();
                    return true;
                }
                if (mx > getRowRight() - 100 && mx < getRowRight() - 50) {
                    minecraft.setScreen(new ObjectiveDetailEditorScreen(
                            ObjectiveListEditorScreen.this, obj));
                    return true;
                }
                return false;
            }
        }
    }
}