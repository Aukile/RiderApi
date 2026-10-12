package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ItemPickerScreen extends Screen {

    private static final int COLS = 9;
    private static final int CELL = 20;

    private final Screen parent;
    private final Consumer<String> onPick;

    private EditBox searchBox;
    private List<Item> filtered = new ArrayList<>();
    private int scrollRow = 0;
    private int gridX, gridY, visibleRows;

    public ItemPickerScreen(Screen parent, Consumer<String> onPick) {
        super(Component.translatable("gui.api.editor.picker.item_title"));
        this.parent = parent;
        this.onPick = onPick;
    }

    @Override
    protected void init() {
        searchBox = new EditBox(font, width / 2 - 100, 30, 200, 20, Component.literal("search"));
        searchBox.setHint(Component.translatable("gui.api.editor.picker.search_item"));
        searchBox.setResponder(this::filter);
        addRenderableWidget(searchBox);

        gridX = width / 2 - (COLS * CELL) / 2;
        gridY = 60;
        visibleRows = (height - gridY - 40) / CELL;

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.cancel"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 28, 100, 20).build());

        filter(""); // 初始化显示全部
    }

    private void filter(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == net.minecraft.world.item.Items.AIR) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            String idStr = id.toString();
            String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
            // 模糊搜索：匹配注册名或显示名
            if (lower.isEmpty() || idStr.contains(lower) || name.contains(lower)) {
                filtered.add(item);
            }
        }
        scrollRow = 0;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        Item hovered = null;

        int startIndex = scrollRow * COLS;
        for (int i = 0; i < visibleRows * COLS; i++) {
            int idx = startIndex + i;
            if (idx >= filtered.size()) break;

            int col = i % COLS;
            int row = i / COLS;
            int x = gridX + col * CELL;
            int y = gridY + row * CELL;

            boolean cellHover = mx >= x && mx < x + CELL && my >= y && my < y + CELL;
            if (cellHover) {
                g.fill(x, y, x + CELL, y + CELL, 0x55FFFFFF);
                hovered = filtered.get(idx);
            }
            g.renderItem(new ItemStack(filtered.get(idx)), x + 2, y + 2);
        }

        // 悬浮显示物品名 + 注册名
        if (hovered != null) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(hovered);
            g.renderComponentTooltip(font, List.of(
                    new ItemStack(hovered).getHoverName(),
                    Component.literal(id.toString()).withStyle(s -> s.withColor(0xFF888888))
            ), mx, my);
        }

        // 滚动条
        int totalRows = (filtered.size() + COLS - 1) / COLS;
        if (totalRows > visibleRows) {
            int trackX = gridX + COLS * CELL + 4;
            int trackH = visibleRows * CELL;
            g.fill(trackX, gridY, trackX + 4, gridY + trackH, 0x33FFFFFF);
            int thumbH = Math.max(10, trackH * visibleRows / totalRows);
            int thumbY = gridY + (int)((float) scrollRow / (totalRows - visibleRows) * (trackH - thumbH));
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }

        g.drawString(font, Component.translatable("gui.api.editor.picker.item_count", filtered.size()), gridX, height - 26, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int startIndex = scrollRow * COLS;
        for (int i = 0; i < visibleRows * COLS; i++) {
            int idx = startIndex + i;
            if (idx >= filtered.size()) break;
            int col = i % COLS;
            int row = i / COLS;
            int x = gridX + col * CELL;
            int y = gridY + row * CELL;
            if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(filtered.get(idx));
                onPick.accept(id.toString());
                minecraft.setScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dy) {
        int totalRows = (filtered.size() + COLS - 1) / COLS;
        int maxScroll = Math.max(0, totalRows - visibleRows);
        scrollRow = (int) Math.max(0, Math.min(maxScroll, scrollRow - dy));
        return true;
    }
}