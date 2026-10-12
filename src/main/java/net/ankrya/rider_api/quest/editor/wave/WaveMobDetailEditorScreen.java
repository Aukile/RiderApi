package net.ankrya.rider_api.quest.editor.wave;

import net.ankrya.rider_api.quest.editor.EntityPickerScreen;
import net.ankrya.rider_api.quest.editor.QuestEditorData;
import net.ankrya.rider_api.quest.editor.common.ScrollFormScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WaveMobDetailEditorScreen extends ScrollFormScreen {

    private final QuestEditorData.WaveMobData mob;

    public WaveMobDetailEditorScreen(Screen parent, QuestEditorData.WaveMobData mob) {
        super(parent, Component.translatable("gui.api.wave.mob.title"));
        this.mob = mob;
    }

    @Override
    protected void buildForm() {
        label("gui.api.wave.mob.entity");
        EditBox entityBox = editBox(mob.entity, v -> mob.entity = v.trim());
        label("gui.api.wave.mob.pick_entity");
        button(Component.literal("..."), b -> minecraft.setScreen(
                new EntityPickerScreen(this, id -> { mob.entity = id; entityBox.setValue(id); })));

        label("gui.api.wave.mob.count");
        editBoxInt(mob.count, v -> mob.count = Math.max(1, v));

        label("gui.api.wave.mob.custom_name");
        editBox(mob.customName, v -> mob.customName = v.trim());

        label("gui.api.wave.mob.max_health");
        editBoxDouble(mob.maxHealth, v -> mob.maxHealth = v);
        label("gui.api.wave.mob.armor");
        editBoxDouble(mob.armor, v -> mob.armor = v);
        label("gui.api.wave.mob.attack");
        editBoxDouble(mob.attackDamage, v -> mob.attackDamage = v);
        label("gui.api.wave.mob.speed");
        editBoxDouble(mob.movementSpeed, v -> mob.movementSpeed = v);
        label("gui.api.wave.mob.knockback");
        editBoxDouble(mob.knockbackResistance, v -> mob.knockbackResistance = v);
        label("gui.api.wave.mob.follow_range");
        editBoxDouble(mob.followRange, v -> mob.followRange = v);

        label("gui.api.wave.mob.nbt");
        editBox(mob.nbt, v -> mob.nbt = v.isBlank() ? "{}" : v);

        checkbox("gui.api.wave.mob.name_visible", mob.nameVisible, v -> mob.nameVisible = v);
        checkbox("gui.api.wave.mob.persistent", mob.persistent, v -> mob.persistent = v);

        button(Component.translatable("gui.api.wave.mob.edit_effects", mob.effects.size()),
                b -> minecraft.setScreen(new EffectListEditorScreen(this, mob.effects)));
    }
}