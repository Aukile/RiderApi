package net.ankrya.rider_api.interfaces.geo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * IGeoItem 动画相关的物品数据（1.20 存在物品 NBT 里，高版本是数据组件 rider_api:geo_triggered_animation）
 */
public final class GeoItemAnimData {
    private GeoItemAnimData() {}

    /**
     * 最后一次通过 {@link IGeoItem#triggerAnimation} 触发的动画名（NBT 键）。<br>
     * 会随物品保存并同步到客户端，重进游戏/别的玩家后来才看到这个物品时，
     * 客户端据此恢复动画（停在最后一帧的动画会直接恢复到最后一帧）
     */
    public static final String TRIGGERED_ANIMATION = "rider_api_triggered_animation";

    /** 获取保存的触发动画名，没有返回 null */
    public static String get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TRIGGERED_ANIMATION) ? tag.getString(TRIGGERED_ANIMATION) : null;
    }

    public static void set(ItemStack stack, String animation) {
        if (stack.isEmpty()) return;
        stack.getOrCreateTag().putString(TRIGGERED_ANIMATION, animation);
    }

    public static void remove(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) tag.remove(TRIGGERED_ANIMATION);
    }
}
