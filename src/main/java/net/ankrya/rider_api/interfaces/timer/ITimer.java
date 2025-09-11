package net.ankrya.rider_api.interfaces.timer;

import org.spongepowered.asm.mixin.Unique;

public interface ITimer {
    int timeNormal = 0;
    int timeStop = 1;
    int timeSlow = 2;
    int riderApi$advanceTime(long time);

    float riderApi$deltaTick();

    @Unique
    float riderApi$tickDelta();

    void setRiderApi$deltaTick(float deltaTick);
}
