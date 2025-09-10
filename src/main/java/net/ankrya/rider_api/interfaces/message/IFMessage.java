package net.ankrya.rider_api.interfaces.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * @author 八云紫Ender <br>
 * 是大大之前写给我的模组里代码，搬到这里了awa <br>
 * 自动网络包，无需注册，使用{@link net.ankrya.rider_api.message.MessageCreater}创建即可使用 <br>
 * 但是只能用于无参的网络包，且不能写匿名类
 */
public interface IFMessage {
    default void fromBytes(FriendlyByteBuf buf){};

    default void toBytes(FriendlyByteBuf buf){};

    void run(NetworkEvent.Context ctx);
}
