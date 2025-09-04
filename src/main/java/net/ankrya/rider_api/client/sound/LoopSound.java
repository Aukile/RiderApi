package net.ankrya.rider_api.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class LoopSound extends AbstractTickableSoundInstance {
    final Entity entity;

    public LoopSound(Entity entity, ResourceLocation soundLocation, SoundSource soundSource, boolean loop, float range) {
        super(SoundEvent.createFixedRangeEvent(soundLocation ,range), soundSource, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = loop;
        this.delay = 0;
    }

    public void updatePos() {
        this.x = (float)this.entity.getX();
        this.y = (float)this.entity.getY();
        this.z = (float)this.entity.getZ();
    }

    public void updateSound() {
        this.pitch = (float)(this.entity.getDeltaMovement().length() / this.entity.getDeltaMovement().length() + 0.01D);
    }

    public void tick() {
        if (isStopped())
            return;
        if (this.entity == null || !this.entity.isAlive()) {
            stopSound();
            return;
        }
        updateSound();
        updatePos();
    }

    public void stopSound(){
        stop();
    }
}
