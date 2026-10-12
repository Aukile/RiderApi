package net.ankrya.rider_api.interfaces.timer;

import net.minecraft.world.entity.Entity;

public interface TimerClientLevel {
    void rider_api$tickNonPassengerTimeSlow(Entity p_104640_);

    void rider_api$tickPassengerTimeSlow(Entity p_104642_, Entity p_104643_);

    void rider_api$tickEntitiesTimeSlow();

    void rider_api$tickNonPassengerTimePause(Entity p_104640_);

    void rider_api$tickPassengerTimePause(Entity p_104642_, Entity p_104643_);

    void rider_api$tickEntitiesTimePause();

    /** 每帧调用：按各自的单独时速推进实体，realTicks 为这一帧经过的真实 tick 数 */
    void rider_api$tickEntitiesByRate(float realTicks);
}
