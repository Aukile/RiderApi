package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.interfaces.message.IEXMessage;
import net.ankrya.rider_api.message.EXMessageCreater;
import net.ankrya.rider_api.message.MessageLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * {@link IEXMessage}的CTSTC网络包<br>
 * 应该不会用到<br>
 * 留例
 */
public class AllPacktForIEX implements IEXMessage {
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

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        message.toBytes(buf);
        IEXMessage.writeString(buf, clazz);
    }

    @Override
    public void run(IPayloadContext ctx) {
        ctx.enqueueWork(()->{
            if (!(message instanceof AllPackt)) {
                if (!ctx.flow().isServerbound())
                    MessageLoader.sendToPlayersNearby(new EXMessageCreater(message), (ServerPlayer) ctx.player());
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
