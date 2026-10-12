package net.ankrya.rider_api.message;

import net.ankrya.rider_api.interfaces.message.IFMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * {@link  IFMessage} 的创建器
 * @author 八云紫Ender <br>
 */
public final class MessageCreater {
    final IFMessage message;

    public MessageCreater(IFMessage message) {
        this.message = message;
    }

    public static void toBuf(MessageCreater create, FriendlyByteBuf buf) {
        String path = create.message.getClass().getName();
        byte[] bytes = path.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        for (byte b : bytes) {
            buf.writeByte(b);
        }
        create.message.toBytes(buf);
//		Main.LOGGER.info("create Buf by " + create.message);
    }

    public static MessageCreater fromBuf(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readInt()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buf.readByte();
        }
        String path = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        IFMessage message;
        try {
            // 安全检查：只允许实例化 IFMessage 的实现类
            Class<?> clazz = Class.forName(path, false, MessageCreater.class.getClassLoader());
            if (!IFMessage.class.isAssignableFrom(clazz))
                throw new IllegalArgumentException("Not an IFMessage: " + path);
            message = (IFMessage) clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        message.fromBytes(buf);
//		Main.LOGGER.info("read Buf in " + message);
        return new MessageCreater(message);
    }

    // 执行message对象的方法
    public static void run(final MessageCreater create, final java.util.function.Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
//			Main.LOGGER.info("run task " + create.message);
            create.message.run(ctx);
        });
        ctx.setPacketHandled(true);
    }
}