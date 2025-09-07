package net.ankrya.rider_api.interfaces.timer;

import org.spongepowered.asm.mixin.Unique;

public interface ITimer {
    int riderApi$advanceTime(long time);

    float riderApi$deltaTick();

    @Unique
    float riderApi$tickDelta();

    void setRiderApi$deltaTick(float deltaTick);
}
