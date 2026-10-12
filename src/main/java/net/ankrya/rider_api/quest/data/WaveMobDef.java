package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** 波次中的一种怪物配置 */
public class WaveMobDef {

    public final ResourceLocation entityType;
    public final int count;

    // 属性覆盖，<0 表示不修改
    public final double maxHealth;
    public final double armor;
    public final double attackDamage;
    public final double movementSpeed;
    public final double knockbackResistance;
    public final double followRange;

    public final String customNameKey;     // 空 = 不设
    public final boolean nameVisible;
    public final boolean persistent;       // true = 不会自然消失
    public final String nbt;

    public final List<MobEffectDef> effects;

    public WaveMobDef(ResourceLocation entityType, int count,
                      double maxHealth, double armor, double attackDamage,
                      double movementSpeed, double knockbackResistance, double followRange,
                      String customNameKey, boolean nameVisible, boolean persistent,
                      String nbt, List<MobEffectDef> effects) {
        this.entityType = entityType;
        this.count = Math.max(1, count);
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.knockbackResistance = knockbackResistance;
        this.followRange = followRange;
        this.customNameKey = customNameKey;
        this.nameVisible = nameVisible;
        this.persistent = persistent;
        this.nbt = nbt;
        this.effects = List.copyOf(effects);
    }

    public static WaveMobDef fromJson(JsonObject o) {
        List<MobEffectDef> fx = new ArrayList<>();
        if (o.has("effects"))
            for (var e : o.getAsJsonArray("effects"))
                fx.add(MobEffectDef.fromJson(e.getAsJsonObject()));

        return new WaveMobDef(
                new ResourceLocation(o.get("entity").getAsString()),
                o.has("count") ? o.get("count").getAsInt() : 1,
                d(o, "max_health"), d(o, "armor"), d(o, "attack_damage"),
                d(o, "movement_speed"), d(o, "knockback_resistance"), d(o, "follow_range"),
                o.has("custom_name") ? o.get("custom_name").getAsString() : "",
                !o.has("name_visible") || o.get("name_visible").getAsBoolean(),
                !o.has("persistent") || o.get("persistent").getAsBoolean(),
                o.has("nbt") ? o.get("nbt").getAsString() : "{}",
                fx);
    }

    private static double d(JsonObject o, String k) {
        return o.has(k) ? o.get(k).getAsDouble() : -1;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("entity", entityType.toString());
        o.addProperty("count", count);
        if (maxHealth >= 0) o.addProperty("max_health", maxHealth);
        if (armor >= 0) o.addProperty("armor", armor);
        if (attackDamage >= 0) o.addProperty("attack_damage", attackDamage);
        if (movementSpeed >= 0) o.addProperty("movement_speed", movementSpeed);
        if (knockbackResistance >= 0) o.addProperty("knockback_resistance", knockbackResistance);
        if (followRange >= 0) o.addProperty("follow_range", followRange);
        if (!customNameKey.isEmpty()) o.addProperty("custom_name", customNameKey);
        if (!nameVisible) o.addProperty("name_visible", false);
        if (!persistent) o.addProperty("persistent", false);
        if (!nbt.equals("{}")) o.addProperty("nbt", nbt);
        if (!effects.isEmpty()) {
            JsonArray arr = new JsonArray();
            effects.forEach(f -> arr.add(f.toJson()));
            o.add("effects", arr);
        }
        return o;
    }
}