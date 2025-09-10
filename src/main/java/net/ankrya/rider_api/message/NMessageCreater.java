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
import java.util.function.Supplier;

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
        return new NMessageCreater(message);
    }

    // 执行message对象的方法
    public static void run(final NMessageCreater create, final Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> create.message.run(context));
        context.setPacketHandled(true);
    }

    private static INMessage creatMessage(FriendlyByteBuf buf, String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int lengthNum = buf.readInt();

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
                return (INMessage) classForName(path).getDeclaredConstructor(parameterClass).newInstance(parameters);
            } else return (INMessage) classForName(path).getDeclaredConstructor().newInstance();
        } else {
            if (hasData) {
                String messageName = buf.readUtf();
                INMessage message = (INMessage) Class.forName(messageName).getDeclaredConstructor(parameterClass).newInstance(parameters);
                return (INMessage) classForName(path).getDeclaredConstructor(String.class, INMessage.class).newInstance(messageName, message);
            } else {
                String messageName = buf.readUtf();
                INMessage message = (INMessage) Class.forName(messageName).getDeclaredConstructor().newInstance();
                return (INMessage) classForName(path).getDeclaredConstructor(String.class, INMessage.class).newInstance(messageName, message);
            }
        }
    }

    public static byte[] getDigits(int number) {
        if (number == 0) {
            return new byte[0];
        }

        int[] temp = new int[10];
        int count = 0;

        while (number > 0) {
            int digit = number % 10;
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
            default -> throw new IllegalArgumentException("Unknown parameter type: " + type);
        };
    }

    private static Class<?> classForName(String name){
        return CLASS_CACHE.computeIfAbsent(name, className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
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
