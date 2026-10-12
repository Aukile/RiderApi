package net.ankrya.rider_api.quest.editor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 编辑器内存数据，最终序列化为 JSON
 */
public class QuestEditorData {

    public String title = "";
    public String description = "";
    @Nullable public String npcId = null;
    public boolean requireNpcDialogue = false;
    public boolean autoStart = false;
    public boolean repeatable = false;

    public final List<String> parents = new ArrayList<>();

    // 目标：类型 + 参数
    public static class ObjectiveData {
        public String type = "kill"; // kill/collect/dialogue
        public String id = "";
        public String label = "";
        public String target = ""; // entity/item/npc_id
        public int count = 1;
    }
    public final List<ObjectiveData> objectives = new ArrayList<>();

    // ===== 波次 =====
    public static class EffectData {
        public String effect = "minecraft:strength";
        public int duration = -1;   // -1 = 无限
        public int amplifier = 0;
        public boolean ambient = false;
        public boolean visible = true;
        public boolean showIcon = true;
    }

    public static class WaveMobData {
        public String entity = "minecraft:zombie";
        public int count = 1;
        public double maxHealth = -1;
        public double armor = -1;
        public double attackDamage = -1;
        public double movementSpeed = -1;
        public double knockbackResistance = -1;
        public double followRange = -1;
        public String customName = "";
        public boolean nameVisible = true;
        public boolean persistent = true;
        public String nbt = "{}";
        public final List<EffectData> effects = new ArrayList<>();
    }

    public static class WaveData {
        public String id = "wave_1";
        public String trigger = "on_accept"; // on_accept/on_objective/after_previous_cleared/manual
        public String triggerObjective = "";
        public int delayTicks = 0;
        public double spawnRadius = 8;
        public boolean showBossBar = false;
        public String bossBarName = "";
        public String announce = "";
        public boolean mustClear = true;
        public final List<WaveMobData> mobs = new ArrayList<>();
    }

    public final List<WaveData> waves = new ArrayList<>();

    // 奖励
    public static class RewardItem {
        public String item = "";
        public int count = 1;
    }
    public int rewardXp = 0;
    public final List<RewardItem> rewardItems = new ArrayList<>();

    // 对话与样式
    public final List<String> dialogues = new ArrayList<>();
    public final List<String> greetings = new ArrayList<>();
    @Nullable public String guiStyleId = null;
    @Nullable public String acceptMessageKey = null;
    @Nullable public String completeMessageKey = null;

    // ===== 转 JSON =====
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("title", title);
        o.addProperty("description", description);
        if (npcId != null) o.addProperty("npc_id", npcId);
        o.addProperty("require_npc_dialogue", requireNpcDialogue);

        if (!parents.isEmpty()) {
            JsonArray arr = new JsonArray();
            parents.forEach(arr::add);
            o.add("parents", arr);
        }

        // 目标
        JsonArray objArr = new JsonArray();
        for (var obj : objectives) {
            JsonObject objO = new JsonObject();
            objO.addProperty("type", obj.type);
            objO.addProperty("id", obj.id);
            objO.addProperty("label", obj.label);
            if (obj.type.equals("kill"))
                objO.addProperty("entity", obj.target);
            else if (obj.type.equals("collect"))
                objO.addProperty("item", obj.target);
            else if (obj.type.equals("dialogue"))
                objO.addProperty("npc_id", obj.target);
            objO.addProperty("count", obj.count);
            objArr.add(objO);
        }
        o.add("objectives", objArr);

        // 波次
        if (!waves.isEmpty()) {
            JsonArray wArr = new JsonArray();
            for (var w : waves) {
                JsonObject wo = new JsonObject();
                wo.addProperty("id", w.id);
                wo.addProperty("trigger", w.trigger);
                if (!w.triggerObjective.isEmpty()) wo.addProperty("trigger_objective", w.triggerObjective);
                if (w.delayTicks > 0) wo.addProperty("delay_ticks", w.delayTicks);
                wo.addProperty("spawn_radius", w.spawnRadius);
                if (w.showBossBar) wo.addProperty("show_boss_bar", true);
                if (!w.bossBarName.isEmpty()) wo.addProperty("boss_bar_name", w.bossBarName);
                if (!w.announce.isEmpty()) wo.addProperty("announce", w.announce);
                if (!w.mustClear) wo.addProperty("must_clear", false);

                JsonArray mArr = new JsonArray();
                for (var m : w.mobs) {
                    JsonObject mo = new JsonObject();
                    mo.addProperty("entity", m.entity);
                    mo.addProperty("count", m.count);
                    if (m.maxHealth >= 0) mo.addProperty("max_health", m.maxHealth);
                    if (m.armor >= 0) mo.addProperty("armor", m.armor);
                    if (m.attackDamage >= 0) mo.addProperty("attack_damage", m.attackDamage);
                    if (m.movementSpeed >= 0) mo.addProperty("movement_speed", m.movementSpeed);
                    if (m.knockbackResistance >= 0) mo.addProperty("knockback_resistance", m.knockbackResistance);
                    if (m.followRange >= 0) mo.addProperty("follow_range", m.followRange);
                    if (!m.customName.isEmpty()) mo.addProperty("custom_name", m.customName);
                    if (!m.nameVisible) mo.addProperty("name_visible", false);
                    if (!m.persistent) mo.addProperty("persistent", false);
                    if (!m.nbt.equals("{}")) mo.addProperty("nbt", m.nbt);
                    if (!m.effects.isEmpty()) {
                        JsonArray fArr = new JsonArray();
                        for (var f : m.effects) {
                            JsonObject fo = new JsonObject();
                            fo.addProperty("effect", f.effect);
                            fo.addProperty("duration", f.duration);
                            if (f.amplifier != 0) fo.addProperty("amplifier", f.amplifier);
                            if (f.ambient) fo.addProperty("ambient", true);
                            if (!f.visible) fo.addProperty("visible", false);
                            if (!f.showIcon) fo.addProperty("show_icon", false);
                            fArr.add(fo);
                        }
                        mo.add("effects", fArr);
                    }
                    mArr.add(mo);
                }
                wo.add("mobs", mArr);
                wArr.add(wo);
            }
            o.add("waves", wArr);
        }

        // 奖励
        JsonObject reward = new JsonObject();
        reward.addProperty("xp", rewardXp);
        if (!rewardItems.isEmpty()) {
            JsonArray items = new JsonArray();
            for (var it : rewardItems) {
                JsonObject itO = new JsonObject();
                itO.addProperty("item", it.item);
                itO.addProperty("count", it.count);
                items.add(itO);
            }
            reward.add("items", items);
        }
        o.add("reward", reward);

        // 对话
        if (!dialogues.isEmpty()) {
            JsonArray dArr = new JsonArray();
            dialogues.forEach(dArr::add);
            o.add("dialogues", dArr);
        }

        if (guiStyleId != null) o.addProperty("gui_style", guiStyleId);
        if (acceptMessageKey != null) o.addProperty("accept_message", acceptMessageKey);
        if (completeMessageKey != null) o.addProperty("complete_message", completeMessageKey);
        if (autoStart) o.addProperty("auto_start", true);
        if (repeatable) o.addProperty("repeatable", true);
        return o;
    }

    // ===== 从 JSON 导入 =====
    public static QuestEditorData fromJson(JsonObject o) {
        QuestEditorData d = new QuestEditorData();
        d.title = o.has("title") ? o.get("title").getAsString() : "";
        d.description = o.has("description") ? o.get("description").getAsString() : "";
        d.npcId = o.has("npc_id") ? o.get("npc_id").getAsString() : null;
        d.requireNpcDialogue = o.has("require_npc_dialogue") && o.get("require_npc_dialogue").getAsBoolean();

        if (o.has("parents"))
            for (var e : o.getAsJsonArray("parents")) d.parents.add(e.getAsString());

        if (o.has("objectives"))
            for (var e : o.getAsJsonArray("objectives")) {
                var oo = e.getAsJsonObject();
                var obj = new ObjectiveData();
                obj.type = oo.get("type").getAsString();
                obj.id = oo.get("id").getAsString();
                obj.label = oo.has("label") ? oo.get("label").getAsString() : "";
                if (oo.has("entity")) obj.target = oo.get("entity").getAsString();
                else if (oo.has("item")) obj.target = oo.get("item").getAsString();
                else if (oo.has("npc_id")) obj.target = oo.get("npc_id").getAsString();
                obj.count = oo.has("count") ? oo.get("count").getAsInt() : 1;
                d.objectives.add(obj);
            }

        if (o.has("waves"))
            for (var e : o.getAsJsonArray("waves")) {
                var wo = e.getAsJsonObject();
                var w = new WaveData();
                w.id = wo.has("id") ? wo.get("id").getAsString() : "wave_" + (d.waves.size() + 1);
                w.trigger = wo.has("trigger") ? wo.get("trigger").getAsString() : "on_accept";
                w.triggerObjective = wo.has("trigger_objective") ? wo.get("trigger_objective").getAsString() : "";
                w.delayTicks = wo.has("delay_ticks") ? wo.get("delay_ticks").getAsInt() : 0;
                w.spawnRadius = wo.has("spawn_radius") ? wo.get("spawn_radius").getAsDouble() : 8;
                w.showBossBar = wo.has("show_boss_bar") && wo.get("show_boss_bar").getAsBoolean();
                w.bossBarName = wo.has("boss_bar_name") ? wo.get("boss_bar_name").getAsString() : "";
                w.announce = wo.has("announce") ? wo.get("announce").getAsString() : "";
                w.mustClear = !wo.has("must_clear") || wo.get("must_clear").getAsBoolean();
                if (wo.has("mobs"))
                    for (var me : wo.getAsJsonArray("mobs")) {
                        var mo = me.getAsJsonObject();
                        var m = new WaveMobData();
                        m.entity = mo.get("entity").getAsString();
                        m.count = mo.has("count") ? mo.get("count").getAsInt() : 1;
                        m.maxHealth = dv(mo, "max_health");
                        m.armor = dv(mo, "armor");
                        m.attackDamage = dv(mo, "attack_damage");
                        m.movementSpeed = dv(mo, "movement_speed");
                        m.knockbackResistance = dv(mo, "knockback_resistance");
                        m.followRange = dv(mo, "follow_range");
                        m.customName = mo.has("custom_name") ? mo.get("custom_name").getAsString() : "";
                        m.nameVisible = !mo.has("name_visible") || mo.get("name_visible").getAsBoolean();
                        m.persistent = !mo.has("persistent") || mo.get("persistent").getAsBoolean();
                        m.nbt = mo.has("nbt") ? mo.get("nbt").getAsString() : "{}";
                        if (mo.has("effects"))
                            for (var fe : mo.getAsJsonArray("effects")) {
                                var fo = fe.getAsJsonObject();
                                var f = new EffectData();
                                f.effect = fo.get("effect").getAsString();
                                f.duration = fo.has("duration") ? fo.get("duration").getAsInt() : -1;
                                f.amplifier = fo.has("amplifier") ? fo.get("amplifier").getAsInt() : 0;
                                f.ambient = fo.has("ambient") && fo.get("ambient").getAsBoolean();
                                f.visible = !fo.has("visible") || fo.get("visible").getAsBoolean();
                                f.showIcon = !fo.has("show_icon") || fo.get("show_icon").getAsBoolean();
                                m.effects.add(f);
                            }
                        w.mobs.add(m);
                    }
                d.waves.add(w);
            }

        if (o.has("dialogues"))
            for (var e : o.getAsJsonArray("dialogues")) d.dialogues.add(e.getAsString());

        if (o.has("reward")) {
            var r = o.getAsJsonObject("reward");
            if (r.has("xp")) d.rewardXp = r.get("xp").getAsInt();
            if (r.has("items"))
                for (var e : r.getAsJsonArray("items")) {
                    var io = e.getAsJsonObject();
                    var ri = new RewardItem();
                    ri.item = io.get("item").getAsString();
                    ri.count = io.has("count") ? io.get("count").getAsInt() : 1;
                    d.rewardItems.add(ri);
                }
        }
        if (o.has("gui_style")) d.guiStyleId = o.get("gui_style").getAsString();
        if (o.has("accept_message")) d.acceptMessageKey = o.get("accept_message").getAsString();
        if (o.has("complete_message")) d.completeMessageKey = o.get("complete_message").getAsString();
        if (o.has("auto_start")) d.autoStart = o.get("auto_start").getAsBoolean();
        if (o.has("repeatable")) d.repeatable = o.get("repeatable").getAsBoolean();
        return d;
    }

    /** 把导入的数据拷进当前实例 */
    public void copyFrom(QuestEditorData other) {
        this.title = other.title;
        this.description = other.description;
        this.npcId = other.npcId;
        this.requireNpcDialogue = other.requireNpcDialogue;
        this.parents.clear(); this.parents.addAll(other.parents);
        this.objectives.clear(); this.objectives.addAll(other.objectives);
        this.waves.clear(); this.waves.addAll(other.waves);
        this.dialogues.clear(); this.dialogues.addAll(other.dialogues);
        this.rewardXp = other.rewardXp;
        this.rewardItems.clear(); this.rewardItems.addAll(other.rewardItems);
        this.guiStyleId = other.guiStyleId;
        this.acceptMessageKey = other.acceptMessageKey;
        this.completeMessageKey = other.completeMessageKey;
        this.autoStart = other.autoStart;
        this.repeatable = other.repeatable;
    }

    private static double dv(JsonObject o, String k) {
        return o.has(k) ? o.get(k).getAsDouble() : -1;
    }
}