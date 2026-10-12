package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.interfaces.message.IEXMessage;
import net.ankrya.rider_api.message.EXMessageCreater;
import net.ankrya.rider_api.message.MessageLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link IEXMessage}的CTSTC网络包<br>
 * 应该不会用到<br>
 * 留例
 */
public class AllPacktForIEX implements IEXMessage {
    /** 允许由服务器转发的消息类，见 {@link AllPackt#allowRelay} */
    private static final Set<Class<? extends IEXMessage>> RELAY_WHITELIST = ConcurrentHashMap.newKeySet();
    final String clazz;
    final IEXMessage message;

    public AllPacktForIEX(String name, IEXMessage message, boolean hasData, int data) {
        this.clazz = name;
        this.message = message;
    }

    public AllPacktForIEX(IEXMessage message){
        this(message.getClass().getName(), message, true, 2);
    }

    public AllPacktForIEX(String name, IEXMessage message){
        this(name, message, true, 2);
    }

    public AllPacktForIEX(Class<?> clazz, IEXMessage message) {
        this(clazz.getName(), message, true, 2);
    }

    public static void allowRelay(Class<? extends IEXMessage> messageClass){
        RELAY_WHITELIST.add(messageClass);
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        message.toBytes(buf);
        IEXMessage.writeString(buf, clazz);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(()->{
            // 原来判断的是 AllPackt（另一套 INMessage 体系的类），IEXMessage 不可能是它，判断恒为 true
            if (message instanceof AllPacktForIEX) return;
            // 原来的转发逻辑是反的（客户端去调用服务端才能用的 sendToAllTracking），改成和 AllPackt 一致
            if (ctx.getDirection().getReceptionSide().isServer()) {
                Player sender = ctx.getSender();
                if (sender == null) return;
                message.run(ctx);
                if (RELAY_WHITELIST.contains(message.getClass()))
                    MessageLoader.getApiLoader().sendToAllTracking(new EXMessageCreater(message), sender);
                else RiderApi.LOGGER.debug("[RiderApi] AllPacktForIEX: {} is not allowed to relay", message.getClass().getName());
            } else message.run(ctx);
        });
    }

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public int dataLong() {
        return message.dataLong() + 1;
    }
}
