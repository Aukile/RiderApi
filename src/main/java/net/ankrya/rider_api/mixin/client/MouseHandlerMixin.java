package net.ankrya.rider_api.mixin.client;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "turnPlayer",at = @At("HEAD"),cancellable = true)
    public void turnPlayer(CallbackInfo ci) {
        if (this.minecraft != null){
            if (this.minecraft.level != null){
                int timer_state = Variables.getVariable(this.minecraft.level, ModVariable.TIME_STATUS);
                if (timer_state == 2){
                    if (!GJ.TimerControl.isPauseEntity(this.minecraft.player)){
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onScroll",at = @At("HEAD"),cancellable = true)
    public void onScroll(long p_91527_, double p_91528_, double p_91529_,CallbackInfo ci) {
        if (this.minecraft != null){
            if (this.minecraft.level != null){
                Player player = minecraft.player;
                boolean flag = Variables.getVariable(player, ModVariable.DISABLE_CONTROL);
                if(flag) ci.cancel();
            }
        }
    }
}
