package net.ankrya.rider_api.help;

import net.ankrya.rider_api.interfaces.timer.ITimeRateEntity;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

/**
 * 单独时速的内部工具（时缓 mixin 用）。<br>
 * 原理：每个实体有一个累加器 acc，每经过 1 个“真实 tick”累加 rate，满 1 就 tick 一次。<br>
 * 服务端按服务器 tick 累加；客户端按每帧经过的真实时间累加，
 * 并把 acc 本身当作这个实体的 partialTick 去渲染，所以不同速率的实体都能平滑插值，不会一卡一卡。
 */
@ApiStatus.Internal
public final class TimeRateHelper {
    /** 单次（一个服务器 tick / 一帧）最多补几次 tick，防止卡顿后瞬间补太多 */
    public static final int MAX_STEPS = 10;
    /** 速率上限（相对正常速度） */
    public static final float MAX_RATE = 10f;

    private TimeRateHelper() {}

    /** 同一个时间戳只允许累加一次，返回 false 表示这个 tick 已经处理过了 */
    public static boolean markTicked(Entity entity, long stamp) {
        ITimeRateEntity timed = (ITimeRateEntity) entity;
        if (timed.rider_api$getTimeStamp() == stamp) return false;
        timed.rider_api$setTimeStamp(stamp);
        return true;
    }

    /** 累加 amount，返回这次需要 tick 的次数（已限制最大值，超出部分直接丢弃） */
    public static int consume(Entity entity, float amount) {
        ITimeRateEntity timed = (ITimeRateEntity) entity;
        float acc = timed.rider_api$getTimeAcc() + Math.max(amount, 0f);
        int steps = (int) acc;
        acc -= steps;
        timed.rider_api$setTimeAcc(acc);
        return Math.min(steps, MAX_STEPS);
    }

    /** 客户端渲染这个实体时应使用的 partialTick */
    public static float partialTick(Entity entity) {
        float acc = ((ITimeRateEntity) entity).rider_api$getTimeAcc();
        return acc < 0f ? 0f : Math.min(acc, 1f);
    }
}
