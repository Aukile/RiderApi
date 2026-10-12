package net.ankrya.rider_api.client.particle.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec3;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 从 bedrock 动画文件读取指定骨骼的轨道。<p>
 * 用法：BoneTrack track = AnimationBoneLoader.load(location, "particle");
 */
public class AnimationBoneLoader {

    /**
     * @param location  动画文件资源路径
     * @param boneName  要读取的骨骼名（如 "particle"、"Camera"、"long1"）
     */
    public static BoneTrack load(ResourceLocation location, String boneName) {
        Minecraft mc = Minecraft.getInstance();
        Optional<Resource> res = mc.getResourceManager().getResource(location);
        if (res.isEmpty()) return null;

        try (Reader reader = new InputStreamReader(res.get().open())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject animations = root.getAsJsonObject("animations");

            // 找到第一个含 bones 的动画
            JsonObject anim = null;
            for (Map.Entry<String, JsonElement> e : animations.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                if (o.has("bones")) { anim = o; break; }
            }
            if (anim == null) return null;

            float animLength = anim.has("animation_length")
                    ? anim.get("animation_length").getAsFloat() : 0f;

            JsonObject bone = anim.getAsJsonObject("bones").getAsJsonObject(boneName);
            if (bone == null) return null;

            var pos = parseChannel(bone, "position", true);
            var rot = parseChannel(bone, "rotation", false);

            float maxTick = Math.max(animLength * 20f,
                    Math.max(lastTime(pos.times), lastTime(rot.times)));

            return new BoneTrack(pos.times, pos.values, pos.easing,
                    rot.times, rot.values, rot.easing, maxTick);

        } catch (Exception ex) {
            return null;
        }
    }

    private static float lastTime(float[] t) {
        return t.length == 0 ? 0 : t[t.length - 1];
    }

    /** 解析一个通道（position 或 rotation） */
    /** 解析一个通道（position 或 rotation），带 easing */
    private static Channel parseChannel(JsonObject bone, String key, boolean isPos) {
        if (!bone.has(key)) return new Channel(new float[0], new Vec3[0], new String[0]);

        JsonObject channel = bone.get(key).getAsJsonObject();

        // 用 TreeMap 保证按时间排序，值里同时存 Vec3 与 easing
        TreeMap<Float, Vec3> mapVal = new TreeMap<>();
        TreeMap<Float, String> mapEase = new TreeMap<>();

        for (Map.Entry<String, JsonElement> e : channel.entrySet()) {
            float tick = Float.parseFloat(e.getKey()) * 20f;
            JsonArray arr = extractVector(e.getValue());
            if (arr == null) continue;

            float x = arr.get(0).getAsFloat();
            float y = arr.get(1).getAsFloat();
            float z = arr.get(2).getAsFloat();

            if (isPos) {
                mapVal.put(tick, new Vec3(x / 16.0, y / 16.0, -z / 16.0));
            } else {
                mapVal.put(tick, new Vec3(x, y, -z));
            }

            // easing 只在关键帧是对象且含 "easing" 时存在
            String ease = "linear";
            if (e.getValue().isJsonObject()) {
                JsonObject o = e.getValue().getAsJsonObject();
                if (o.has("easing")) ease = o.get("easing").getAsString();
            }
            mapEase.put(tick, ease);
        }

        int n = mapVal.size();
        float[] times = new float[n];
        Vec3[] values = new Vec3[n];
        String[] eases = new String[n];
        int i = 0;
        for (var en : mapVal.entrySet()) {
            times[i] = en.getKey();
            values[i] = en.getValue();
            eases[i] = mapEase.get(en.getKey());
            i++;
        }
        return new Channel(times, values, eases);
    }

    /** 关键帧值可能是 [x,y,z] 或 {"vector":[x,y,z]} */
    private static JsonArray extractVector(JsonElement value) {
        if (value.isJsonArray()) {
            return value.getAsJsonArray();
        }
        if (value.isJsonObject()) {
            JsonObject o = value.getAsJsonObject();
            if (o.has("vector")) return o.getAsJsonArray("vector");
            // 有些格式是 {"post":[...]} 或再嵌套，取第一个数组
            for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                if (e.getValue().isJsonArray()) return e.getValue().getAsJsonArray();
            }
        }
        return null;
    }

    private record Channel(float[] times, Vec3[] values, String[] easing) {}
}
