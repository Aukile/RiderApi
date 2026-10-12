package net.ankrya.rider_api.quest.editor.translation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class TranslationEditorScreen extends Screen {

    private static final int W = 260;
    private static final int H = 130;

    private final Screen parent;
    private final Consumer<String> onSelect;
    private final String pendingKey;
    private final String pendingValue;

    private EditBox keyBox;
    private EditBox valueBox;

    public TranslationEditorScreen(Screen parent, String initialKey, Consumer<String> onSelect) {
        super(Component.translatable("gui.api.trans.title"));
        this.parent = parent;
        this.onSelect = onSelect;
        String initKey = initialKey == null ? "" : initialKey;
        this.pendingKey = initKey;
        this.pendingValue = initKey.isEmpty() ? "" : TranslationManager.INSTANCE.get(initKey);
    }

    @Override
    protected void init() {
        int left = (width - W) / 2;
        int top = (height - H) / 2;

        keyBox = new EditBox(font, left + 12, top + 34, W - 24, 18, Component.empty());
        keyBox.setHint(Component.translatable("gui.api.trans.key"));
        keyBox.setMaxLength(256);
        keyBox.setValue(pendingKey);
        addRenderableWidget(keyBox);

        valueBox = new EditBox(font, left + 12, top + 68, W - 24, 18, Component.empty());
        valueBox.setHint(Component.translatable("gui.api.trans.value"));
        valueBox.setMaxLength(1024);
        valueBox.setValue(pendingValue);
        addRenderableWidget(valueBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.trans.save"),
                b -> save()
        ).bounds(left + 12, top + H - 26, (W - 32) / 2, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.cancel"),
                b -> minecraft.setScreen(parent)
        ).bounds(left + W / 2 + 4, top + H - 26, (W - 32) / 2, 20).build());
    }

    private void save() {
        String key = keyBox.getValue().trim();
        String value = valueBox.getValue().trim();
        if (key.isEmpty()) return;
        TranslationManager.INSTANCE.put(key, value.isEmpty() ? key : value);
        onSelect.accept(key);
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0x80000000);

        int left = (width - W) / 2;
        int top = (height - H) / 2;

        g.fill(left, top, left + W, top + H, 0xF0202028);
        g.renderOutline(left, top, W, H, 0xFFAAAAAA);

        g.drawCenteredString(font, title, width / 2, top + 10, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.api.trans.key"), left + 12, top + 24, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.api.trans.value"), left + 12, top + 58, 0xAAAAAA);

        super.render(g, mx, my, pt);
    }

    @Override public boolean isPauseScreen() { return false; }
}