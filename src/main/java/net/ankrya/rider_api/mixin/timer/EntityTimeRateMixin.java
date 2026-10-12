package net.ankrya.rider_api.mixin.timer;

import net.ankrya.rider_api.interfaces.timer.ITimeRateEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** 给每个实体加上“单独时速”的累加器字段，见 {@link ITimeRateEntity} */
@Mixin(Entity.class)
public abstract class EntityTimeRateMixin implements ITimeRateEntity {
    @Unique
    private float rider_api$timeAcc = 0f;
    @Unique
    private long rider_api$timeStamp = Long.MIN_VALUE;

    @Override
    public float rider_api$getTimeAcc() {
        return rider_api$timeAcc;
    }

    @Override
    public void rider_api$setTimeAcc(float acc) {
        this.rider_api$timeAcc = acc;
    }

    @Override
    public long rider_api$getTimeStamp() {
        return rider_api$timeStamp;
    }

    @Override
    public void rider_api$setTimeStamp(long stamp) {
        this.rider_api$timeStamp = stamp;
    }
}
