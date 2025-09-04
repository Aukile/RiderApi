package net.ankrya.rider_api.item.data;

import net.ankrya.rider_api.help.GJ;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * 存储ItemStack的自定义数据(应该是)<br>
 * 使用例：<pre>{@code public static final DataComponentType<ArmorData> BACKUP_ARMOR = DataComponentType.<ArmorData>builder().persistent(ArmorData.CODEC).networkSynchronized(ArmorData.STREAM_CODEC).build();}</pre>
 */
public record ArmorData(ResourceLocation itemId, int count, CompoundTag tag) {

    /**
     * CODEC 用于持久化存储（世界保存）
     */
    public static final Codec<ArmorData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("itemId").forGetter(ArmorData::itemId),
                    ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(ArmorData::count),
                    CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(ArmorData::tag)
            ).apply(instance, ArmorData::new)
    );

    /**
     * STREAM_CODEC 用于网络同步
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            ArmorData::itemId,

            ByteBufCodecs.INT,
            ArmorData::count,

            ByteBufCodecs.COMPOUND_TAG,
            ArmorData::tag,

            ArmorData::new
    );

    /**
     * 从 ItemStack 创建 ArmorData
     *
     * @param stack 要转换的物品堆栈
     * @return 对应的 ArmorData 对象
     */
    public static ArmorData fromItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot create ArmorData from empty ItemStack");
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        GJ.ToItem.getNbt(stack);
        CompoundTag nbt = GJ.ToItem.getNbt(stack);

        if (stack.isDamaged()) {
            nbt.putInt("Damage", stack.getDamageValue());
        }

        return new ArmorData(id, stack.getCount(), nbt);
    }

    /**
     * 尝试从 ItemStack 创建 ArmorData
     *
     * @param stack 要转换的物品堆栈
     * @return 对应的 ArmorData 对象，如果堆栈为空则返回 null
     */
    @Nullable
    public static ArmorData fromItemStackOrNull(ItemStack stack) {
        return stack.isEmpty() ? null : fromItemStack(stack);
    }

    /**
     * 将 ArmorData 转换回 ItemStack
     *
     * @return 对应的物品堆栈
     */
    public ItemStack toItemStack() {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item, count);

        if (!tag.isEmpty()) {
            GJ.ToItem.setNbt(stack, c -> c.merge(tag.copy()));

            if (tag.contains("Damage", Tag.TAG_INT)) {
                stack.setDamageValue(tag.getInt("Damage"));
                tag.remove("Damage"); // 避免重复设置
            }
        }

        return stack;
    }

    /**
     * 尝试将 ArmorData 转换回 ItemStack
     *
     * @return 对应的物品堆栈，如果数据无效则返回空堆栈
     */
    public ItemStack toItemStackSafe() {
        try {
            return toItemStack();
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * 获取物品显示名称
     */
    public Component getDisplayName() {
        return toItemStack().getHoverName();
    }

    /**
     * 检查此数据是否表示有效的物品
     */
    public boolean isValid() {
        return BuiltInRegistries.ITEM.containsKey(itemId);
    }

    /**
     * 基于内容的相等性检查
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArmorData armorData = (ArmorData) o;
        return count == armorData.count &&
                Objects.equals(itemId, armorData.itemId) &&
                Objects.equals(tag, armorData.tag);
    }

    /**
     * 基于内容的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(itemId, count, tag);
    }

    /**
     * 序列化为 CompoundTag
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", itemId.toString());
        tag.putInt("count", count);

        if (!this.tag.isEmpty()) {
            tag.put("nbt", this.tag.copy());
        }

        return tag;
    }

    /**
     * 从 CompoundTag 加载
     */
    public static Optional<ArmorData> load(CompoundTag tag) {
        if (!tag.contains("itemId", Tag.TAG_STRING) ||
                !tag.contains("count", Tag.TAG_INT)) {
            return Optional.empty();
        }

        try {
            ResourceLocation id = ResourceLocation.parse(tag.getString("itemId"));
            int count = tag.getInt("count");
            CompoundTag nbt = tag.contains("nbt", Tag.TAG_COMPOUND)
                    ? tag.getCompound("nbt")
                    : new CompoundTag();

            return Optional.of(new ArmorData(id, count, nbt));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public @NotNull String toString() {
        return "ArmorData[" + "itemId=" + itemId + ", count=" + count + ", tag=" + tag + ']';
    }
}
