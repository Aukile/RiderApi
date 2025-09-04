package net.ankrya.rider_api.api.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * 使用{@link net.ankrya.rider_api.item.base.armor.BaseRiderArmor#equip} 来穿甲时会触发
 */
public class RiderArmorEquipEvent extends Event {
    private final LivingEntity entity;
    private final EquipmentSlot slot;
    private final ItemStack stack;

    public RiderArmorEquipEvent(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        this.entity = entity;
        this.slot = slot;
        this.stack = stack;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    /**
     * 穿装备之<b>前</b>，在这里可以取消<br>
     * @see net.ankrya.rider_api.item.base.armor.BaseRiderArmor#equip
     */
    public static class Pre extends RiderArmorEquipEvent {
        boolean isCanceled = false;
        public Pre(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
            super(entity, slot, stack);
        }

        public boolean canRun() {
            return !isCanceled;
        }

        public void setCanceled(boolean cancel){
            this.isCanceled = cancel;
        }
    }

    /**
     * 穿上装备之<b>后</b><br>
     * @see net.ankrya.rider_api.item.base.armor.BaseRiderArmor#equip
     */
    public static class Post extends RiderArmorEquipEvent {
        public Post(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
            super(entity, slot, stack);
        }
    }
}
