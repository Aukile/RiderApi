package net.ankrya.rider_api.help.runnable;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Predicate;

public class ConditionsToRun {
    Predicate<Integer> conditions;
    Runnable runnable;
    int tick;
    IEventBus bus;

    public ConditionsToRun(IEventBus bus, Runnable runnable, Predicate<Integer> conditions) {
        this.conditions = conditions;
        this.runnable = runnable;
        this.bus = bus;
        this.tick = 0;
        bus.register(this);
    }

    public ConditionsToRun(Runnable runnable, Predicate<Integer> conditions) {
        this(MinecraftForge.EVENT_BUS, runnable, conditions);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.tick++;
            if (conditions.test(tick)) {
                this.runnable.run();
                this.bus.unregister(this);
            }
        }
    }
}
