package net.ankrya.rider_api.mixin.timer.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

@Pseudo
@Mixin(value = {GeoModel.class}, remap = false)
public class GeoModelMixin <T extends GeoAnimatable> {
    @ModifyExpressionValue(method = {"handleAnimations"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isPaused()Z", remap = true)})
    private boolean forcePausedState(boolean original) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 2)
                return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"handleAnimations"}, at = {@At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;shouldPlayAnimsWhileGamePaused()Z")})
    private boolean disableAnimState(boolean original) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 2)
                return false;
        }
        return original;
    }
}
