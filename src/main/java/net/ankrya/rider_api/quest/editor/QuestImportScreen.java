package net.ankrya.rider_api.quest.editor;

import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuestImportScreen extends Screen {
    private final Screen parent;
    private final QuestEditorData target;
    private ImportList list;

    public QuestImportScreen(Screen parent, QuestEditorData target) {
        super(Component.translatable("gui.api.editor.import_title"));
        this.parent = parent;
        this.target = target;
    }

    @Override
    protected void init() {
        list = new ImportList();
        addRenderableWidget(list);
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.cancel"),
                b -> minecraft.setScreen(parent)).bounds(width / 2 - 50, height - 28, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
    }


    private class ImportList extends ObjectSelectionList<ImportList.Entry> {
        ImportList() {
            super(QuestImportScreen.this.minecraft, QuestImportScreen.this.width, QuestImportScreen.this.height - 80, 40, (40) + (QuestImportScreen.this.height - 80), 20);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            QuestRegistry.INSTANCE.all().forEach(def -> addEntry(new Entry(def.id().toString())));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final String id;
            Entry(String id) { this.id = id; }

            @Override
            public Component getNarration() { return Component.literal(id); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                if (hover) g.fill(left, top, left + w, top + h, 0x33FFFFFF);
                g.drawString(font, id, left + 4, top + 6, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                ResourceLocation rl = new ResourceLocation(id);
                QuestRegistry.INSTANCE.getRaw(rl).ifPresent(json ->
                        target.copyFrom(QuestEditorData.fromJson(json)));
                minecraft.setScreen(parent);
                return true;
            }
        }
    }
}
