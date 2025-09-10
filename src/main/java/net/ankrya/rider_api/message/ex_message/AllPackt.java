package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.interfaces.message.INMessage;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.NMessageCreater;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 用于发送网络包至服务器再发送至客户端（仅限于{@link INMessage}的实现类）
 */
public class AllPackt implements INMessage {
    final String clazz;
    final INMessage message;

    public AllPackt(String name, INMessage message) {
        this.clazz = name;
        this.message = message;
        if (this.message instanceof AllPackt)
            throw new RuntimeException("AllPackt can't be AllPackt");
    }

    public AllPackt(INMessage message){
        this(message.getClass().getName(), message);
    }

    public AllPackt(Class<?> clazz, INMessage message) {
        this(clazz.getName(), message);
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        message.toBytes(buf);
        buf.writeUtf(clazz);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(()->{
            if (!(message instanceof AllPackt)) {
                if (ctx.getDirection().getReceptionSide().isClient())
                    MessageLoader.getLoader().sendToPlayersNearby(new NMessageCreater(message), ctx.getSender());
                else message.run(ctx);
            }
        });
    }
}
