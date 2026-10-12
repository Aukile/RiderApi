package net.ankrya.rider_api.help.advancement;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RequirementsScreen extends Screen {

    private final Screen parent;
    private final AdvancementEditorData data;

    public RequirementsScreen(Screen parent, AdvancementEditorData data) {
        super(Component.translatable("gui.api.adv.req_title"));
        this.parent = parent;
        this.data = data;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        // AND：每个 criterion 单独一组
        addRenderableWidget(Button.builder(Component.translatable("gui.api.adv.req_and"),
                b -> {
                    data.requirements.clear();
                    for (var c : data.criteria) data.requirements.add(List.of(c.name));
                }).bounds(cx - 100, 50, 200, 20).build());

        // OR：全部 criterion 在同一组
        addRenderableWidget(Button.builder(Component.translatable("gui.api.adv.req_or"),
                b -> {
                    data.requirements.clear();
                    List<String> group = new ArrayList<>();
                    data.criteria.forEach(c -> group.add(c.name));
                    data.requirements.add(group);
                }).bounds(cx - 100, 74, 200, 20).build());

        // 清空（默认全 AND）
        addRenderableWidget(Button.builder(Component.translatable("gui.api.adv.req_clear"),
                b -> data.requirements.clear()).bounds(cx - 100, 98, 200, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(cx - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        g.drawCenteredString(font,
                Component.translatable("gui.api.adv.req_current", data.requirements.size()),
                width / 2, 130, 0xAAAAAA);
    }

}