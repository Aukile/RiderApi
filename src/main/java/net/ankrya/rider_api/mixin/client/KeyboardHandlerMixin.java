package net.ankrya.rider_api.mixin.client;

import net.ankrya.rider_api.init.RiderEffects;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin({KeyboardHandler.class})
public class KeyboardHandlerMixin {
    @Final
    @Shadow private Minecraft minecraft;
    @Inject(method = "keyPress",at = {@At(value = "HEAD")}, cancellable = true)
    public void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci){
        int[] saveKey = new int[]{GLFW.GLFW_KEY_ESCAPE};
        if (minecraft.player != null && minecraft.player.hasEffect(RiderEffects.STUN.get()) && Arrays.stream(saveKey).noneMatch(k -> k == key)) {
            ci.cancel();
        }
    }
}
