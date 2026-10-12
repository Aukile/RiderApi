package net.ankrya.rider_api.quest.editor.wave;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class EffectPickerScreen extends Screen {

    private final Screen parent;
    private final Consumer<String> onPick;
    private List<MobEffect> filtered = new ArrayList<>();
    private EffectListWidget listWidget;

    public EffectPickerScreen(Screen parent, Consumer<String> onPick) {
        super(Component.translatable("gui.api.wave.effects.picker_title"));
        this.parent = parent;
        this.onPick = onPick;
    }

    @Override
    protected void init() {
        EditBox search = new EditBox(font, width / 2 - 100, 28, 200, 20, Component.empty());
        search.setHint(Component.translatable("gui.api.wave.effects.search"));
        search.setResponder(this::filter);
        addRenderableWidget(search);

        listWidget = new EffectListWidget();
        addRenderableWidget(listWidget);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.cancel"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 28, 100, 20).build());

        filter("");
    }

    private void filter(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (MobEffect fx : BuiltInRegistries.MOB_EFFECT) {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(fx);
            if (id == null) continue;
            String name = fx.getDisplayName().getString().toLowerCase(Locale.ROOT);
            if (lower.isEmpty() || id.toString().contains(lower) || name.contains(lower))
                filtered.add(fx);
        }
        if (listWidget != null) listWidget.refresh();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
    }


    private class EffectListWidget extends ObjectSelectionList<EffectListWidget.Entry> {
        EffectListWidget() {
            super(EffectPickerScreen.this.minecraft, EffectPickerScreen.this.width, EffectPickerScreen.this.height - 90, 56, (56) + (EffectPickerScreen.this.height - 90), 20);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            filtered.forEach(f -> addEntry(new Entry(f)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final MobEffect fx;
            final ResourceLocation id;
            Entry(MobEffect f) {
                fx = f;
                id = BuiltInRegistries.MOB_EFFECT.getKey(f);
            }

            @Override public Component getNarration() { return fx.getDisplayName(); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                if (hover) g.fill(left, top, left + w, top + h, 0x33FFFFFF);
                g.drawString(font, fx.getDisplayName(), left + 4, top + 2, 0xFFFFFF);
                g.drawString(font, id.toString(), left + 140, top + 2, 0xFF888888);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                onPick.accept(id.toString());
                minecraft.setScreen(parent);
                return true;
            }
        }
    }
}