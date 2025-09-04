package net.ankrya.rider_api.interfaces.inside_use;

import net.ankrya.rider_api.client.sound.LoopSound;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ISoundMap {
    Map<ResourceLocation, LoopSound> rider$getLoopSounds();
    boolean rider$containsLoopSound(ResourceLocation id);
    void rider$addLoopSound(ResourceLocation id, LoopSound sound);
    void rider$removeLoopSound(ResourceLocation id);
}
