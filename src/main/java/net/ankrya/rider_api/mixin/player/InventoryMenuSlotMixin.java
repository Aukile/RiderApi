package net.ankrya.rider_api.mixin.player;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/world/inventory/InventoryMenu$1"})
public class InventoryMenuSlotMixin {

    @Inject(method = {"mayPickup"}, at = {@At("HEAD")}, cancellable = true)
    public void mayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        // 原来 getEntityGameType 在客户端拿不到 PlayerInfo 时返回 null，会空指针；直接用 player.isCreative()
        if (!player.isCreative() && (boolean) Variables.getVariable(player, ModVariable.DISABLE_ARMOR_SLOT)) {
            cir.setReturnValue(false);
        }
    }
}
