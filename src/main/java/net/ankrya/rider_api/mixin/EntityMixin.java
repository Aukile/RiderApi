package net.ankrya.rider_api.mixin;

import net.ankrya.rider_api.client.sound.LoopSound;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.interfaces.inside_use.ISoundMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements ISoundMap {
    @Shadow public abstract void setDeltaMovement(Vec3 vec3);

    @Unique
    public Map<ResourceLocation, LoopSound> rider$loopSounds = new HashMap<>();

    @Override
    public Map<ResourceLocation, LoopSound> rider$getLoopSounds() {
        return rider$loopSounds;
    }

    @Override
    public boolean rider$containsLoopSound(ResourceLocation id) {
        return rider$loopSounds.containsKey(id);
    }

    @Override
    public void rider$addLoopSound(ResourceLocation id, LoopSound sound) {
        rider$loopSounds.put(id, sound);
    }

    @Override
    public void rider$removeLoopSound(ResourceLocation id) {
        if (rider$loopSounds.containsKey(id)){
            rider$loopSounds.get(id).stopSound();
            rider$loopSounds.remove(id);
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType moverType, Vec3 vec3, CallbackInfo ci) {
        if (Variables.getVariable((Entity) (Object) this, ModVariable.DISABLE_MOVE)){
            ci.cancel();
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void turn(double xRot, double yRot, CallbackInfo ci) {
        if (Variables.getVariable((Entity) (Object) this, ModVariable.DISABLE_MOVE)){
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (Variables.getVariable((Entity) (Object) this, ModVariable.DISABLE_MOVE)){
            this.setDeltaMovement(Vec3.ZERO);
        }
    }
}
