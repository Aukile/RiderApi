package net.ankrya.rider_api.quest.editor.style;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.util.*;
import java.util.function.Consumer;

/** 从资源管理器里扫描所有 textures/**.png，支持搜索，选中带缩略图预览 */
public class TexturePickerScreen extends Screen {

    private final Screen parent;
    private final Consumer<String> onPick;

    private EditBox searchBox;
    private List<ResourceLocation> all = new ArrayList<>();
    private List<ResourceLocation> filtered = new ArrayList<>();
    private int scrollRow = 0;

    private int gridX, gridY, cols, cell = 40, visibleRows;

    public TexturePickerScreen(Screen parent, Consumer<String> onPick) {
        super(Component.translatable("gui.api.style.picker.title"));
        this.parent = parent;
        this.onPick = onPick;
    }

    @Override
    protected void init() {
        // 扫描所有已加载的 png 贴图
        if (all.isEmpty()) {
            Map<ResourceLocation, Resource> map = Minecraft.getInstance()
                    .getResourceManager()
                    .listResources("textures", rl -> rl.getPath().endsWith(".png"));
            all = new ArrayList<>(map.keySet());
            all.sort(Comparator.comparing(ResourceLocation::toString));
        }

        searchBox = new EditBox(font, width / 2 - 120, 26, 240, 18,
                Component.translatable("gui.api.style.picker.search"));
        searchBox.setHint(Component.translatable("gui.api.style.picker.search"));
        searchBox.setResponder(this::filter);
        addRenderableWidget(searchBox);

        cols = Math.max(4, (width - 60) / cell);
        gridX = width / 2 - (cols * cell) / 2;
        gridY = 54;
        visibleRows = (height - gridY - 40) / cell;

        addRenderableWidget(Button.builder(
                Component.translatable("gui.api.editor.btn.cancel"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 26, 100, 20).build());

        filter("");
    }

    private void filter(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (var rl : all) {
            if (lower.isEmpty() || rl.toString().contains(lower)) filtered.add(rl);
        }
        scrollRow = 0;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        ResourceLocation hovered = null;
        int start = scrollRow * cols;
        for (int i = 0; i < visibleRows * cols; i++) {
            int idx = start + i;
            if (idx >= filtered.size()) break;
            int col = i % cols, row = i / cols;
            int x = gridX + col * cell, y = gridY + row * cell;

            boolean h = mx >= x && mx < x + cell && my >= y && my < y + cell;
            if (h) { g.fill(x, y, x + cell, y + cell, 0x55FFFFFF); hovered = filtered.get(idx); }

            // 缩略图（贴图直接 blit 到 32x32）
            try {
                g.blit(filtered.get(idx), x + 4, y + 4, 0, 0, 32, 32, 32, 32);
            } catch (Exception e) {
                g.fill(x + 4, y + 4, x + 36, y + 36, 0xFF444444);
            }
            g.renderOutline(x + 3, y + 3, 34, 34, 0xFF000000);
        }

        if (hovered != null) {
            g.renderTooltip(font, Component.literal(hovered.toString()), mx, my);
        }

        // 滚动条
        int totalRows = (filtered.size() + cols - 1) / cols;
        if (totalRows > visibleRows) {
            int tx = gridX + cols * cell + 4;
            int th = visibleRows * cell;
            g.fill(tx, gridY, tx + 4, gridY + th, 0x33FFFFFF);
            int thumbH = Math.max(12, th * visibleRows / totalRows);
            int thumbY = gridY + (int)((float) scrollRow / (totalRows - visibleRows) * (th - thumbH));
            g.fill(tx, thumbY, tx + 4, thumbY + thumbH, 0xFFAAAAAA);
        }

        g.drawString(font,
                Component.translatable("gui.api.style.picker.count", filtered.size()),
                gridX, height - 24, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int start = scrollRow * cols;
        for (int i = 0; i < visibleRows * cols; i++) {
            int idx = start + i;
            if (idx >= filtered.size()) break;
            int col = i % cols, row = i / cols;
            int x = gridX + col * cell, y = gridY + row * cell;
            if (mx >= x && mx < x + cell && my >= y && my < y + cell) {
                onPick.accept(filtered.get(idx).toString());
                minecraft.setScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dy) {
        int totalRows = (filtered.size() + cols - 1) / cols;
        int maxScroll = Math.max(0, totalRows - visibleRows);
        scrollRow = (int) Math.max(0, Math.min(maxScroll, scrollRow - dy));
        return true;
    }
}