package net.ankrya.rider_api.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoopSound extends AbstractTickableSoundInstance {
    final Entity entity;

    public LoopSound(Entity entity, ResourceLocation soundLocation, SoundSource soundSource, boolean loop, float range) {
        super(SoundEvent.createFixedRangeEvent(soundLocation, range), soundSource, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = loop;
        this.delay = 0;
        this.volume = 1.0F;
        if (entity != null) {
            this.x = (float) entity.getX();
            this.y = (float) entity.getY();
            this.z = (float) entity.getZ();
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
    }

    public void updatePos() {
        if (this.entity != null) {
            this.x = (float) this.entity.getX();
            this.y = (float) this.entity.getY();
            this.z = (float) this.entity.getZ();
        }
    }

    /**
     * 原来的计算是 length / length + 0.01，实体静止时是 0/0 = NaN（声音异常），运动时恒为 1.01。
     * 主分支已不再每 tick 调用，这里保留方法以兼容外部调用，并去掉 NaN
     */
    @Deprecated
    public void updateSound() {
        this.pitch = 1.0F;
    }

    @Override
    public void tick() {
        if (this.entity == null || !this.entity.isAlive()) {
            stopSound();
            return;
        }

        if (isStopped()) {
            return;
        }

        updatePos();
    }

    public void stopSound() {
        stop();
    }
}