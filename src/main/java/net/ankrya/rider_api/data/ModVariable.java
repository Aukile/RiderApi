package net.ankrya.rider_api.data;

public class ModVariable {
    /**攻击冷却变量，int类型*/
    public static final String HIT_COOLING = "hit_cooling";
    /**时停控制器，int类型*/
    public static final String TIME_STATUS = "time_status";
    /**禁用控制，boolean类型*/
    public static final String DISABLE_CONTROL = "disable_control";

    /**
     * 添加变量 <br>
     * @see Variables#registerVariable
     */
    public static void init(Variables variables){
        variables.registerVariable(int.class, HIT_COOLING, 0, false);
        variables.registerVariable(int.class, TIME_STATUS, 0, false);
        variables.registerVariable(boolean.class, DISABLE_CONTROL, false, false);
    }
}
