package net.ankrya.rider_api.mixin;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/world/inventory/ArmorSlot"})
public class ArmorPickSlotMixin {
    @Inject(method = {"mayPickup"}, at = {@At("HEAD")}, cancellable = true)
    public void mayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if ((boolean) Variables.getVariable(player, ModVariable.DISABLE_ARMOR_SLOT) && !GJ.ToPlayer.getEntityGameType(player).isCreative()) {
            cir.setReturnValue(false);
        }
    }
}
