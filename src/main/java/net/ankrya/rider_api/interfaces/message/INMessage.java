package net.ankrya.rider_api.interfaces.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 万用网络包，{@link IEXMessage}的迭代款，同样支持有参网络包 <br>
 * 使用{@link net.ankrya.rider_api.message.NMessageCreater}创建网络包 <br>
 * 能够自动识别数据类型（必须使用接口中的方法写） <br>
 * 优化了发包的读写次数 <br>
 * （当然导致它具有了上限，但是上限是变量数量不能超过9（把中间体改成long就可以支持到18），应该不会有这种情况吧？）
 */
public interface INMessage {

    /**
     * 使用{@link INMessage#autoWriteAll}方法自动解析 <br>
     * 全自动完了属于是（但是我就写了8个识别，多了爆炸，嘻嘻）
     */
    void toBytes(FriendlyByteBuf buf);

    void run(NetworkEvent.Context ctx);

    /**自动解析,一行解决*/
    static void autoWriteAll(FriendlyByteBuf buf, Comparable<?>... values){
        if (values.length == 0){
            buf.writeInt(0);
            return;
        }

        List<Integer> types = new ArrayList<>();
        List<Comparable<?>> valueList = new ArrayList<>();
        for (var value : values) autoAnalysis(value, types, valueList);

        int time = 0;
        int valueInt = 0;
        for (int type : types) valueInt = creatMassageNumber(valueInt, type, time++);
        buf.writeInt(valueInt);

        for (int type : types) writeFromType(buf, type, valueList.get(types.indexOf(type)));
    }

    /**自动解析的协助方法，请勿调用*/
    static <T> void writeFromType(FriendlyByteBuf buf, int type, T value){
        switch (type){
            case 1: buf.writeInt((int) value); break;
            case 2: buf.writeFloat((float) value); break;
            case 3: buf.writeDouble((double) value); break;
            case 4: buf.writeBoolean((boolean) value); break;
            case 5: buf.writeUtf((String) value); break;
            case 6: buf.writeResourceLocation((ResourceLocation) value); break;
            case 7: buf.writeUUID((UUID) value); break;
            case 8: buf.writeBlockPos((BlockPos) value); break;
        }
    }

    /**自动解析的协助方法，请勿调用*/
    static void autoAnalysis(Comparable<?> value, List<Integer> types, List<Comparable<?>>  values){
        Class<?> aClass = value.getClass();
        values.add(value);
        if (aClass.isAssignableFrom(Integer.class)) types.add(1);
        else if (aClass.isAssignableFrom(Float.class)) types.add(2);
        else if (aClass.isAssignableFrom(Double.class)) types.add(3);
        else if (aClass.isAssignableFrom(Boolean.class)) types.add(4);
        else if (aClass.isAssignableFrom(String.class)) types.add(5);
        else if (aClass.isAssignableFrom(ResourceLocation.class)) types.add(6);
        else if (aClass.isAssignableFrom(UUID.class)) types.add(7);
        else if (aClass.isAssignableFrom(BlockPos.class)) types.add(8);
        else throw new IllegalArgumentException("Unsupported type: " + aClass.getName());
    }

    static int creatMassageNumber(int value, int type,int time){
        return (int) (value + type * Math.pow(10, time));
    }
}
