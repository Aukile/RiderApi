package net.ankrya.rider_api.data;

public class ModVariable {
    /**攻击冷却变量，int类型*/
    public static final String HIT_COOLING = "hit_cooling";

    /**
     * 添加变量 <br>
     * @see Variables#registerVariable
     */
    public static void init(Variables variables){
        variables.registerVariable(int.class, HIT_COOLING, 0, false);
    }
}
