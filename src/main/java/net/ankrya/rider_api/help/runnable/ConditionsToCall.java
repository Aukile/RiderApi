package net.ankrya.rider_api.help.runnable;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        this(MinecraftForge.EVENT_BUS, callable, conditions);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END){
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
