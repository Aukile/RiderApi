package net.ankrya.rider_api.quest.editor.common;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** 1.20 的 Checkbox 没有 builder / onValueChange，这里补一个等价的创建方法 */
public final class Checkboxes {
    private Checkboxes() {}

    public static Checkbox create(int x, int y, Component text, Font font, boolean selected, Consumer<Boolean> onChange) {
        int w = 20 + 4 + font.width(text);
        return new Checkbox(x, y, w, 20, text, selected) {
            @Override
            public void onPress() {
                super.onPress();
                onChange.accept(this.selected());
            }
        };
    }
}
