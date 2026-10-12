package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StringListEditorScreen extends Screen {

    private final Screen parent;
    private final List<String> items;   // 改名，避免与继承的 list 成员冲突
    private EditBox inputBox;
    private StringListWidget listWidget;

    public StringListEditorScreen(Screen parent, String title, List<String> items) {
        super(Component.literal(title));
        this.parent = parent;
        this.items = items;
    }

    @Override
    protected void init() {
        inputBox = new EditBox(font, width / 2 - 100, 30, 160, 20, Component.literal("input"));
        addRenderableWidget(inputBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.add"),
                b -> {
                    String val = inputBox.getValue().trim();
                    if (!val.isEmpty()) {
                        items.add(val);
                        listWidget.refresh();
                        inputBox.setValue("");
                    }
                }).bounds(width / 2 + 65, 30, 40, 20).build());

        listWidget = new StringListWidget();
        addRenderableWidget(listWidget);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
    }

    private class StringListWidget extends ObjectSelectionList<StringListWidget.Entry> {
        StringListWidget() {
            // 关键修复：用外部类限定，避免引用自己继承的 minecraft/width/height
            super(StringListEditorScreen.this.minecraft, StringListEditorScreen.this.width, StringListEditorScreen.this.height - 100, 60, (60) + (StringListEditorScreen.this.height - 100), 20);
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

            @Override
            public Component getNarration() { return Component.literal(text); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                g.drawString(font, text, left + 4, top + 6, 0xFFFFFF);
                if (hover && mx > left + w - 60) {
                    g.fill(left + w - 60, top, left + w, top + h, 0x55FF4444);
                    g.drawCenteredString(font, Component.translatable("gui.api.editor.btn.remove").getString(), left + w - 30, top + 6, 0xFFFFFF);
                }
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                if (mx > getRowRight() - 60) {
                    items.remove(text);   // 明确是 List<String>.remove(Object)
                    refresh();
                    return true;
                }
                return false;
            }
        }
    }
}