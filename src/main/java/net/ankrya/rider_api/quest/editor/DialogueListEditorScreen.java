package net.ankrya.rider_api.quest.editor;

import net.ankrya.rider_api.quest.editor.translation.TranslationEditorScreen;
import net.ankrya.rider_api.quest.editor.translation.TranslationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** 对话列表编辑：每条是一个翻译键，按顺序排列 */
public class DialogueListEditorScreen extends Screen {

    private final Screen parent;
    private final List<String> dialogues; // 存翻译键
    private DialogueList listWidget;

    public DialogueListEditorScreen(Screen parent, List<String> dialogues) {
        super(Component.translatable("gui.api.editor.dialogues.title"));
        this.parent = parent;
        this.dialogues = dialogues;
    }

    @Override
    protected void init() {
        // 新增一条对话：先弹翻译编辑器建键，保存后加入列表
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.add"),
                b -> minecraft.setScreen(new TranslationEditorScreen(this, "", key -> {
                    if (!key.isEmpty()) {
                        dialogues.add(key);
                        listWidget.refresh();
                    }
                }))).bounds(width / 2 - 50, 30, 100, 20).build());

        listWidget = new DialogueList();
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


    private class DialogueList extends ObjectSelectionList<DialogueList.Entry> {
        DialogueList() {
            super(DialogueListEditorScreen.this.minecraft, DialogueListEditorScreen.this.width, DialogueListEditorScreen.this.height - 100, 60, (60) + (DialogueListEditorScreen.this.height - 100), 24);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            dialogues.forEach(k -> addEntry(new Entry(k)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final String key;
            Entry(String k) { key = k; }

            @Override public Component getNarration() { return Component.literal(key); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                // 序号
                g.drawString(font, "#" + (idx + 1), left + 2, top + 8, 0xFF6EC6FF);
                // 预览文本
                String preview = TranslationManager.INSTANCE.get(key);
                String shown = font.plainSubstrByWidth(preview, w - 160);
                g.drawString(font, shown, left + 26, top + 2, 0xFFFFFF);
                // 键名
                g.drawString(font, key, left + 26, top + 12, 0xFF888888);

                if (hover) {
                    int right = left + w;
                    // 上移
                    drawMini(g, right - 130, top, "▲", 0x5544AAFF, mx, my);
                    // 下移
                    drawMini(g, right - 108, top, "▼", 0x5544AAFF, mx, my);
                    // 编辑
                    if (mx > right - 86 && mx < right - 46) {
                        g.fill(right - 86, top, right - 46, top + h, 0x5544FF44);
                    }
                    g.drawCenteredString(font,
                            Component.translatable("gui.api.editor.btn.edit").getString(),
                            right - 66, top + 8, 0xFFFFFF);
                    // 删除
                    if (mx > right - 46) g.fill(right - 46, top, right, top + h, 0x55FF4444);
                    g.drawCenteredString(font,
                            Component.translatable("gui.api.editor.btn.remove").getString(),
                            right - 23, top + 8, 0xFFFFFF);
                }
            }

            private void drawMini(GuiGraphics g, int x, int top, String s, int hoverBg,
                                  int mx, int my) {
                boolean h = mx >= x && mx < x + 20 && my >= top && my < top + 24;
                if (h) g.fill(x, top, x + 20, top + 24, hoverBg);
                g.drawCenteredString(font, s, x + 10, top + 8, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                int right = getRowRight();
                int idx = dialogues.indexOf(key);

                // 上移
                if (mx > right - 130 && mx < right - 110) {
                    if (idx > 0) {
                        dialogues.remove(idx);
                        dialogues.add(idx - 1, key);
                        refresh();
                    }
                    return true;
                }
                // 下移
                if (mx > right - 108 && mx < right - 88) {
                    if (idx < dialogues.size() - 1) {
                        dialogues.remove(idx);
                        dialogues.add(idx + 1, key);
                        refresh();
                    }
                    return true;
                }
                // 编辑（改这条对话的翻译内容，键名不变或改名）
                if (mx > right - 86 && mx < right - 46) {
                    minecraft.setScreen(new TranslationEditorScreen(
                            DialogueListEditorScreen.this, key, newKey -> {
                        int i = dialogues.indexOf(key);
                        if (i >= 0) dialogues.set(i, newKey);
                        refresh();
                    }));
                    return true;
                }
                // 删除
                if (mx > right - 46) {
                    dialogues.remove(key);
                    refresh();
                    return true;
                }
                return false;
            }
        }
    }
}