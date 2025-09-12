package net.ankrya.rider_api.data;

import net.ankrya.rider_api.api.event.DataInitEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ModVariable {
    /**攻击冷却变量，int类型*/
    public static final String HIT_COOLING = "hit_cooling";
    /**攻击冷却变量，int类型*/
    public static final String TIME_STATUS = "time_status";

    /**
     * 添加变量 <br>
     * @see Variables#registerVariable
     */
    public static void init(Variables variables){
        variables.registerVariable(int.class, HIT_COOLING, 0, false);
        variables.registerVariable(int.class, TIME_STATUS, 0, false);
        NeoForge.EVENT_BUS.post(new DataInitEvent(variables));
    }
}
