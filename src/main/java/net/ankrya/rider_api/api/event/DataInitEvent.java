package net.ankrya.rider_api.api.event;

import net.ankrya.rider_api.data.VariableSerializer;
import net.ankrya.rider_api.data.Variables;
import net.minecraftforge.eventbus.api.Event;

/**
 * 注册同步数据的事件
 */
public class DataInitEvent extends Event {
    private final Variables variables;

    public DataInitEvent(Variables variables) {
        this.variables = variables;
    }

    /**
     * 注册同步的数据<br>
     * 能支持的数据类型看{@link VariableSerializer}
     * @param clazz 类型
     * @param name 名称
     * @param defaultValue 默认值
     * @param save 是否保存（玩家死亡是否保存/世界之间维度间是否同步）
     */
    public <T> void register(Class<T> clazz, String name, T defaultValue, boolean save){
        variables.registerVariable(clazz, name, defaultValue, save);
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
