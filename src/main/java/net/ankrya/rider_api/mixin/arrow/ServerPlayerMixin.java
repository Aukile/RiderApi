package net.ankrya.rider_api.mixin.arrow;

import com.mojang.authlib.GameProfile;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.entity.ArrowSource;
import net.ankrya.rider_api.interfaces.ItemToArrow;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Unique
    private static final float riderApi$damage = 1f;
    @Unique
    private static final float riderApi$throwPower = 1f;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float p_251702_, GameProfile gameProfile) {
        super(level, blockPos, p_251702_, gameProfile);
    }

    @Inject(method = {"drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"}, at = @At("HEAD"),cancellable = true)
    public void drop(ItemStack stack, boolean p_9086_, boolean p_9087_, CallbackInfoReturnable<ItemEntity> cir){
        if ((boolean) Variables.getVariable(this, ModVariable.ARROW_DROP_MODE) || stack.getItem() instanceof ItemToArrow){
            LivingEntity shoot = this;
            Level level = shoot.level();
            if (!level.isClientSide()) {
                float damageBonus = 0;
                if (stack.getItem() instanceof TieredItem tieredItem) damageBonus = tieredItem.getTier().getAttackDamageBonus();
                Projectile _entityToSpawn = ArrowSource.shoot(stack,true,level, this, riderApi$damage + damageBonus);
                _entityToSpawn.setPos(shoot.getX(), shoot.getEyeY() - 0.1, shoot.getZ());
                _entityToSpawn.shoot(shoot.getLookAngle().x, shoot.getLookAngle().y, shoot.getLookAngle().z, riderApi$throwPower, 0);
                level.addFreshEntity(_entityToSpawn);
            }
            cir.setReturnValue(null);
        }
    }
}
