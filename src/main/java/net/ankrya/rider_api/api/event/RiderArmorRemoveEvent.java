package net.ankrya.rider_api.api.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 使用{@link net.ankrya.rider_api.item.base.armor.BaseRiderArmor#unequip} 解除变身时触发
 */
public class RiderArmorRemoveEvent extends Event {
    private final LivingEntity entity;
    private final EquipmentSlot slot;

    public RiderArmorRemoveEvent(LivingEntity entity, EquipmentSlot slot) {
        this.entity = entity;
        this.slot = slot;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }


    /**
     * 脱装备之<b>前</b>,可取消
     * @see net.ankrya.rider_api.item.base.armor.BaseRiderArmor#unequip
     */
    public static class Pre extends RiderArmorRemoveEvent {

        public Pre(LivingEntity entity, EquipmentSlot slot) {
            super(entity, slot);
        }
    }

    /**
     * 脱装备之<b>后</b>
     * @see net.ankrya.rider_api.item.base.armor.BaseRiderArmor#unequip
     */
    public static class Post extends RiderArmorRemoveEvent {
        public Post(LivingEntity entity, EquipmentSlot slot) {
            super(entity, slot);
        }
    }
}
