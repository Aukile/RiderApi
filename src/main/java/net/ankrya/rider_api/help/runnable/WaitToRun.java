package net.ankrya.rider_api.help.runnable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class WaitToRun {
    Runnable runnable;

    int delayTick;

    IEventBus bus;

    public WaitToRun(IEventBus bus, Runnable runnable, int delayTick) {
        this.runnable = runnable;
        this.delayTick = delayTick;
        this.bus = bus;
        bus.register(this);
    }

    public WaitToRun(Runnable runnable, int delayTick) {
        this(NeoForge.EVENT_BUS, runnable, delayTick);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (event.hasTime()) {
            this.delayTick = (this.delayTick > 0) ? (this.delayTick - 1) : 0;
            if (this.delayTick <= 0) {
                this.runnable.run();
                this.bus.unregister(this);
            }
        }
    }
}

