package net.ankrya.rider_api.quest.editor;

import net.ankrya.rider_api.quest.editor.wave.WaveListEditorScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ObjectivesRewardsTab extends GridLayoutTab {

    public ObjectivesRewardsTab(Screen screen, QuestEditorData data) {
        super(Component.translatable("gui.api.editor.tab.objectives"));

        GridLayout.RowHelper rows = this.layout.rowSpacing(6).createRowHelper(1);

        rows.addChild(Button.builder(
                Component.translatable("gui.api.editor.btn.edit_objectives", data.objectives.size()),
                b -> screen.getMinecraft().setScreen(
                        new ObjectiveListEditorScreen(screen, data.objectives))
        ).width(220).build());

        rows.addChild(Button.builder(
                Component.translatable("gui.api.editor.btn.edit_waves", data.waves.size()),
                b -> screen.getMinecraft().setScreen(
                        new WaveListEditorScreen(screen, data))
        ).width(220).build());

        rows.addChild(Button.builder(
                Component.translatable("gui.api.editor.btn.edit_rewards"),
                b -> screen.getMinecraft().setScreen(
                        new RewardEditorScreen(screen, data))
        ).width(220).build());
    }
}