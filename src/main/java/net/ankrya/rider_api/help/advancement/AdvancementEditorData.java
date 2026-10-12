package net.ankrya.rider_api.help.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 成就编辑器内存数据 */
public class AdvancementEditorData {

    public enum Frame { TASK, GOAL, CHALLENGE }

    // ===== display =====
    public boolean hasDisplay = true;
    public String iconItem = "minecraft:stone";
    public String titleKey = "";        // 翻译键或纯文本
    public String descKey = "";
    public Frame frame = Frame.TASK;
    @Nullable public String backgroundTexture = null; // 仅根成就用
    public boolean showToast = true;
    public boolean announceToChat = true;
    public boolean hidden = false;

    // ===== parent =====
    @Nullable public String parent = null;

    // ===== criteria =====
    // 每条：名称 -> (trigger, JSON条件字符串)
    public static class Criterion {
        public String name = "";
        public String trigger = "minecraft:impossible";
        public String conditionsJson = "{}"; // 原始条件 JSON
    }
    public final List<Criterion> criteria = new ArrayList<>();

    // ===== requirements =====
    // 每个 group 是一组 criterion 名（OR 关系），group 之间是 AND
    // 空则默认全部 AND
    public final List<List<String>> requirements = new ArrayList<>();

    // ===== rewards =====
    public int rewardExperience = 0;
    public final List<String> rewardRecipes = new ArrayList<>();
    public final List<String> rewardLoot = new ArrayList<>();
    @Nullable public String rewardFunction = null;

    // ===== JSON 导出 =====
    public JsonObject toJson() {
        JsonObject root = new JsonObject();

        if (parent != null && !parent.isEmpty())
            root.addProperty("parent", parent);

        if (hasDisplay) root.add("display", buildDisplay());

        root.add("criteria", buildCriteria());

        if (!requirements.isEmpty())
            root.add("requirements", buildRequirements());

        JsonObject rewards = buildRewards();
        if (!rewards.entrySet().isEmpty())
            root.add("rewards", rewards);

        return root;
    }

    private JsonObject buildDisplay() {
        JsonObject d = new JsonObject();

        JsonObject icon = new JsonObject();
        icon.addProperty("item", iconItem);   // 1.20 用 "item"（1.21 是 "id"）
        d.add("icon", icon);

        d.add("title", textComponent(titleKey));
        d.add("description", textComponent(descKey));

        d.addProperty("frame", frame.name().toLowerCase(Locale.ROOT));
        if (backgroundTexture != null && !backgroundTexture.isEmpty())
            d.addProperty("background", backgroundTexture);
        d.addProperty("show_toast", showToast);
        d.addProperty("announce_to_chat", announceToChat);
        d.addProperty("hidden", hidden);
        return d;
    }

    /** 翻译键用 {"translate":...}，纯文本用 {"text":...} */
    private static JsonObject textComponent(String s) {
        JsonObject t = new JsonObject();
        if (s.contains(".") && !s.contains(" ")) {
            t.addProperty("translate", s);
        } else {
            t.addProperty("text", s);
        }
        return t;
    }

    private JsonObject buildCriteria() {
        JsonObject c = new JsonObject();
        for (Criterion cr : criteria) {
            if (cr.name.isEmpty()) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("trigger", cr.trigger);
            try {
                var parsed = com.google.gson.JsonParser.parseString(cr.conditionsJson);
                if (parsed.isJsonObject() && !parsed.getAsJsonObject().entrySet().isEmpty())
                    entry.add("conditions", parsed.getAsJsonObject());
            } catch (Exception ignored) {}
            c.add(cr.name, entry);
        }
        return c;
    }

    private JsonArray buildRequirements() {
        JsonArray arr = new JsonArray();
        for (List<String> group : requirements) {
            JsonArray g = new JsonArray();
            group.forEach(g::add);
            arr.add(g);
        }
        return arr;
    }

    private JsonObject buildRewards() {
        JsonObject r = new JsonObject();
        if (rewardExperience != 0) r.addProperty("experience", rewardExperience);
        if (!rewardRecipes.isEmpty()) {
            JsonArray a = new JsonArray();
            rewardRecipes.forEach(a::add);
            r.add("recipes", a);
        }
        if (!rewardLoot.isEmpty()) {
            JsonArray a = new JsonArray();
            rewardLoot.forEach(a::add);
            r.add("loot", a);
        }
        if (rewardFunction != null && !rewardFunction.isEmpty())
            r.addProperty("function", rewardFunction);
        return r;
    }
}