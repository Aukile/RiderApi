package net.ankrya.rider_api.quest.editor.wave;

import net.ankrya.rider_api.quest.editor.QuestEditorData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WaveMobListEditorScreen extends Screen {

    private final Screen parent;
    private final QuestEditorData.WaveData wave;
    private MobList listWidget;

    public WaveMobListEditorScreen(Screen parent, QuestEditorData.WaveData wave) {
        super(Component.translatable("gui.api.wave.mobs.title"));
        this.parent = parent;
        this.wave = wave;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.api.wave.mobs.add"),
                b -> {
                    wave.mobs.add(new QuestEditorData.WaveMobData());
                    listWidget.refresh();
                }).bounds(width / 2 - 50, 28, 100, 20).build());

        listWidget = new MobList();
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


    private class MobList extends ObjectSelectionList<MobList.Entry> {
        MobList() {
            super(WaveMobListEditorScreen.this.minecraft, WaveMobListEditorScreen.this.width, WaveMobListEditorScreen.this.height - 100, 56, (56) + (WaveMobListEditorScreen.this.height - 100), 26);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            wave.mobs.forEach(m -> addEntry(new Entry(m)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final QuestEditorData.WaveMobData mob;
            Entry(QuestEditorData.WaveMobData m) { mob = m; }

            @Override public Component getNarration() { return Component.literal(mob.entity); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                g.drawString(font, mob.entity + " x" + mob.count, left + 4, top + 3, 0xFFFFFF);
                StringBuilder sb = new StringBuilder();
                if (mob.maxHealth >= 0) sb.append("HP:").append((int) mob.maxHealth).append(' ');
                if (mob.attackDamage >= 0) sb.append("ATK:").append((int) mob.attackDamage).append(' ');
                if (!mob.effects.isEmpty()) sb.append("buff:").append(mob.effects.size());
                g.drawString(font, sb.toString(), left + 4, top + 14, 0xFF888888);

                if (hover) {
                    int right = left + w;
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

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                int right = getRowRight();
                if (mx > right - 48) { wave.mobs.remove(mob); refresh(); return true; }
                if (mx > right - 88 && mx < right - 48) {
                    minecraft.setScreen(new WaveMobDetailEditorScreen(
                            WaveMobListEditorScreen.this, mob));
                    return true;
                }
                return false;
            }
        }
    }
}