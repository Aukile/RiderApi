package net.ankrya.rider_api.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ArmorBrokenEvent extends Event {

    private final LivingEntity entity;
    private final ItemStack stack;

    public ArmorBrokenEvent(LivingEntity entity, ItemStack stack) {
        this.entity = entity;
        this.stack = stack;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ItemStack getStack() {
        return stack;
    }
}
