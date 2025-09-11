package net.ankrya.rider_api.interfaces.timer;

import org.spongepowered.asm.mixin.Unique;

public interface ITimer {
    int timeStop = 2;
    int timeSlow = 1;
    int rider_api$advanceTime(long p_92526_);

    float rider_api$partialTick();

    @Unique
    float rider_api$tickDelta();

    void setrider_api$partialTick(float partialTick);
}
