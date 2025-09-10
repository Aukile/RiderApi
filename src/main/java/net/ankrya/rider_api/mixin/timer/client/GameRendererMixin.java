package net.ankrya.rider_api.mixin.timer.client;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.interfaces.timer.TimerGamerRenderer;
import net.ankrya.rider_api.mixin.accessor.MinecraftAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import javax.annotation.Nullable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements TimerGamerRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private LightTexture lightTexture;

    @Shadow @Final private Camera mainCamera;

    @Shadow protected abstract void tickFov();

    @Shadow private int tick;

    @Shadow @Final public ItemInHandRenderer itemInHandRenderer;

    @Shadow private float darkenWorldAmountO;

    @Shadow private float darkenWorldAmount;

    @Shadow private int itemActivationTicks;

    @Shadow @Nullable private ItemStack itemActivationItem;

//    @Inject(method = "tick",at = @At("HEAD"),cancellable = true)
//    public void tick(CallbackInfo ci) {
//        ClientLevel level = this.minecraft.level;
//        if (level != null) {
//            int time_state = (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
//            if (time_state == 2) {
//                if (GJ.TimerControl.isPauseEntity(this.minecraft.player))
//                    rider_api$tickSpecial();
//                ci.cancel();
//
//
//            }
//        }
//    }

    @Unique
    @Override
    public void rider_api$tickSpecial(){
        this.tickFov();

        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.mainCamera.tick();

        this.itemInHandRenderer.tick();

        this.darkenWorldAmountO = this.darkenWorldAmount;
        if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
            this.darkenWorldAmount += 0.05F;
            if (this.darkenWorldAmount > 1.0F) {
                this.darkenWorldAmount = 1.0F;
            }
        } else if (this.darkenWorldAmount > 0.0F) {
            this.darkenWorldAmount -= 0.0125F;
        }

        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightTexture;updateLightTexture(F)V"))
    private float updateLightTexture(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        return (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2 ? 0.0F : original;
    }

    @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"))
    private float pick(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        Entity entity = this.minecraft.player;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && !GJ.TimerControl.isPauseEntity(entity) )
            return 0.0F;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && GJ.TimerControl.isPauseEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1  && GJ.TimerControl.isSlowEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        return original;
    }

    @ModifyArg(method = {"renderLevel"}, index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V"))
    private float renderLevel(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        if ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2)
            return 0.0F;
        return original;
    }

    @ModifyArg(method = {"renderLevel"}, index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private float bobHurt(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        Entity entity = this.minecraft.player;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2 && !GJ.TimerControl.isPauseEntity(entity) )
            return 0.0F;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && GJ.TimerControl.isPauseEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1  && GJ.TimerControl.isSlowEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        return original;
    }



    @ModifyArg(method = {"renderLevel"}, index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"))
    private float setup(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        Entity entity = this.minecraft.player;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2 && !GJ.TimerControl.isPauseEntity(entity) )
            return 0.0F;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && GJ.TimerControl.isPauseEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1  && GJ.TimerControl.isSlowEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        return original;
    }

    @ModifyArg(method = {"renderLevel"}, index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V"))
    private float renderItemInHand(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2&& !GJ.TimerControl.isPauseEntity(entity) )
            return 0.0F;
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && GJ.TimerControl.isPauseEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1  && GJ.TimerControl.isSlowEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        return original;
    }

    @ModifyArg(method = {"render", "renderLevel"}, index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float renderConfusionOverlay(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && GJ.TimerControl.isPauseEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1  && GJ.TimerControl.isSlowEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        return (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2 ? 0.0F : original;
    }

    @ModifyArg(method = {"render"}, index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"))
    private float renderItemActivationAnimation(float original) {
        ClientLevel level = this.minecraft.level;
        if (level == null)
            return original;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1  && GJ.TimerControl.isSlowEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        if (entity != null && (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2  && GJ.TimerControl.isPauseEntity(entity) )
        {
            return  ((MinecraftAccessor) minecraft).getTimer() instanceof  ITimer ITimer ? ITimer.rider_api$partialTick() : original;
        }
        return (int) Variables.getVariable(level, ModVariable.TIME_STATUS) ==2 ? 0.0F : original;
    }

    @ModifyArg(method = "render",at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"),index = 4,remap = false)
    private float renderDeltaFrame1(float original) {
        Timer rider_api$timer = ((MinecraftAccessor) Minecraft.getInstance()).getTimer();
        if (rider_api$timer != null){
            ClientLevel level = this.minecraft.level;
            if (level != null && minecraft.player != null){
                ITimer ITimer = (ITimer) rider_api$timer;
                if ( ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1 && GJ.TimerControl.isSlowEntity(minecraft.player))
                        || ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 2 && GJ.TimerControl.isPauseEntity(minecraft.player) ))
                    return ITimer.rider_api$tickDelta();
            }
        }
        return original;
    }

    @ModifyArg(method = "render",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Overlay;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"),index = 3)
    private float renderDeltaFrame2(float original) {
        Timer rider_api$timer = ((MinecraftAccessor) Minecraft.getInstance()).getTimer();
        if (rider_api$timer != null){
            ClientLevel level = this.minecraft.level;
            if (level != null && minecraft.player != null){
                ITimer ITimer = (ITimer) rider_api$timer;
                if ( ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1 && GJ.TimerControl.isSlowEntity(minecraft.player))
                        || ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 2 && GJ.TimerControl.isPauseEntity(minecraft.player) ))
                    return ITimer.rider_api$tickDelta();
            }
        }
        return original;
    }

    @ModifyArg(method = "render",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"),index = 1)
    private float renderFrameTime1(float original) {
        Timer rider_api$timer = ((MinecraftAccessor) Minecraft.getInstance()).getTimer();
        if (rider_api$timer != null){
            ClientLevel level = this.minecraft.level;
            if (level != null && minecraft.player != null){
                ITimer ITimer = (ITimer) rider_api$timer;
                if ( ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 1 && GJ.TimerControl.isSlowEntity(minecraft.player))
                        || ((int) Variables.getVariable(level, ModVariable.TIME_STATUS) == 2 && GJ.TimerControl.isPauseEntity(minecraft.player) ))
                    return ITimer.rider_api$partialTick();
            }
        }
        return original;
    }
}
