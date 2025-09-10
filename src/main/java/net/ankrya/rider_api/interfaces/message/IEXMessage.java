package net.ankrya.rider_api.interfaces.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * {@link IFMessage}的升级款，使用{@link net.ankrya.rider_api.message.EXMessageCreater}创建 <br>
 * 无需注册的自动网络包，缺点是读写次数过多，我没把全部的数据类型写进去，感觉够用了
 */
public interface IEXMessage {
    default void toBytes(@NotNull FriendlyByteBuf buf){
        buf.writeBoolean(hasData());
        if (hasData())
            buf.writeInt(dataLong());
    }

    void run(NetworkEvent.Context ctx);

    boolean hasData();
    int dataLong();

    static void writeInt(@NotNull FriendlyByteBuf buf, int i){
        buf.writeInt(0);
        buf.writeInt(i);
    }

    static void writeFloat(@NotNull FriendlyByteBuf buf, float f){
        buf.writeInt(1);
        buf.writeFloat(f);
    }

    static void writeDouble(@NotNull FriendlyByteBuf buf, double d){
        buf.writeInt(2);
        buf.writeDouble(d);
    }

    static void writeBoolean(@NotNull FriendlyByteBuf buf, boolean aBoolean){
        buf.writeInt(3);
        buf.writeBoolean(aBoolean);
    }

    static void writeString(@NotNull FriendlyByteBuf buf, String s){
        buf.writeInt(4);
        buf.writeUtf(s);
    }

    static void writeResourceLocation(@NotNull FriendlyByteBuf buf, ResourceLocation resourceLocation){
        buf.writeInt(5);
        buf.writeResourceLocation(resourceLocation);
    }

    static void writeUUID(@NotNull FriendlyByteBuf buf, UUID uuid){
        buf.writeInt(6);
        buf.writeUUID(uuid);
    }

    static void writeBlockPos(@NotNull FriendlyByteBuf buf, BlockPos blockPos){
        buf.writeInt(7);
        buf.writeBlockPos(blockPos);
    }
}
