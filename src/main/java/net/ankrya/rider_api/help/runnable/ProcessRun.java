package net.ankrya.rider_api.help.runnable;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class ProcessRun {
    Map<Integer, Runnable> data;
    int tick;
    int maxTick;
    IEventBus bus;

    public ProcessRun(IEventBus bus, Map<Integer, Runnable> data) {
        this.data = data;
        this.bus = bus;
        this.tick = 0;
        this.maxTick = data.keySet().stream().max(Integer::compareTo).orElse(0);
        bus.register(this);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        this.tick++;
        Runnable runnable = data.get(tick);
        if (runnable != null)
            runnable.run();

        if (tick >= maxTick)
            this.bus.unregister(this);
    }
}
