package net.ankrya.rider_api.message;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.IEXMessage;
import net.ankrya.rider_api.message.ex_message.AllPackt;
import com.google.common.primitives.Primitives;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * {@link IEXMessage} 的创建器 <br>
 * 会自动解析出对应的网络包 <br>
 */
public class EXMessageCreater implements CustomPacketPayload{
    public static final Type<EXMessageCreater> TYPE = new Type<>(GJ.Easy.getApiResource("message_ex_creater"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EXMessageCreater> CODEC = StreamCodec.of(EXMessageCreater::toBuf, EXMessageCreater::fromBuf);
    final IEXMessage message;

    public EXMessageCreater(IEXMessage message) {
        this.message = message;
    }

    public static void toBuf(FriendlyByteBuf buf, EXMessageCreater create) {
        String path = create.message.getClass().getName();
        byte[] bytes = path.getBytes();
        buf.writeInt(bytes.length);
        for (byte b : bytes) {
            buf.writeByte(b);
        }
        create.message.toBytes(buf);
    }

    public static EXMessageCreater fromBuf(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readInt()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buf.readByte();
        }
        String path = new String(bytes);
        IEXMessage message;
        try {
            message = creatMessage(buf, path);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return new EXMessageCreater(message);
    }

    public static void run(final EXMessageCreater create, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> create.message.run(ctx));
    }

    @Override
    public @NotNull CustomPacketPayload.Type<EXMessageCreater> type() {
        return TYPE;
    }

    private static IEXMessage creatMessage(FriendlyByteBuf buf, String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        boolean hasParameters = buf.readBoolean();

        if (!Objects.equals(path, AllPackt.class.getName())) {
            if (hasParameters) {
                int length = buf.readInt();
                Class<?>[] parameterClass = new Class[length];
                Object[] parameters = new Object[length];
                for (int i = 0; i < length; i++) {
                    var object = readParameter(buf);
                    Class<?> aClass = object.getClass();
                    parameterClass[i] = Primitives.unwrap(aClass);
                    parameters[i] = object;
                }
                return (IEXMessage) Class.forName(path).getDeclaredConstructor(parameterClass).newInstance(parameters);
            } else return (IEXMessage) Class.forName(path).getDeclaredConstructor().newInstance();
        } else {
            if (hasParameters) {
                int length = buf.readInt();
                Class<?>[] parameterClass = new Class[length];
                Object[] parameters = new Object[length];
                String messageName = null;
                for (int i = 0; i < length + 1; i++) {
                    var object = readParameter(buf);
                    if (i != length){
                        Class<?> aClass = object.getClass();
                        parameterClass[i] = Primitives.unwrap(aClass);
                        parameters[i] = object;
                    } else messageName = (String) object;
                }
                IEXMessage message = (IEXMessage) Class.forName(messageName).getDeclaredConstructor(parameterClass).newInstance(parameters);
                return (IEXMessage) Class.forName(path).getDeclaredConstructor(String.class,IEXMessage.class).newInstance(messageName, message);
            } else {
                String messageName = (String) readParameter(buf);
                IEXMessage message = (IEXMessage) Class.forName(messageName).getDeclaredConstructor().newInstance();
                return (IEXMessage) Class.forName(path).getDeclaredConstructor(String.class,IEXMessage.class).newInstance(messageName, message);
            }
        }
    }

    private static Comparable<?> readParameter(FriendlyByteBuf buf){
        int type = buf.readInt();
        return switch (type) {
            case 0 -> buf.readInt();
            case 1 -> buf.readFloat();
            case 2 -> buf.readDouble();
            case 3 -> buf.readBoolean();
            case 4 -> buf.readUtf();
            case 5 -> buf.readResourceLocation();
            case 6 -> buf.readUUID();
            case 7 -> buf.readBlockPos();
            default -> throw new IllegalArgumentException("Unknown parameter type: " + type);
        };
    }
}
