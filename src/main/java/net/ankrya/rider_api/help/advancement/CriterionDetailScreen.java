package net.ankrya.rider_api.help.advancement;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CriterionDetailScreen extends Screen {

    private final Screen parent;
    private final AdvancementEditorData.Criterion crit;
    private EditBox condBox;

    public CriterionDetailScreen(Screen parent, AdvancementEditorData.Criterion crit) {
        super(Component.translatable("gui.api.adv.criterion_title"));
        this.parent = parent;
        this.crit = crit;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 40;

        EditBox nameBox = new EditBox(font, cx - 100, y, 200, 20, Component.empty());
        nameBox.setValue(crit.name);
        nameBox.setResponder(v -> crit.name = v);
        addRenderableWidget(nameBox);
        y += 30;

        // trigger 循环选择（常用列表）
        addRenderableWidget(CycleButton.<String>builder(Component::literal)
                .withValues(AdvancementTriggers.COMMON)
                .withInitialValue(AdvancementTriggers.COMMON.contains(crit.trigger)
                        ? crit.trigger : AdvancementTriggers.COMMON.get(0))
                .create(cx - 100, y, 200, 20, Component.translatable("gui.api.adv.trigger"),
                        (btn, val) -> crit.trigger = val));
        y += 24;
        // 也允许手填 trigger
        EditBox triggerBox = new EditBox(font, cx - 100, y, 200, 20, Component.empty());
        triggerBox.setValue(crit.trigger);
        triggerBox.setResponder(v -> crit.trigger = v);
        addRenderableWidget(triggerBox);
        y += 30;

        // conditions JSON 手填
        condBox = new EditBox(font, cx - 100, y, 200, 20, Component.empty());
        condBox.setValue(crit.conditionsJson);
        condBox.setResponder(v -> crit.conditionsJson = v);
        addRenderableWidget(condBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(cx - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0xC0101018);
        super.render(g, mx, my, pt);
        int cx = width / 2;
        g.drawCenteredString(font, title, cx, 12, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.api.adv.crit_name"), cx - 150, 46, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.api.adv.trigger"), cx - 150, 100, 0xAAAAAA);
        g.drawString(font, Component.translatable("gui.api.adv.conditions"), cx - 150, 130, 0xAAAAAA);
    }

}