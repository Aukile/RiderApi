package net.ankrya.rider_api.api.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

public class LinerServerTickEvent extends TickEvent {
    public LinerServerTickEvent(Type type, LogicalSide side, Phase phase) {
        super(type, side, phase);
    }
}
