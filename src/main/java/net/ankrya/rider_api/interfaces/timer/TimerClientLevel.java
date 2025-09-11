package net.ankrya.rider_api.interfaces.timer;

import net.minecraft.world.entity.Entity;

public interface TimerClientLevel {
    void rider_api$tickNonPassengerTimeSlow(Entity p_104640_);

    void rider_api$tickPassengerTimeSlow(Entity p_104642_, Entity p_104643_);

    void rider_api$tickEntitiesTimeSlow();

    void rider_api$tickNonPassengerTimePause(Entity p_104640_);

    void rider_api$tickPassengerTimePause(Entity p_104642_, Entity p_104643_);

    void rider_api$tickEntitiesTimePause();
}
