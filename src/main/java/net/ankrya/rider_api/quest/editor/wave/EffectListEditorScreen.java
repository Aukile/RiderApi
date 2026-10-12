package net.ankrya.rider_api.quest.editor.wave;

import net.ankrya.rider_api.quest.editor.QuestEditorData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** buff 列表：等级 +/-、时长切换、删除 */
public class EffectListEditorScreen extends Screen {

    private final Screen parent;
    private final List<QuestEditorData.EffectData> effects;
    private EffectList listWidget;

    /** 常用时长档位（tick），-1 = 无限 */
    private static final int[] DURATIONS = { -1, 200, 600, 1200, 3600, 12000 };

    public EffectListEditorScreen(Screen parent, List<QuestEditorData.EffectData> effects) {
        super(Component.translatable("gui.api.wave.effects.title"));
        this.parent = parent;
        this.effects = effects;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.api.wave.effects.add"),
                b -> minecraft.setScreen(new EffectPickerScreen(this, id -> {
                    var f = new QuestEditorData.EffectData();
                    f.effect = id;
                    effects.add(f);
                }))).bounds(width / 2 - 50, 28, 100, 20).build());

        listWidget = new EffectList();
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


    private static Component effectName(String id) {
        try {
            var key = ResourceKey.create(Registries.MOB_EFFECT, new ResourceLocation(id));
            return BuiltInRegistries.MOB_EFFECT.getHolder(key)
                    .map(h -> h.value().getDisplayName())
                    .orElse(Component.literal(id));
        } catch (Exception e) {
            return Component.literal(id);
        }
    }

    private static String durationText(int ticks) {
        return ticks < 0 ? "∞" : (ticks / 20) + "s";
    }

    private class EffectList extends ObjectSelectionList<EffectList.Entry> {
        EffectList() {
            super(EffectListEditorScreen.this.minecraft, EffectListEditorScreen.this.width, EffectListEditorScreen.this.height - 100, 56, (56) + (EffectListEditorScreen.this.height - 100), 26);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            effects.forEach(f -> addEntry(new Entry(f)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final QuestEditorData.EffectData fx;
            Entry(QuestEditorData.EffectData f) { fx = f; }

            @Override public Component getNarration() { return effectName(fx.effect); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                g.drawString(font, Component.empty().append(effectName(fx.effect))
                        .append(Component.literal("  " + (fx.amplifier + 1) + "级")), left + 4, top + 3, 0xFFFFFF);
                g.drawString(font, fx.effect + "  " + durationText(fx.duration),
                        left + 4, top + 14, 0xFF888888);

                int right = left + w;
                if (hover) {
                    g.drawCenteredString(font, "[-]", right - 190, top + 9, 0xFFAAAAAA);
                    g.drawCenteredString(font, "[+]", right - 165, top + 9, 0xFFAAAAAA);
                    g.drawCenteredString(font, "[" + durationText(fx.duration) + "]",
                            right - 120, top + 9, 0xFFAAAAAA);
                    if (mx > right - 48) g.fill(right - 48, top, right, top + h, 0x55FF4444);
                    g.drawCenteredString(font,
                            Component.translatable("gui.api.editor.btn.remove").getString(),
                            right - 24, top + 9, 0xFFFFFF);
                }
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                int right = getRowRight();
                if (mx > right - 200 && mx < right - 180) {
                    fx.amplifier = Math.max(0, fx.amplifier - 1); return true;
                }
                if (mx > right - 175 && mx < right - 155) {
                    fx.amplifier = Math.min(255, fx.amplifier + 1); return true;
                }
                if (mx > right - 145 && mx < right - 95) {
                    int i = 0;
                    for (int k = 0; k < DURATIONS.length; k++) if (DURATIONS[k] == fx.duration) i = k;
                    fx.duration = DURATIONS[(i + 1) % DURATIONS.length];
                    return true;
                }
                if (mx > right - 48) { effects.remove(fx); refresh(); return true; }
                return false;
            }
        }
    }
}