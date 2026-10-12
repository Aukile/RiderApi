package net.ankrya.rider_api.help.advancement;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CriteriaListScreen extends Screen {

    private final Screen parent;
    private final AdvancementEditorData data;
    private CritList list;

    public CriteriaListScreen(Screen parent, AdvancementEditorData data) {
        super(Component.translatable("gui.api.adv.criteria_title"));
        this.parent = parent;
        this.data = data;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.add"),
                b -> {
                    var c = new AdvancementEditorData.Criterion();
                    c.name = "criterion_" + data.criteria.size();
                    data.criteria.add(c);
                    list.refresh();
                }).bounds(width / 2 - 50, 30, 100, 20).build());

        list = new CritList();
        addRenderableWidget(list);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
    }


    private class CritList extends ObjectSelectionList<CritList.Entry> {
        CritList() {
            super(CriteriaListScreen.this.minecraft, CriteriaListScreen.this.width, CriteriaListScreen.this.height - 100, 60, (60) + (CriteriaListScreen.this.height - 100), 22);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }
        void refresh() {
            clearEntries();
            data.criteria.forEach(c -> addEntry(new Entry(c)));
        }
        class Entry extends ObjectSelectionList.Entry<Entry> {
            final AdvancementEditorData.Criterion crit;
            Entry(AdvancementEditorData.Criterion c) { crit = c; }
            @Override public Component getNarration() { return Component.literal(crit.name); }
            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                g.drawString(font, crit.name + "  [" + crit.trigger + "]", left + 4, top + 6, 0xFFFFFF);
                if (hover && mx > left + w - 100 && mx < left + w - 50) {
                    g.fill(left + w - 100, top, left + w - 50, top + h, 0x5544FF44);
                    g.drawCenteredString(font, Component.translatable("gui.api.editor.btn.edit").getString(),
                            left + w - 75, top + 7, 0xFFFFFF);
                }
                if (hover && mx > left + w - 50) {
                    g.fill(left + w - 50, top, left + w, top + h, 0x55FF4444);
                    g.drawCenteredString(font, Component.translatable("gui.api.editor.btn.remove").getString(),
                            left + w - 25, top + 7, 0xFFFFFF);
                }
            }
            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                if (mx > getRowRight() - 50) {
                    data.criteria.remove(crit);
                    refresh();
                    return true;
                }
                if (mx > getRowRight() - 100 && mx < getRowRight() - 50) {
                    minecraft.setScreen(new CriterionDetailScreen(CriteriaListScreen.this, crit));
                    return true;
                }
                return false;
            }
        }
    }
}