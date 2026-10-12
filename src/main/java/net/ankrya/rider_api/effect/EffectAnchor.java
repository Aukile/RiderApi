package net.ankrya.rider_api.effect;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * 特效锚点：定义特效的空间附着方式。
 */
public sealed interface EffectAnchor {

    void encode(FriendlyByteBuf buf);

    static EffectAnchor decode(FriendlyByteBuf buf) {
        byte type = buf.readByte();
        return switch (type) {
            case 0 -> new FollowPlayer(buf.readDouble(), buf.readDouble(), buf.readDouble());
            case 1 -> new WorldPosition(buf.readDouble(), buf.readDouble(), buf.readDouble());
            default -> throw new IllegalArgumentException("Unknown anchor type: " + type);
        };
    }

    // ────────────────────────────────────────────────────

    /**
     * 跟随玩家（默认）。
     * offset 为相对玩家的偏移，(0,0,0) = 玩家脚底中心。
     */
    record FollowPlayer(double offsetX, double offsetY, double offsetZ) implements EffectAnchor {
        public FollowPlayer() {
            this(0, 0, 0);
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeByte(0);
            buf.writeDouble(offsetX);
            buf.writeDouble(offsetY);
            buf.writeDouble(offsetZ);
        }

        public Vec3 getOffset() {
            return new Vec3(offsetX, offsetY, offsetZ);
        }
    }

    /**
     * 固定世界坐标，不跟随任何实体。
     */
    record WorldPosition(double x, double y, double z) implements EffectAnchor {
        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeByte(1);
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
        }

        public Vec3 getPosition() {
            return new Vec3(x, y, z);
        }
    }

    // 快捷构造
    static EffectAnchor followPlayer() {
        return new FollowPlayer();
    }

    static EffectAnchor followPlayer(double offsetX, double offsetY, double offsetZ) {
        return new FollowPlayer(offsetX, offsetY, offsetZ);
    }

    static EffectAnchor worldPos(double x, double y, double z) {
        return new WorldPosition(x, y, z);
    }

    static EffectAnchor worldPos(Vec3 pos) {
        return new WorldPosition(pos.x, pos.y, pos.z);
    }
}