package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import javax.annotation.Nullable;

/** 怪物出生时附加的一条 buff/debuff */
public record MobEffectDef(ResourceLocation effect, int duration, int amplifier,
                           boolean ambient, boolean visible, boolean showIcon) {

    /** duration = -1 表示无限 */
    public static final int INFINITE = -1;

    @Nullable
    public MobEffectInstance create() {
        var fx = BuiltInRegistries.MOB_EFFECT.get(effect);
        if (fx == null) return null;
        return new MobEffectInstance(fx, duration, Math.max(0, amplifier), ambient, visible, showIcon);
    }

    public static MobEffectDef fromJson(JsonObject o) {
        return new MobEffectDef(
                new ResourceLocation(o.get("effect").getAsString()),
                o.has("duration") ? o.get("duration").getAsInt() : INFINITE,
                o.has("amplifier") ? o.get("amplifier").getAsInt() : 0,
                o.has("ambient") && o.get("ambient").getAsBoolean(),
                !o.has("visible") || o.get("visible").getAsBoolean(),
                !o.has("show_icon") || o.get("show_icon").getAsBoolean()
        );
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("effect", effect.toString());
        o.addProperty("duration", duration);
        if (amplifier != 0) o.addProperty("amplifier", amplifier);
        if (ambient) o.addProperty("ambient", true);
        if (!visible) o.addProperty("visible", false);
        if (!showIcon) o.addProperty("show_icon", false);
        return o;
    }

    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeResourceLocation(effect);
        buf.writeVarInt(duration);
        buf.writeVarInt(amplifier);
        buf.writeBoolean(ambient);
        buf.writeBoolean(visible);
        buf.writeBoolean(showIcon);
    }

    public static MobEffectDef fromBuf(FriendlyByteBuf buf) {
        return new MobEffectDef(buf.readResourceLocation(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }
}