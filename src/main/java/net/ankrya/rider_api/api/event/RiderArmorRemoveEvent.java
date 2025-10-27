package net.ankrya.rider_api.api.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * 使用{@link net.ankrya.rider_api.item.base.armor.BaseRiderArmor#unequip} 解除变身时触发
 */
public class RiderArmorRemoveEvent extends Event {
    private final LivingEntity entity;
    private final EquipmentSlot slot;
    private final ItemStack stack;

    public RiderArmorRemoveEvent(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
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
     * 脱装备之<b>前</b>,可取消
     * @see net.ankrya.rider_api.item.base.armor.BaseRiderArmor#unequip
     */
    public static class Pre extends RiderArmorRemoveEvent {
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
     * 脱装备之<b>后</b>
     * @see net.ankrya.rider_api.item.base.armor.BaseRiderArmor#unequip
     */
    public static class Post extends RiderArmorRemoveEvent {
        public Post(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
            super(entity, slot, stack);
        }
    }
}
