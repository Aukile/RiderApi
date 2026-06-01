package net.ankrya.rider_api.data;

import net.ankrya.rider_api.api.event.DataInitEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ModVariable {
    /**攻击冷却变量，int类型*/
    public static final String HIT_COOLING = "hit_cooling";
    /**攻击冷却变量，int类型*/
    public static final String TIME_STATUS = "time_status";
    /**禁止移动，boolean类型*/
    public static final String DISABLE_MOVE = "disable_move";
    /**弹射物形式丢弃模式，boolean类型*/
    public static final String ARROW_DROP_MODE = "arrow_drop_mode";
    /**禁止盔甲槽位，boolean类型*/
    public static final String DISABLE_ARMOR_SLOT = "disable_armor_slot";
    /**时间减慢时速，float类型*/
    public static final String TIME_SLOW_TIMER = "time_slow_timer";

    /**
     * 添加变量 <br>
     * @see Variables#registerVariable
     */
    public static void init(Variables variables){
        variables.registerVariable(int.class, HIT_COOLING, 0, false);
        variables.registerVariable(int.class, TIME_STATUS, 0, false);
        variables.registerVariable(boolean.class, DISABLE_MOVE, false, false);
        variables.registerVariable(boolean.class, ARROW_DROP_MODE, false, false);
        variables.registerVariable(boolean.class, DISABLE_ARMOR_SLOT, false, false);
        variables.registerVariable(float.class, TIME_SLOW_TIMER, 500.0f, true);
        NeoForge.EVENT_BUS.post(new DataInitEvent(variables));
    }
}
