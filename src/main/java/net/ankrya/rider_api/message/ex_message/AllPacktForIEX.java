package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.interfaces.message.IEXMessage;
import net.ankrya.rider_api.message.EXMessageCreater;
import net.ankrya.rider_api.message.MessageLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * {@link IEXMessage}的CTSTC网络包<br>
 * 应该不会用到<br>
 * 留例
 */
public class AllPacktForIEX implements IEXMessage {
    final String clazz;
    final IEXMessage message;

    public AllPacktForIEX(String name, IEXMessage message) {
        this.clazz = name;
        this.message = message;
    }

    public AllPacktForIEX(IEXMessage message){
        this(message.getClass().getName(), message);
    }

    public AllPacktForIEX(Class<?> clazz, IEXMessage message) {
        this(clazz.getName(), message);
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        message.toBytes(buf);
        IEXMessage.writeString(buf, clazz);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(()->{
            if (!(message instanceof AllPackt)) {
                if (ctx.getDirection().getReceptionSide().isClient())
                    MessageLoader.getApiLoader().sendToPlayersNearby(new EXMessageCreater(message), ctx.getSender());
                else message.run(ctx);
            }
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
