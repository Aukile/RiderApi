package net.ankrya.rider_api.quest.editor.common;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SuggestListEditorScreen extends Screen {

    private final Screen parent;
    private final List<String> items;
    private final List<String> allCandidates;
    /** 可选：把候选/条目 ID 转成额外显示文本（如任务标题），null 则只显示 ID */
    @Nullable private final Function<String, Component> displayName;

    private EditBox inputBox;
    private ListWidget listWidget;

    private List<String> suggestions = new ArrayList<>();
    private int suggestIndex = 0;
    private int suggestScroll = 0;
    private static final int MAX_VISIBLE = 8;

    public SuggestListEditorScreen(Screen parent, Component title,
                                   List<String> items, List<String> allCandidates) {
        this(parent, title, items, allCandidates, null);
    }

    public SuggestListEditorScreen(Screen parent, Component title,
                                   List<String> items, List<String> allCandidates,
                                   @Nullable Function<String, Component> displayName) {
        super(title);
        this.parent = parent;
        this.items = items;
        this.allCandidates = allCandidates;
        this.displayName = displayName;
    }

    @Override
    protected void init() {
        inputBox = new EditBox(font, width / 2 - 120, 34, 200, 20, Component.empty());
        inputBox.setHint(Component.translatable("gui.api.adv.suggest_hint"));
        inputBox.setResponder(this::updateSuggestions);
        addRenderableWidget(inputBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.add"),
                b -> commit()).bounds(width / 2 + 85, 34, 40, 20).build());

        listWidget = new ListWidget();
        addRenderableWidget(listWidget);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)).bounds(width / 2 - 50, height - 28, 100, 20).build());
    }

    private void commit() {
        String v = inputBox.getValue().trim();
        if (!v.isEmpty()) {
            items.add(v);
            listWidget.refresh();
            inputBox.setValue("");
            suggestions.clear();
        }
    }

    private void updateSuggestions(String text) {
        if (text.isEmpty()) { suggestions.clear(); return; }
        String lower = text.toLowerCase(Locale.ROOT);
        suggestions = allCandidates.stream()
                .filter(s -> matches(s, lower))
                .sorted((a, b) -> {
                    boolean as = a.toLowerCase(Locale.ROOT).startsWith(lower);
                    boolean bs = b.toLowerCase(Locale.ROOT).startsWith(lower);
                    if (as && !bs) return -1;
                    if (!as && bs) return 1;
                    return a.compareTo(b);
                })
                .collect(Collectors.toList());
        suggestIndex = 0;
        suggestScroll = 0;
        if (suggestions.size() == 1 && suggestions.get(0).equalsIgnoreCase(text))
            suggestions.clear();
    }

    /** 匹配 ID 或显示名 */
    private boolean matches(String candidate, String lower) {
        if (candidate.toLowerCase(Locale.ROOT).contains(lower)) return true;
        if (displayName != null) {
            String dn = displayName.apply(candidate).getString().toLowerCase(Locale.ROOT);
            return dn.contains(lower);
        }
        return false;
    }

    /** 下拉/条目的显示文本：有 displayName 就 "标题 (id)"，否则纯 id */
    private Component label(String id) {
        if (displayName != null) {
            return Component.empty()
                    .append(displayName.apply(id))
                    .append(Component.literal("  " + id).withStyle(s -> s.withColor(0xFF888888)));
        }
        return Component.literal(id);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (inputBox.isFocused() && !suggestions.isEmpty()) {
            if (k == InputConstants.KEY_DOWN) {
                suggestIndex = (suggestIndex + 1) % suggestions.size();
                clampScroll(); return true;
            } else if (k == InputConstants.KEY_UP) {
                suggestIndex = (suggestIndex - 1 + suggestions.size()) % suggestions.size();
                clampScroll(); return true;
            } else if (k == InputConstants.KEY_TAB) {
                inputBox.setValue(suggestions.get(suggestIndex));
                inputBox.moveCursorToEnd();
                suggestions.clear(); return true;
            } else if (k == InputConstants.KEY_RETURN) {
                inputBox.setValue(suggestions.get(suggestIndex));
                suggestions.clear();
                commit(); return true;
            }
        }
        return super.keyPressed(k, s, m);
    }

    private void clampScroll() {
        if (suggestIndex < suggestScroll) suggestScroll = suggestIndex;
        else if (suggestIndex >= suggestScroll + MAX_VISIBLE)
            suggestScroll = suggestIndex - MAX_VISIBLE + 1;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dy) {
        if (!suggestions.isEmpty()) {
            int x = inputBox.getX(), y = inputBox.getY() + inputBox.getHeight();
            int w = inputBox.getWidth(), h = Math.min(MAX_VISIBLE, suggestions.size()) * 12 + 2;
            if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
                int max = Math.max(0, suggestions.size() - MAX_VISIBLE);
                suggestScroll = (int) Math.max(0, Math.min(max, suggestScroll - dy));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, dy);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!suggestions.isEmpty()) {
            int x = inputBox.getX(), y = inputBox.getY() + inputBox.getHeight();
            int w = inputBox.getWidth();
            int visible = Math.min(MAX_VISIBLE, suggestions.size());
            if (mx >= x && mx <= x + w && my >= y && my <= y + visible * 12 + 2) {
                int row = (int) ((my - y - 2) / 12);
                int idx = suggestScroll + row;
                if (idx >= 0 && idx < suggestions.size()) {
                    inputBox.setValue(suggestions.get(idx));
                    inputBox.moveCursorToEnd();
                    suggestions.clear();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        if (!suggestions.isEmpty()) {
            int x = inputBox.getX(), y = inputBox.getY() + inputBox.getHeight();
            int w = inputBox.getWidth();
            int visible = Math.min(MAX_VISIBLE, suggestions.size());
            int h = visible * 12 + 2;
            g.fill(x, y, x + w, y + h, 0xEE000000);
            g.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFFAAAAAA);
            for (int i = 0; i < visible; i++) {
                int real = suggestScroll + i;
                int ry = y + 1 + i * 12;
                if (real == suggestIndex) g.fill(x, ry, x + w, ry + 12, 0x44FFFFFF);
                // 下拉里显示 标题+ID
                g.drawString(font, label(suggestions.get(real)), x + 4, ry + 2,
                        real == suggestIndex ? 0xFFFF00 : 0xFFFFFF);
            }
            if (suggestions.size() > MAX_VISIBLE) {
                int trackX = x + w - 4;
                int thumbH = Math.max(8, h * visible / suggestions.size());
                int thumbY = y + (int)((float) suggestScroll / (suggestions.size() - visible) * (h - thumbH));
                g.fill(trackX, y, trackX + 3, y + h, 0x66000000);
                g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFAAAAAA);
            }
        }
    }


    private class ListWidget extends ObjectSelectionList<ListWidget.Entry> {
        ListWidget() {
            super(SuggestListEditorScreen.this.minecraft, SuggestListEditorScreen.this.width, SuggestListEditorScreen.this.height - 120, 66, (66) + (SuggestListEditorScreen.this.height - 120), 20);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }
        void refresh() {
            clearEntries();
            items.forEach(s -> addEntry(new Entry(s)));
        }
        class Entry extends ObjectSelectionList.Entry<Entry> {
            final String text;
            Entry(String s) { text = s; }
            @Override public Component getNarration() { return Component.literal(text); }
            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                g.drawString(font, label(text), left + 4, top + 6, 0xFFFFFF);
                if (hover && mx > left + w - 60) {
                    g.fill(left + w - 60, top, left + w, top + h, 0x55FF4444);
                    g.drawCenteredString(font,
                            Component.translatable("gui.api.editor.btn.remove").getString(),
                            left + w - 30, top + 6, 0xFFFFFF);
                }
            }
            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                if (mx > getRowRight() - 60) {
                    items.remove(text);
                    refresh();
                    return true;
                }
                return false;
            }
        }
    }
}