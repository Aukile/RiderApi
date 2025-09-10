package net.ankrya.rider_api.help.runnable;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        this(runnable, tick, interval, MinecraftForge.EVENT_BUS);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.tick = (this.tick > 0) ? (this.tick - 1) : this.tick;
            if (this.tick % this.interval == 0.0F)
                this.runnable.run();
            if (this.tick == 0)
                this.bus.unregister(this);
        }
    }
}
