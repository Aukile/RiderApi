package net.ankrya.rider_api.quest.editor.wave;

import net.ankrya.rider_api.quest.editor.QuestEditorData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WaveListEditorScreen extends Screen {

    private final Screen parent;
    private final QuestEditorData data;
    private WaveList listWidget;

    public WaveListEditorScreen(Screen parent, QuestEditorData data) {
        super(Component.translatable("gui.api.wave.list.title"));
        this.parent = parent;
        this.data = data;
    }

    private List<QuestEditorData.WaveData> waves() { return data.waves; }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.api.wave.list.add"),
                b -> {
                    var w = new QuestEditorData.WaveData();
                    w.id = "wave_" + (waves().size() + 1);
                    if (!waves().isEmpty()) w.trigger = "after_previous_cleared";
                    waves().add(w);
                    listWidget.refresh();
                }).bounds(width / 2 - 50, 28, 100, 20).build());

        listWidget = new WaveList();
        addRenderableWidget(listWidget);

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


    private class WaveList extends ObjectSelectionList<WaveList.Entry> {
        WaveList() {
            super(WaveListEditorScreen.this.minecraft, WaveListEditorScreen.this.width, WaveListEditorScreen.this.height - 100, 56, (56) + (WaveListEditorScreen.this.height - 100), 26);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            waves().forEach(w -> addEntry(new Entry(w)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final QuestEditorData.WaveData wave;
            Entry(QuestEditorData.WaveData w) { wave = w; }

            @Override public Component getNarration() { return Component.literal(wave.id); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                int mobs = wave.mobs.stream().mapToInt(m -> m.count).sum();
                g.drawString(font, "#" + (idx + 1) + "  " + wave.id, left + 4, top + 3, 0xFFFFFF);

                Component sub = Component.empty()
                        .append(WaveDetailEditorScreen.triggerName(wave.trigger))
                        .append(Component.literal(
                                (wave.delayTicks > 0 ? " +" + wave.delayTicks + "t" : "")
                                        + "  mobs:" + mobs
                                        + (wave.showBossBar ? "  [bar]" : "")));
                g.drawString(font, sub, left + 4, top + 14, 0xFF888888);

                if (hover) {
                    int right = left + w;
                    drawMini(g, right - 132, top, h, "▲", mx, my);
                    drawMini(g, right - 110, top, h, "▼", mx, my);
                    if (mx > right - 88 && mx < right - 48)
                        g.fill(right - 88, top, right - 48, top + h, 0x5544FF44);
                    g.drawCenteredString(font,
                            Component.translatable("gui.api.editor.btn.edit").getString(),
                            right - 68, top + 9, 0xFFFFFF);
                    if (mx > right - 48) g.fill(right - 48, top, right, top + h, 0x55FF4444);
                    g.drawCenteredString(font,
                            Component.translatable("gui.api.editor.btn.remove").getString(),
                            right - 24, top + 9, 0xFFFFFF);
                }
            }

            private void drawMini(GuiGraphics g, int x, int top, int h, String s, int mx, int my) {
                if (mx >= x && mx < x + 20 && my >= top && my < top + h)
                    g.fill(x, top, x + 20, top + h, 0x5544AAFF);
                g.drawCenteredString(font, s, x + 10, top + 9, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                int right = getRowRight();
                int idx = waves().indexOf(wave);
                if (mx > right - 132 && mx < right - 112) {
                    if (idx > 0) { waves().remove(idx); waves().add(idx - 1, wave); refresh(); }
                    return true;
                }
                if (mx > right - 110 && mx < right - 90) {
                    if (idx < waves().size() - 1) { waves().remove(idx); waves().add(idx + 1, wave); refresh(); }
                    return true;
                }
                if (mx > right - 88 && mx < right - 48) {
                    minecraft.setScreen(new WaveDetailEditorScreen(
                            WaveListEditorScreen.this, data, wave));
                    return true;
                }
                if (mx > right - 48) { waves().remove(wave); refresh(); return true; }
                return false;
            }
        }
    }
}