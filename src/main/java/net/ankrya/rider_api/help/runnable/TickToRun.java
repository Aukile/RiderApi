package net.ankrya.rider_api.help.runnable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickToRun {
    Runnable runnable;

    int tick;

    float interval;

    IEventBus bus;

    public TickToRun(Runnable runnable, int tick, float interval, IEventBus bus) {
        this.runnable = runnable;
        this.tick = tick;
        this.interval = interval;
        this.bus = bus;
        bus.register(this);
    }

    public TickToRun(Runnable runnable, int tick, float interval) {
        this(runnable, tick, interval, NeoForge.EVENT_BUS);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (event.hasTime()) {
            this.tick = (this.tick > 0) ? (this.tick - 1) : this.tick;
            if (this.tick % this.interval == 0.0F)
                this.runnable.run();
            if (this.tick == 0)
                this.bus.unregister(this);
        }
    }
}
