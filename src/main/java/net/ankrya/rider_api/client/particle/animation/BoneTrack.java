package net.ankrya.rider_api.client.particle.animation;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BoneTrack {

    private final float[] posTimes;
    private final Vec3[]  posValues;
    private final String[] posEasing;   // 每个关键帧的缓动类型（作用于「到达该帧」的区间）
    private final float[] rotTimes;
    private final Vec3[]  rotValues;
    private final String[] rotEasing;
    public final float maxTick;

    public BoneTrack(float[] posTimes, Vec3[] posValues, String[] posEasing,
                     float[] rotTimes, Vec3[] rotValues, String[] rotEasing,
                     float maxTick) {
        this.posTimes = posTimes;
        this.posValues = posValues;
        this.posEasing = posEasing;
        this.rotTimes = rotTimes;
        this.rotValues = rotValues;
        this.rotEasing = rotEasing;
        this.maxTick = maxTick;
    }

    public Vec3 samplePosition(float tick) {
        return sample(posTimes, posValues, posEasing, tick);
    }

    public Vec3 sampleRotation(float tick) {
        return sample(rotTimes, rotValues, rotEasing, tick);
    }

    private static Vec3 sample(float[] times, Vec3[] values, String[] easing, float tick) {
        if (times.length == 0) return Vec3.ZERO;
        if (tick <= times[0]) return values[0];
        if (tick >= times[times.length - 1]) return values[values.length - 1];

        for (int i = 0; i < times.length - 1; i++) {
            if (tick >= times[i] && tick <= times[i + 1]) {
                float span = times[i + 1] - times[i];
                float t = span <= 0 ? 0 : (tick - times[i]) / span;
                // Blockbench 的 easing 挂在「目标关键帧」上，即区间的终点帧
                t = applyEasing(easing[i + 1], t);
                Vec3 a = values[i], b = values[i + 1];
                return new Vec3(
                        Mth.lerp(t, a.x, b.x),
                        Mth.lerp(t, a.y, b.y),
                        Mth.lerp(t, a.z, b.z)
                );
            }
        }
        return values[values.length - 1];
    }

    /** 支持 Blockbench 常见 easing，未知类型退回 linear */
    private static float applyEasing(String type, float t) {
        if (type == null) return t;
        return switch (type) {
            case "easeInSine"    -> 1f - (float) Math.cos((t * Math.PI) / 2.0);
            case "easeOutSine"   -> (float) Math.sin((t * Math.PI) / 2.0);
            case "easeInOutSine" -> (float) (-(Math.cos(Math.PI * t) - 1.0) / 2.0);
            case "easeInQuad"    -> t * t;
            case "easeOutQuad"   -> t * (2f - t);
            case "easeInOutQuad" -> t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
            case "easeInCubic"   -> t * t * t;
            case "easeOutCubic"  -> 1f - (float) Math.pow(1f - t, 3);
            case "easeInOutCubic"-> t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
            default              -> t; // linear 及未知
        };
    }
}