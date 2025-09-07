package net.ankrya.rider_api.interfaces.timer;

import java.util.function.BooleanSupplier;

public interface TimerServerLevel {
    void rider_api$specialEntityTimeSlow(BooleanSupplier booleanSupplier);

    void rider_api$specialEntityTimePause(BooleanSupplier booleanSupplier);
}
