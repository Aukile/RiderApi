package net.ankrya.rider_api.compat.animation;

import com.google.gson.JsonObject;

/**
 * 根据动画 JSON 的结构自动判断其 item 骨骼坐标空间。
 * 两种格式在文件结构上完全不同,无需猜测数值。
 */
public final class ItemCoordSpaceDetector {

    private ItemCoordSpaceDetector() {}

    /**
     * @param root 动画文件的根 JSON 对象
     * @return 该动画的 item 坐标空间
     */
    public static ItemCoordSpace detect(JsonObject root) {
        // Blender / emotecraft 原生格式:含 emote / moves
        if (root.has("emote") || root.has("moves")) {
            return ItemCoordSpace.PLAYER_ANIM;
        }
        // 有些原生格式是 emote 数组包裹
        if (root.has("name") && root.has("author") && !root.has("format_version")) {
            return ItemCoordSpace.PLAYER_ANIM;
        }

        // Blockbench / bedrock 格式:含 format_version + animations
        if (root.has("format_version") && root.has("animations")) {
            return ItemCoordSpace.BEDROCK;
        }

        // 兜底:进一步探测 animations 里是否有 bones 结构(bedrock 特征)
        if (root.has("animations") && root.get("animations").isJsonObject()) {
            JsonObject anims = root.getAsJsonObject("animations");
            for (var entry : anims.entrySet()) {
                if (entry.getValue().isJsonObject()
                        && entry.getValue().getAsJsonObject().has("bones")) {
                    return ItemCoordSpace.BEDROCK;
                }
            }
        }

        // 实在识别不出,默认原生透传(最安全,不做破坏性变换)
        return ItemCoordSpace.PLAYER_ANIM;
    }

    public enum ItemCoordSpace {
        /** Blender / emotecraft 原生格式 —— item 值已在 playeranim 坐标系,透传 */
        PLAYER_ANIM,
        /** Blockbench / bedrock 格式 —— item 值是骨骼本地旋转,需轴交换 */
        BEDROCK
    }
}