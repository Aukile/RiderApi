package net.ankrya.rider_api.interfaces.timer;

import org.jetbrains.annotations.ApiStatus;

/**
 * 由 {@code EntityTimeRateMixin} 混入到所有 Entity 上，保存“单独时速”用的累加器。<br>
 * 一般不需要直接用，设置速率请用 {@link net.ankrya.rider_api.help.GJ.TimerControl#setEntityTimeRate}
 */
@ApiStatus.Internal
public interface ITimeRateEntity {
    /** 距离下一次 tick 的进度（0~1），客户端同时用作渲染的 partialTick */
    float rider_api$getTimeAcc();

    void rider_api$setTimeAcc(float acc);

    /** 上一次累加时的时间戳（服务端为服务器 tick 数），防止同一 tick 内重复累加 */
    long rider_api$getTimeStamp();

    void rider_api$setTimeStamp(long stamp);
}
