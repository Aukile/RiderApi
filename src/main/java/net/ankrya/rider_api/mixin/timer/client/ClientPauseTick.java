package net.ankrya.rider_api.mixin.timer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.mixin.accessor.MinecraftAccessor;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

public class ClientPauseTick {

    @Mixin({LevelRenderer.class})
    public static abstract class LevelRendererMixin {
        @Shadow @Nullable private ClientLevel level;

        @Shadow @Final private Minecraft minecraft;

        @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

        @Inject(method = {"tick", "tickRain"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            if (level != null) {
                int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
                if (time_state == 2) {
                    ci.cancel();
                }
            }
        }



        @ModifyArg(method = {"renderLevel"}, index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
        private float renderSnowAndRain(float original) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null)
                return original;
            return (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2 ? 0.0F : original;
        }

        @Inject(method = "renderEntity",at = @At("HEAD"),cancellable = true)
        private void renderEntity(Entity p_109518_, double p_109519_, double p_109520_, double p_109521_, float p_109522_, PoseStack p_109523_, MultiBufferSource p_109524_, CallbackInfo ci) {
            if (this.level != null) {
                if (((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 2 && GJ.TimerControl.isPauseEntity(p_109518_))
                        || ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1 && GJ.TimerControl.isSlowEntity(p_109518_) ) ){

                    ITimer ITimer = (ITimer) ((MinecraftAccessor)minecraft).getTimer();
                    float patialTick = ITimer.rider_api$partialTick();
                    double d0 = Mth.lerp((double)patialTick, p_109518_.xOld, p_109518_.getX());
                    double d1 = Mth.lerp((double)patialTick, p_109518_.yOld, p_109518_.getY());
                    double d2 = Mth.lerp((double)patialTick, p_109518_.zOld, p_109518_.getZ());
                    float f = Mth.lerp(patialTick, p_109518_.yRotO, p_109518_.getYRot());
                    this.entityRenderDispatcher.render(p_109518_, d0 - p_109519_, d1 - p_109520_, d2 - p_109521_, f, patialTick, p_109523_, p_109524_, this.entityRenderDispatcher.getPackedLightCoords(p_109518_, p_109522_));
                    ci.cancel();
                }
            }
        }
    }

    @Mixin({LightTexture.class})
    public static abstract class LightTextureMixin {
        @Shadow @Final private Minecraft minecraft;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            if (this.minecraft != null){
                if (this.minecraft.level != null){
                    int time_state = Variables.getVariable(this.minecraft.level, ModVariable.TIME_STATUS);
                    if (time_state == 2) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Mixin({TextureManager.class})
    public static abstract class TextureManagerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null){
                int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
                if (time_state == 2) {
                    ci.cancel();
                }
            }
        }
    }

    @Mixin({Camera.class})
    public static abstract class CameraMixin {
        @Shadow private Entity entity;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level != null){
                int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
                if(time_state == 2 && !GJ.TimerControl.isPauseEntity(entity)){
                    ci.cancel();
                }
            }

        }
    }

    @Mixin({ItemInHandRenderer.class})
    public static abstract class ItemInHandRendererMixin{
        @Final
        @Shadow private  Minecraft minecraft;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            ClientLevel level = minecraft.level;
            if (level != null)
            {
                int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
                if(time_state == 2 && this.minecraft.player != null && !GJ.TimerControl.isPauseEntity(this.minecraft.player)){
                    ci.cancel();
                }
            }


        }
    }

    @Mixin({RenderStateShard.class})
    public static abstract class RenderStateShardMixin {
        @Unique
        private static long timeclock$millis = Util.getMillis();

        @Redirect(method = {"setupGlintTexturing"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"))
        private static long modifyGetMillis() {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level == null)
                return Util.getMillis();
            if ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1) {
                timeclock$millis = Util.getMillis() * 2L / 20L;
            }
            return timeclock$millis;
        }
    }

    @Mixin({MusicManager.class})
    public static abstract class MusicManagerMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level != null)
            {
                int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
                if(time_state == 2){
                    ci.cancel();
                }
            }
        }
    }

    @Mixin({SoundEngine.class})
    public static abstract class SoundEngineMixin {
        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void pauseTick(CallbackInfo ci) {
            ClientLevel level = (Minecraft.getInstance()).level;
            if (level != null)
            {
                int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
                if(time_state == 2){
                    ci.cancel();
                }
            }
        }
    }

}
