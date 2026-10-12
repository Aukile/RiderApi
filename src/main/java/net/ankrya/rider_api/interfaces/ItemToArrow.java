package net.ankrya.rider_api.interfaces;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 实现这个接口的物品在玩家按 Q 丢出时会被投掷出去（见 ServerPlayerMixin）。<br>
 * 下面的方法都可以重写来控制投掷行为
 */
public interface ItemToArrow {
    /** 返回 false 时按普通物品丢出 */
    default boolean startThrow(LivingEntity shoot, ItemStack stack){
        return true;
    }

    /** 命中后是否可以捡回 */
    default boolean pick(LivingEntity shoot, ItemStack stack){
        return true;
    }

    default float damage(LivingEntity shoot, ItemStack stack, float damageBonus){
        return damageBonus;
    }

    default int knockback(LivingEntity shoot, ItemStack stack){
        return 1;
    }

    default Vec3 shootPos(LivingEntity shoot, ItemStack stack){
        return new Vec3(shoot.getX(), shoot.getEyeY() - 0.1, shoot.getZ());
    }

    default Vec3 angle(LivingEntity shoot, ItemStack stack){
        return shoot.getLookAngle();
    }

    default float throwPower(LivingEntity shoot, ItemStack stack){
        return 1f;
    }

    default void onShoot(LivingEntity shoot, ItemStack stack, Projectile entityToSpawn){
        stack.hurtAndBreak(1, shoot, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }
}
