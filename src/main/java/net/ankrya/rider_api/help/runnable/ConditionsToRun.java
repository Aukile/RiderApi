package net.ankrya.rider_api.help.runnable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

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

    /**
     * conditions提供的int为运行刻
     * @param runnable 运行
     * @param conditions 条件
     */
    public ConditionsToRun(Runnable runnable, Predicate<Integer> conditions) {
        this(NeoForge.EVENT_BUS, runnable, conditions);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (event.hasTime()) {
            this.tick++;
            if (conditions.test(tick)) {
                this.runnable.run();
                this.bus.unregister(this);
            }
        }
    }
}
