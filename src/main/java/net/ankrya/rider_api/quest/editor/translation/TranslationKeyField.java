package net.ankrya.rider_api.quest.editor.translation;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** 翻译键输入：显示 key + 值预览，点击打开弹窗编辑 */
public class TranslationKeyField {

    private final Screen parent;
    private final Consumer<String> onChanged;
    private String currentKey;
    private Button button;

    public TranslationKeyField(Screen parent, String labelKey, String initialKey,
                               Consumer<String> onChanged) {
        this.parent = parent;
        this.currentKey = initialKey == null ? "" : initialKey;
        this.onChanged = onChanged;
    }

    public Button createButton(int x, int y, int width) {
        button = Button.builder(getDisplayText(), b -> openEditor())
                .bounds(x, y, width, 20).build();
        return button;
    }

    private Component getDisplayText() {
        if (currentKey.isEmpty()) {
            return Component.translatable("gui.api.trans.empty");
        }
        String preview = TranslationManager.INSTANCE.get(currentKey);
        return Component.literal(preview + " ")
                .append(Component.literal("[" + currentKey + "]")
                        .withStyle(s -> s.withColor(0xFF888888)));
    }

    private void openEditor() {
        parent.getMinecraft().setScreen(
                new TranslationEditorScreen(parent, currentKey, key -> {
                    currentKey = key;
                    onChanged.accept(key);
                    if (button != null) button.setMessage(getDisplayText());
                }));
    }

    public String getKey() { return currentKey; }
}