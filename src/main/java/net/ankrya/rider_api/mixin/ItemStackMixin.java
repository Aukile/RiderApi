package net.ankrya.rider_api.mixin;

import net.ankrya.rider_api.api.event.ArmorBrokenEvent;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 1))
    public void hurtAndBreak(int damage, ServerLevel level, @Nullable LivingEntity entity, Consumer<Item> itemConsumer, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        NeoForge.EVENT_BUS.post(new ArmorBrokenEvent(entity, itemStack));
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        if (entity instanceof Player player){
            ItemStack itemStack = (ItemStack) (Object) this;
            boolean remove = GJ.ToItem.getNbt(itemStack).getBoolean(GJ.ToItem.REMOVE);
            if (remove) GJ.ToItem.playerRemoveItem(player, itemStack, 64);
        }
    }
}
