package net.ankrya.rider_api.help.runnable;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        this(MinecraftForge.EVENT_BUS, runnable, delayTick);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.delayTick = (this.delayTick > 0) ? (this.delayTick - 1) : 0;
            if (this.delayTick <= 0) {
                this.runnable.run();
                this.bus.unregister(this);
            }
        }
    }
}

