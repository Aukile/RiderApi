package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 一个波次 */
public class WaveSpawnDef {

    public enum Trigger {
        /** 接取任务时立刻（或延迟 delay）生成 */
        ON_ACCEPT,
        /** 指定目标完成时生成 */
        ON_OBJECTIVE,
        /** 上一个波次被清空后生成 */
        AFTER_PREVIOUS_CLEARED,
        /** 只能由代码/命令手动触发 */
        MANUAL;

        public static Trigger of(String s) {
            try { return valueOf(s.toUpperCase(Locale.ROOT)); }
            catch (Exception e) { return ON_ACCEPT; }
        }
    }

    /** 波次 id，任务内唯一 */
    public final String id;
    public final Trigger trigger;
    /** trigger = ON_OBJECTIVE 时监听的目标 id */
    @Nullable public final String triggerObjectiveId;
    /** 触发后延迟多少 tick 生成 */
    public final int delayTicks;
    /** 以玩家为中心的水平散布半径 */
    public final double spawnRadius;
    /** 是否显示整波血条 */
    public final boolean showBossBar;
    /** 血条 / 提示的翻译键，空 = 用任务标题 */
    public final String bossBarNameKey;
    /** 生成时给玩家发的提示翻译键，空 = 不发 */
    public final String announceKey;
    /** 本波是否需要被清空（用于 AFTER_PREVIOUS_CLEARED 链和 wave_clear 目标） */
    public final boolean mustClear;

    public final List<WaveMobDef> mobs;

    public WaveSpawnDef(String id, Trigger trigger, @Nullable String triggerObjectiveId,
                        int delayTicks, double spawnRadius, boolean showBossBar,
                        String bossBarNameKey, String announceKey, boolean mustClear,
                        List<WaveMobDef> mobs) {
        this.id = id;
        this.trigger = trigger;
        this.triggerObjectiveId = triggerObjectiveId;
        this.delayTicks = Math.max(0, delayTicks);
        this.spawnRadius = spawnRadius <= 0 ? 8 : spawnRadius;
        this.showBossBar = showBossBar;
        this.bossBarNameKey = bossBarNameKey;
        this.announceKey = announceKey;
        this.mustClear = mustClear;
        this.mobs = List.copyOf(mobs);
    }

    public static WaveSpawnDef fromJson(JsonObject o, int index) {
        List<WaveMobDef> mobs = new ArrayList<>();
        if (o.has("mobs"))
            for (var e : o.getAsJsonArray("mobs"))
                mobs.add(WaveMobDef.fromJson(e.getAsJsonObject()));

        return new WaveSpawnDef(
                o.has("id") ? o.get("id").getAsString() : "wave_" + index,
                o.has("trigger") ? Trigger.of(o.get("trigger").getAsString()) : Trigger.ON_ACCEPT,
                o.has("trigger_objective") ? o.get("trigger_objective").getAsString() : null,
                o.has("delay_ticks") ? o.get("delay_ticks").getAsInt() : 0,
                o.has("spawn_radius") ? o.get("spawn_radius").getAsDouble() : 8,
                o.has("show_boss_bar") && o.get("show_boss_bar").getAsBoolean(),
                o.has("boss_bar_name") ? o.get("boss_bar_name").getAsString() : "",
                o.has("announce") ? o.get("announce").getAsString() : "",
                !o.has("must_clear") || o.get("must_clear").getAsBoolean(),
                mobs);
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("trigger", trigger.name().toLowerCase(Locale.ROOT));
        if (triggerObjectiveId != null && !triggerObjectiveId.isEmpty())
            o.addProperty("trigger_objective", triggerObjectiveId);
        if (delayTicks > 0) o.addProperty("delay_ticks", delayTicks);
        o.addProperty("spawn_radius", spawnRadius);
        if (showBossBar) o.addProperty("show_boss_bar", true);
        if (!bossBarNameKey.isEmpty()) o.addProperty("boss_bar_name", bossBarNameKey);
        if (!announceKey.isEmpty()) o.addProperty("announce", announceKey);
        if (!mustClear) o.addProperty("must_clear", false);
        JsonArray arr = new JsonArray();
        mobs.forEach(m -> arr.add(m.toJson()));
        o.add("mobs", arr);
        return o;
    }

    /** 兼容旧的 elite_spawns：一条 elite 转成一个单怪波次 */
    public static WaveSpawnDef fromLegacyElite(JsonObject o, int index) {
        JsonObject mob = new JsonObject();
        mob.addProperty("entity", o.get("entity").getAsString());
        if (o.has("count")) mob.addProperty("count", o.get("count").getAsInt());
        copy(o, mob, "max_health");
        copy(o, mob, "armor");
        copy(o, mob, "attack_damage");
        copy(o, mob, "movement_speed");
        if (o.has("custom_name")) mob.addProperty("custom_name", o.get("custom_name").getAsString());
        if (o.has("nbt")) mob.addProperty("nbt", o.get("nbt").getAsString());

        JsonArray mobs = new JsonArray();
        mobs.add(mob);

        JsonObject wave = new JsonObject();
        wave.addProperty("id", "legacy_" + index);
        wave.addProperty("trigger",
                (o.has("spawn_on_start") && o.get("spawn_on_start").getAsBoolean())
                        ? "on_accept" : "manual");
        if (o.has("show_boss_bar")) wave.addProperty("show_boss_bar", o.get("show_boss_bar").getAsBoolean());
        wave.add("mobs", mobs);
        return fromJson(wave, index);
    }

    private static void copy(JsonObject from, JsonObject to, String key) {
        if (from.has(key) && from.get(key).getAsDouble() >= 0)
            to.addProperty(key, from.get(key).getAsDouble());
    }
}