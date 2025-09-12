package net.ankrya.rider_api.api.event;

import net.ankrya.rider_api.data.Variables;
import net.neoforged.bus.api.Event;

public class DataInitEvent extends Event {
    private final Variables variables;

    public DataInitEvent(Variables variables) {
        this.variables = variables;
    }

    public <T> void register(Class<T> clazz, String name, T defaultValue, boolean save) {
        this.variables.registerVariable(clazz, name, defaultValue, save);
    }

    public boolean isCancelable() {
        return false;
    }
}
