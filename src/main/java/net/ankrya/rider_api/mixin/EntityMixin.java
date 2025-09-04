package net.ankrya.rider_api.mixin;

import net.ankrya.rider_api.client.sound.LoopSound;
import net.ankrya.rider_api.interfaces.inside_use.ISoundMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public class EntityMixin implements ISoundMap {
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
}
