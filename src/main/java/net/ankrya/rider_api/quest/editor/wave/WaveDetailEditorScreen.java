package net.ankrya.rider_api.quest.editor.wave;

import net.ankrya.rider_api.quest.editor.QuestEditorData;
import net.ankrya.rider_api.quest.editor.common.ScrollFormScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WaveDetailEditorScreen extends ScrollFormScreen {

    private static final List<String> TRIGGERS =
            List.of("on_accept", "on_objective", "after_previous_cleared", "manual");

    private final QuestEditorData data;
    private final QuestEditorData.WaveData wave;

    public WaveDetailEditorScreen(Screen parent, QuestEditorData data,
                                  QuestEditorData.WaveData wave) {
        super(parent, Component.translatable("gui.api.wave.detail.title"));
        this.data = data;
        this.wave = wave;
    }

    public static Component triggerName(String trigger) {
        return Component.translatable("gui.api.wave.trigger." + trigger);
    }

    @Override
    protected void buildForm() {
        // 波次 id
        label("gui.api.wave.field.id");
        editBox(wave.id, v -> wave.id = v.isBlank() ? wave.id : v.trim());

        // 触发方式：点击循环切换
        label("gui.api.wave.field.trigger");
        var holder = new Button[1];
        button(triggerName(wave.trigger), b -> {
            int i = TRIGGERS.indexOf(wave.trigger);
            wave.trigger = TRIGGERS.get((i + 1) % TRIGGERS.size());
            b.setMessage(triggerName(wave.trigger));
            rebuild(); // on_objective 会多出一行字段，仍需重建
        });

        // 触发目标（仅 on_objective）
        if (wave.trigger.equals("on_objective")) {
            label("gui.api.wave.field.trigger_objective");
            EditBox box = editBox(wave.triggerObjective, v -> wave.triggerObjective = v.trim());

            if (!data.objectives.isEmpty()) {
                label("gui.api.wave.field.pick_objective");
                button(Component.translatable("gui.api.wave.field.next_objective"), b -> {
                    var ids = data.objectives.stream().map(o -> o.id).toList();
                    int i = ids.indexOf(wave.triggerObjective);
                    wave.triggerObjective = ids.get((i + 1) % ids.size());
                    box.setValue(wave.triggerObjective);
                });
            }
        }

        label("gui.api.wave.field.delay");
        editBoxInt(wave.delayTicks, v -> wave.delayTicks = Math.max(0, v));

        label("gui.api.wave.field.radius");
        editBoxDouble(wave.spawnRadius, v -> wave.spawnRadius = v <= 0 ? 8 : v);

        label("gui.api.wave.field.announce");
        editBox(wave.announce, v -> wave.announce = v.trim());

        label("gui.api.wave.field.boss_bar_name");
        editBox(wave.bossBarName, v -> wave.bossBarName = v.trim());

        checkbox("gui.api.wave.field.show_boss_bar", wave.showBossBar, v -> wave.showBossBar = v);
        checkbox("gui.api.wave.field.must_clear", wave.mustClear, v -> wave.mustClear = v);

        // 怪物列表
        button(Component.translatable("gui.api.wave.field.edit_mobs", wave.mobs.size()),
                b -> minecraft.setScreen(new WaveMobListEditorScreen(this, wave)));
    }

    private void rebuild() { rebuildWidgets(); }
}