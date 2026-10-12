package net.ankrya.rider_api.mixin.client;

import net.ankrya.rider_api.init.RiderEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void zzz$banArmorPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (player.hasEffect(RiderEffects.STUN.get())) {
			cir.setReturnValue(false);
		}
	}
}