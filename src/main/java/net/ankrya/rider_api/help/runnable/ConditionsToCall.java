package net.ankrya.rider_api.help.runnable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

public class ConditionsToCall<T>{
    Predicate<Integer> conditions;
    FutureTask<T> futureTask;
    int tick;
    IEventBus bus;

    public ConditionsToCall(IEventBus bus, Callable<T> callable, Predicate<Integer> conditions) {
        this.conditions = conditions;
        this.bus = bus;
        this.tick = 0;
        futureTask = new FutureTask<>(callable);
        bus.register(this);
    }

    public ConditionsToCall(Callable<T> callable, Predicate<Integer> conditions) {
        this(NeoForge.EVENT_BUS, callable, conditions);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (event.hasTime()){
            if (conditions.test(tick)) {
                getFutureTask().run();
            }
            tick++;
        }
    }

    public FutureTask<T> getFutureTask() {
        return futureTask;
    }
}
