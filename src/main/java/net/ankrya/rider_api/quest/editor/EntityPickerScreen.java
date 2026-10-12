package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class EntityPickerScreen extends Screen {

    private final Screen parent;
    private final Consumer<String> onPick;
    private final Predicate<EntityType<?>> filter;

    private EditBox searchBox;
    private EntityListWidget listWidget;
    private List<EntityType<?>> filtered = new ArrayList<>();

    public EntityPickerScreen(Screen parent, Consumer<String> onPick) {
        this(parent, onPick, t -> true);
    }

    public EntityPickerScreen(Screen parent, Consumer<String> onPick,
                              Predicate<EntityType<?>> filter) {
        super(Component.translatable("gui.api.editor.picker.entity_title"));
        this.parent = parent;
        this.onPick = onPick;
        this.filter = filter;
    }

    @Override
    protected void init() {
        searchBox = new EditBox(font, width / 2 - 100, 30, 200, 20, Component.literal("search"));
        searchBox.setHint(Component.translatable("gui.api.editor.picker.search_entity"));
        searchBox.setResponder(this::filter);
        addRenderableWidget(searchBox);

        listWidget = new EntityListWidget();
        addRenderableWidget(listWidget);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.cancel"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 28, 100, 20).build());

        filter("");
    }

    private void filter(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (!filter.test(type)) continue;
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            String idStr = id.toString();
            String name = type.getDescription().getString().toLowerCase(Locale.ROOT);
            if (lower.isEmpty() || idStr.contains(lower) || name.contains(lower)) {
                filtered.add(type);
            }
        }
        filtered.sort((a, b) -> BuiltInRegistries.ENTITY_TYPE.getKey(a).toString()
                .compareTo(BuiltInRegistries.ENTITY_TYPE.getKey(b).toString()));
        if (listWidget != null) listWidget.refresh();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
    }

    private class EntityListWidget extends ObjectSelectionList<EntityListWidget.Entry> {
        EntityListWidget() {
            super(EntityPickerScreen.this.minecraft, EntityPickerScreen.this.width, EntityPickerScreen.this.height - 90, 60, (60) + (EntityPickerScreen.this.height - 90), 18);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            filtered.forEach(t -> addEntry(new Entry(t)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final EntityType<?> type;
            final ResourceLocation id;
            Entry(EntityType<?> t) {
                type = t;
                id = BuiltInRegistries.ENTITY_TYPE.getKey(t);
            }

            @Override
            public Component getNarration() { return type.getDescription(); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                if (hover) g.fill(left, top, left + w, top + h, 0x33FFFFFF);
                g.drawString(font, type.getDescription(), left + 4, top + 2, 0xFFFFFF);
                g.drawString(font, id.toString(), left + 4, top + 2 + font.lineHeight, 0xFF888888);
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