package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @param npcId              绑定 NPC（可选）
 * @param requireNpcDialogue 是否必须与 NPC 对话才能接受
 * @param dialogues          NPC 对话台词列表
 * @param acceptMessageKey   接受任务时的提示翻译键，null 则用默认 "quest.api.accepted"
 * @param completeMessageKey 完成任务时的提示翻译键，null 则用默认 "quest.api.completed"
 */
public record QuestDefinition(ResourceLocation id, String title, String description,
                              @Nullable ResourceLocation guiStyleId, @Nullable ResourceLocation npcId,
                              boolean requireNpcDialogue, List<ResourceLocation> parentIds,
                              List<QuestObjective> objectives, List<WaveSpawnDef> waves,
                              List<String> dialogues,
                              @Nullable String acceptMessageKey, @Nullable String completeMessageKey,
                              QuestReward reward, boolean autoStart, boolean repeatable) {

    public static QuestDefinition fromJson(ResourceLocation id, JsonObject o) {
        String title = o.get("title").getAsString();
        String description = o.has("description") ? o.get("description").getAsString() : "";

        ResourceLocation guiStyleId = o.has("gui_style") ? new ResourceLocation(o.get("gui_style").getAsString())
                : null;

        ResourceLocation npcId = o.has("npc_id")
                ? new ResourceLocation(o.get("npc_id").getAsString()) : null;
        boolean requireNpc = o.has("require_npc_dialogue")
                && o.get("require_npc_dialogue").getAsBoolean();

        List<ResourceLocation> parents = new ArrayList<>();
        if (o.has("parents"))
            for (var e : o.getAsJsonArray("parents"))
                parents.add(new ResourceLocation(e.getAsString()));

        List<QuestObjective> objectives = new ArrayList<>();
        if (o.has("objectives"))
            for (var e : o.getAsJsonArray("objectives"))
                objectives.add(QuestObjective.fromJson(e.getAsJsonObject()));

        List<WaveSpawnDef> waves = new ArrayList<>();
        if (o.has("waves")) {
            var arr = o.getAsJsonArray("waves");
            for (int i = 0; i < arr.size(); i++)
                waves.add(WaveSpawnDef.fromJson(arr.get(i).getAsJsonObject(), i));
        } else if (o.has("elite_spawns")) { // 旧数据兼容
            var arr = o.getAsJsonArray("elite_spawns");
            for (int i = 0; i < arr.size(); i++)
                waves.add(WaveSpawnDef.fromLegacyElite(arr.get(i).getAsJsonObject(), i));
        }

        List<String> dialogues = new ArrayList<>();
        if (o.has("dialogues"))
            for (var e : o.getAsJsonArray("dialogues"))
                dialogues.add(e.getAsString());

        String acceptMessageKey = o.has("accept_message") ? o.get("accept_message").getAsString() : null;
        String completeMessageKey = o.has("complete_message")
                ? o.get("complete_message").getAsString() : null;

        QuestReward reward = o.has("reward")
                ? QuestReward.fromJson(o.getAsJsonObject("reward"))
                : QuestReward.EMPTY;

        boolean autoStart = o.has("auto_start") && o.get("auto_start").getAsBoolean();
        boolean repeatable = o.has("repeatable") && o.get("repeatable").getAsBoolean();

        return new QuestDefinition(id, title, description, guiStyleId, npcId,
                requireNpc, parents, objectives, waves, dialogues,
                acceptMessageKey, completeMessageKey, reward, autoStart, repeatable);
    }

    public Optional<WaveSpawnDef> wave(String waveId) {
        return waves.stream().filter(w -> w.id.equals(waveId)).findFirst();
    }
}