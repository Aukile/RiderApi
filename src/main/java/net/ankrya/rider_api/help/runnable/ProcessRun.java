package net.ankrya.rider_api.help.runnable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

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
        this.maxTick = data.keySet().stream().max(Integer::compareTo).get();
        bus.register(this);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (event.hasTime()) {
            this.tick++;
            for (Map.Entry<Integer, Runnable> entry : data.entrySet())
                if (entry.getKey() == tick)
                    entry.getValue().run();

            if (tick >= maxTick)
                this.bus.unregister(this);
        }
    }
}
