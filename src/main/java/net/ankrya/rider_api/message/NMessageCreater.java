package net.ankrya.rider_api.message;

import com.google.common.primitives.Primitives;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.ankrya.rider_api.message.ex_message.AllPackt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link  INMessage} 网络包创建器 <br>
 * 好好的优化过了 <br>
 * 千万别在run里抛出异常了TAT <br>
 * 痛死我了
 */
public class NMessageCreater{
    // 缓存类名的
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    // 构造器缓存，因为有AllPackt的存在，太乱了，再说吧
//    private static final Map<ClassKey, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    final INMessage message;

    public NMessageCreater(INMessage message) {
        this.message = message;
    }

    public static void toBuf(NMessageCreater create, FriendlyByteBuf buf) {
        String path = create.message.getClass().getName();
        buf.writeUtf(path);
        create.message.toBytes(buf);
    }

    public static NMessageCreater fromBuf(FriendlyByteBuf buf) {
        String path = buf.readUtf();
        INMessage message;
        try {
            message = creatMessage(buf, path);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        NMessageCreater create = new NMessageCreater(message);
        return create;
    }

    // 执行message对象的方法
    public static void run(final NMessageCreater create, final java.util.function.Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> create.message.run(context));
        context.setPacketHandled(true);
    }

    private static INMessage creatMessage(FriendlyByteBuf buf, String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        long lengthNum = buf.readLong();

        byte[] bytes = getDigits(lengthNum);
        int length = bytes.length;
        Class<?>[] parameterClass = new Class[length];
        Object[] parameters = new Object[length];
        boolean hasData = true;

        if (length == 0) hasData = false;
        else for (int i = 0; i < length; i++) {
            var object = readParameter(buf, bytes[i]);
            Class<?> aClass = object.getClass();
            parameterClass[i] = Primitives.unwrap(aClass);
            parameters[i] = object;
        }
        if (!Objects.equals(path, AllPackt.class.getName())) {
            if (hasData) {
                return (INMessage) messageClass(path).getDeclaredConstructor(parameterClass).newInstance(parameters);
            } else return (INMessage) messageClass(path).getDeclaredConstructor().newInstance();
        } else {
            String messageName = buf.readUtf();
            INMessage message = hasData
                    ? (INMessage) messageClass(messageName).getDeclaredConstructor(parameterClass).newInstance(parameters)
                    : (INMessage) messageClass(messageName).getDeclaredConstructor().newInstance();
            // 发送者 id 和"是否发回给发送者"，见 AllPackt
            int sourceId = buf.readInt();
            boolean includeSender = buf.readBoolean();
            return new AllPackt(messageName, message, sourceId, includeSender);
        }
    }

    public static byte[] getDigits(long number) {
        if (number == 0) {
            return new byte[0];
        }

        long[] temp = new long[18];
        int count = 0;

        while (number > 0) {
            long digit = number % 10;
            temp[count] = digit;
            count++;
            number /= 10;
        }

        byte[] digits = new byte[count];
        for (int i = 0; i < count; i++) {
            digits[i] = (byte) temp[i];
        }
        return digits;
    }

    private static Comparable<?> readParameter(FriendlyByteBuf buf, byte type){
        return switch (type) {
            case 1 -> buf.readInt();
            case 2 -> buf.readFloat();
            case 3 -> buf.readDouble();
            case 4 -> buf.readBoolean();
            case 5 -> buf.readUtf();
            case 6 -> buf.readResourceLocation();
            case 7 -> buf.readUUID();
            case 8 -> buf.readBlockPos();
            case 9 -> buf.readLong();
            default -> throw new IllegalArgumentException("Unknown parameter type: " + type);
        };
    }

    private static Class<?> classForName(String name){
        return CLASS_CACHE.computeIfAbsent(name, className -> {
            try {
                // 先不初始化类，校验通过后再用
                Class<?> clazz = Class.forName(className, false, NMessageCreater.class.getClassLoader());
                if (!INMessage.class.isAssignableFrom(clazz))
                    throw new IllegalArgumentException("Not an INMessage: " + className);
                return clazz;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 安全检查：网络包里的类名来自对方，必须是 INMessage 才允许实例化。
     * 原来任何类名都会被 Class.forName + 反射构造，恶意客户端可以让服务端实例化任意类
     */
    private static Class<?> messageClass(String name){
        return classForName(name);
    }

//    private record ClassKey(Class<?> clazz, Class<?>[] paramTypes) {}
//
//    private static Constructor<?> getCachedConstructor(Class<?> clazz, Class<?>[] paramTypes){
//
//        ClassKey key = new ClassKey(clazz, paramTypes);
//        return CONSTRUCTOR_CACHE.computeIfAbsent(key, k -> {
//            try {
//                return clazz.getDeclaredConstructor(paramTypes);
//            } catch (NoSuchMethodException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
}
