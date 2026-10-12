package net.ankrya.rider_api.effect;

import net.minecraft.network.FriendlyByteBuf;

public record PlayerEffectConfig(
        String effectId,
        String modid,
        String model,
        String texture,
        String animation,
        int durationTicks,
        boolean loop,
        boolean autoGlow,
        float scale,
        EffectAnchor anchor  // 新增
) {
    public static final int INFINITE = -1;

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(effectId);
        buf.writeUtf(modid);
        buf.writeUtf(model);
        buf.writeUtf(texture);
        buf.writeUtf(animation);
        buf.writeInt(durationTicks);
        buf.writeBoolean(loop);
        buf.writeBoolean(autoGlow);
        buf.writeFloat(scale);
        anchor.encode(buf);
    }

    public static PlayerEffectConfig decode(FriendlyByteBuf buf) {
        return new PlayerEffectConfig(
                buf.readUtf(), buf.readUtf(), buf.readUtf(),
                buf.readUtf(), buf.readUtf(),
                buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(),
                EffectAnchor.decode(buf)
        );
    }

    // ──────────────────── Builder ────────────────────

    public static Builder id(String effectId) {
        return new Builder(effectId);
    }

    public static final class Builder {
        private final String effectId;
        private String modid    = "rider_api";
        private String model    = "null";
        private String texture  = "null";
        private String animation = "idle";
        private int duration    = INFINITE;
        private boolean loop    = true;
        private boolean autoGlow = false;
        private float scale     = 1.0f;
        private EffectAnchor anchor = EffectAnchor.followPlayer(); // 默认跟随

        public Builder(String effectId) {
            this.effectId = effectId;
        }

        public Builder from(String modid)         { this.modid = modid; return this; }
        public Builder model(String model)         { this.model = model; return this; }
        public Builder texture(String texture)     { this.texture = texture; return this; }
        public Builder animation(String animation) { this.animation = animation; return this; }
        public Builder duration(int ticks)         { this.duration = ticks; return this; }
        public Builder loop(boolean loop)          { this.loop = loop; return this; }
        public Builder autoGlow()                  { this.autoGlow = true; return this; }
        public Builder scale(float scale)          { this.scale = scale; return this; }

        // 锚点设置
        public Builder followPlayer()              { this.anchor = EffectAnchor.followPlayer(); return this; }
        public Builder followPlayer(double x, double y, double z) {
            this.anchor = EffectAnchor.followPlayer(x, y, z);
            return this;
        }
        public Builder worldPos(double x, double y, double z) {
            this.anchor = EffectAnchor.worldPos(x, y, z);
            return this;
        }
        public Builder anchor(EffectAnchor anchor) { this.anchor = anchor; return this; }

        public PlayerEffectConfig build() {
            return new PlayerEffectConfig(effectId, modid, model, texture,
                    animation, duration, loop, autoGlow, scale, anchor);
        }
    }
}