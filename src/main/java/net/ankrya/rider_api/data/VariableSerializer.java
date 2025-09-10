package net.ankrya.rider_api.data;

import net.ankrya.rider_api.interfaces.inside_use.IVariable;
import com.google.common.primitives.Primitives;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 数据解析器 <br>
 * 想加类型在后面加就行
 */
public final class VariableSerializer {
    public static <T> IVariable<T> auto(@NotNull T value){
        Class<?> type = value.getClass();
        return VariableSerializer.auto(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> IVariable<T> auto(Class<?> tClass){
        Class<?> type = Primitives.unwrap(tClass);
        if (type == boolean.class)
            return (IVariable<T>) BOOLEAN;
        if (type == byte.class)
            return (IVariable<T>) BYTE;
        if (type == int.class)
            return (IVariable<T>) INTEGER;
        if (type == float.class)
            return (IVariable<T>) FLOAT;
        if (type == double.class)
            return (IVariable<T>) DOUBLE;
        if (type == CompoundTag.class)
            return (IVariable<T>) COMPOUND_TAG;
        if (type == BlockPos.class)
            return (IVariable<T>) BLOCK_POS;
        if (type == UUID.class)
            return (IVariable<T>) UUID;
        if (type == ItemStack.class)
            return (IVariable<T>) ITEM_STACK;
        if (type == ResourceLocation.class)
            return (IVariable<T>) RESOURCE_LOCATION;
        if (type == Vec3.class)
            return (IVariable<T>) VECTOR_3D;
        else throw new IllegalArgumentException("Can't find DataSource for " + type);
    }

    @SuppressWarnings("unchecked")
    public static <T> IVariable<T> auto(int typeId){
        return (IVariable<T>) switch (typeId) {
            case 0 -> BOOLEAN;
            case 1 -> BYTE;
            case 2 -> INTEGER;
            case 3 -> FLOAT;
            case 4 -> DOUBLE;
            case 5 -> COMPOUND_TAG;
            case 6 -> BLOCK_POS;
            case 7 -> UUID;
            case 8 -> ITEM_STACK;
            case 9 -> RESOURCE_LOCATION;
            case 10 -> VECTOR_3D;
            default -> throw new IllegalArgumentException("Can't find DataSource for " + typeId);
        };
    }
    
    public static final IVariable<Boolean> BOOLEAN = new IVariable<>() {
        @Override
        public int typeId() {
            return 0;
        }

        @Override
        public void write(FriendlyByteBuf buf, Boolean value) {
            buf.writeBoolean(value);
        }

        @Override
        public Boolean read(FriendlyByteBuf buf) {
            return buf.readBoolean();
        }

        @Override
        public Tag write(Boolean value) {
            return ByteTag.valueOf(value);
        }

        @Override
        public Boolean read(Tag tag) {
            return ((ByteTag) tag).getAsByte() != 0;
        }
    };

    public static final IVariable<Byte> BYTE = new IVariable<>() {
        @Override
        public int typeId() {
            return 1;
        }

        @Override
        public void write(FriendlyByteBuf buf, Byte value) {
            buf.writeByte(value);
        }

        @Override
        public Byte read(FriendlyByteBuf buf) {
            return buf.readByte();
        }

        @Override
        public Tag write(Byte value) {
            return ByteTag.valueOf(value);
        }

        @Override
        public Byte read(Tag tag) {
            return ((ByteTag) tag).getAsByte();
        }
    };

    public static final IVariable<Integer> INTEGER = new IVariable<>() {
        @Override
        public int typeId() {
            return 2;
        }

        @Override
        public void write(FriendlyByteBuf buf, Integer value) {
            buf.writeVarInt(value);
        }

        @Override
        public Integer read(FriendlyByteBuf buf) {
            return buf.readVarInt();
        }

        @Override
        public Tag write(Integer value) {
            return IntTag.valueOf(value);
        }

        @Override
        public Integer read(Tag tag) {
            return ((IntTag) tag).getAsInt();
        }
    };

    public static final IVariable<Float> FLOAT = new IVariable<>() {
        @Override
        public int typeId() {
            return 3;
        }

        @Override
        public void write(FriendlyByteBuf buf, Float value) {
            buf.writeFloat(value);
        }

        @Override
        public Float read(FriendlyByteBuf buf) {
            return buf.readFloat();
        }

        @Override
        public Tag write(Float value) {
            return FloatTag.valueOf(value);
        }

        @Override
        public Float read(Tag tag) {
            return ((FloatTag) tag).getAsFloat();
        }
    };

    public static final IVariable<Double> DOUBLE = new IVariable<>() {
        @Override
        public int typeId() {
            return 4;
        }

        @Override
        public void write(FriendlyByteBuf buf, Double value) {
            buf.writeDouble(value);
        }

        @Override
        public Double read(FriendlyByteBuf buf) {
            return buf.readDouble();
        }

        @Override
        public Tag write(Double value) {
            return DoubleTag.valueOf(value);
        }

        @Override
        public Double read(Tag tag) {
            return ((DoubleTag) tag).getAsDouble();
        }
    };

    public static final IVariable<CompoundTag> COMPOUND_TAG = new IVariable<>() {
        @Override
        public int typeId() {
            return 5;
        }

        @Override
        public void write(FriendlyByteBuf buf, CompoundTag value) {
            buf.writeNbt(value);
        }

        @Override
        public CompoundTag read(FriendlyByteBuf buf) {
            return buf.readNbt();
        }

        @Override
        public Tag write(CompoundTag value) {
            return value;
        }

        @Override
        public CompoundTag read(Tag tag) {
            return (CompoundTag) tag;
        }
    };

    public static final IVariable<BlockPos> BLOCK_POS = new IVariable<>() {
        @Override
        public int typeId() {
            return 6;
        }

        @Override
        public void write(FriendlyByteBuf buf, BlockPos value) {
            buf.writeBlockPos(value);
        }

        @Override
        public BlockPos read(FriendlyByteBuf buf) {
            return buf.readBlockPos();
        }

        @Override
        public Tag write(BlockPos value) {
            return LongTag.valueOf(value.asLong());
        }

        @Override
        public BlockPos read(Tag tag) {
            return BlockPos.of(((LongTag) tag).getAsLong());
        }
    };

    public static final IVariable<UUID> UUID = new IVariable<>() {
        @Override
        public int typeId() {
            return 7;
        }

        @Override
        public void write(FriendlyByteBuf buf, UUID value) {
            buf.writeUUID(value);
        }

        @Override
        public UUID read(FriendlyByteBuf buf) {
            return buf.readUUID();
        }

        @Override
        public Tag write(UUID value) {
            CompoundTag compound = new CompoundTag();
            compound.putUUID("dm_uuid", value);
            return compound;
        }

        @Override
        public UUID read(Tag tag) {
            CompoundTag compound = (CompoundTag) tag;
            return compound.getUUID("dm_uuid");
        }
    };

    public static final IVariable<ItemStack> ITEM_STACK = new IVariable<>() {
        @Override
        public int typeId() {
            return 8;
        }


        @Override
        public void write(FriendlyByteBuf buf, ItemStack value) {
            buf.writeItemStack(value, true);
        }

        @Override
        public ItemStack read(FriendlyByteBuf buf) {
            return buf.readItem();
        }

        @Override
        public Tag write(ItemStack value) {
            return ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, value)
                    .result()
                    .orElse(new CompoundTag());
        }

        @Override
        public ItemStack read(Tag tag) {
            return ItemStack.CODEC.parse(NbtOps.INSTANCE, tag)
                    .result()
                    .orElse(ItemStack.EMPTY);
        }
    };

    public static final IVariable<ResourceLocation> RESOURCE_LOCATION = new IVariable<>() {
        @Override
        public int typeId() {
            return 9;
        }

        @Override
        public void write(FriendlyByteBuf buf, ResourceLocation value) {
            buf.writeResourceLocation(value);
        }

        @Override
        public ResourceLocation read(FriendlyByteBuf buf) {
            return buf.readResourceLocation();
        }

        @Override
        public Tag write(ResourceLocation value) {
            return StringTag.valueOf(value.toString());
        }

        @Override
        public ResourceLocation read(Tag tag) {
            return ResourceLocation.tryParse(tag.getAsString());
        }
    };

    public static final IVariable<Vec3> VECTOR_3D = new IVariable<>() {
        @Override
        public int typeId() {
            return 10;
        }

        @Override
        public void write(FriendlyByteBuf buf, Vec3 value) {
            buf.writeVector3f(value.toVector3f());
        }

        @Override
        public Vec3 read(FriendlyByteBuf buf) {
            return new Vec3(buf.readVector3f());
        }

        @Override
        public Tag write(Vec3 value) {
            CompoundTag compound = new CompoundTag();
            compound.putDouble("x", value.x);
            compound.putDouble("y", value.y);
            compound.putDouble("z", value.z);
            return  compound;
        }

        @Override
        public Vec3 read(Tag nbt) {
            CompoundTag compound = (CompoundTag) nbt;
            return new Vec3(compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z"));
        }
    };
}
