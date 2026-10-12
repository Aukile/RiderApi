package net.ankrya.rider_api.quest.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {

    private final ResourceLocation questId;
    private final Map<String, Integer> counters = new HashMap<>();
    private int dialogueStep;
    private QuestState state;

    public QuestProgress(ResourceLocation questId) {
        this.questId      = questId;
        this.dialogueStep = 0;
        this.state        = QuestState.ACTIVE;
    }

    // -------- 访问器 --------
    public ResourceLocation getQuestId() { return questId; }
    public QuestState getState(){ return state; }
    public void setState(QuestState s)   { this.state = s; }
    public int getDialogueStep()         { return dialogueStep; }
    public void advanceDialogue()        { dialogueStep++; }
    public void resetDialogue()          { dialogueStep = 0; }

    /** 重置计数器与对话步数（可重复任务重新接取时调用） */
    public void reset() {
        counters.clear();
        dialogueStep = 0;
    }

    public int getCounter(String objectiveId) {
        return counters.getOrDefault(objectiveId, 0);
    }
    public void incrementCounter(String objectiveId, int amount) {
        counters.merge(objectiveId, amount, Integer::sum);
    }
    public void setCounter(String objectiveId, int value) {
        counters.put(objectiveId, value);
    }

    // -------- 网络序列化 --------
    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeResourceLocation(questId);
        buf.writeUtf(state.name());
        buf.writeInt(dialogueStep);
        buf.writeInt(counters.size());
        counters.forEach((k, v) -> { buf.writeUtf(k); buf.writeInt(v); });
    }

    public static QuestProgress fromBuf(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        QuestProgress p = new QuestProgress(id);
        p.state        = QuestState.valueOf(buf.readUtf());
        p.dialogueStep = buf.readInt();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) p.counters.put(buf.readUtf(), buf.readInt());
        return p;
    }

    // -------- NBT 序列化（用于 Attachment） --------
    public net.minecraft.nbt.CompoundTag toNbt() {
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("quest", questId.toString());
        tag.putString("state", state.name());
        tag.putInt("dialogue_step", dialogueStep);
        var cTag = new net.minecraft.nbt.CompoundTag();
        counters.forEach(cTag::putInt);
        tag.put("counters", cTag);
        return tag;
    }

    public static QuestProgress fromNbt(net.minecraft.nbt.CompoundTag tag) {
        QuestProgress p = new QuestProgress(new ResourceLocation(tag.getString("quest")));
        p.state        = QuestState.valueOf(tag.getString("state"));
        p.dialogueStep = tag.getInt("dialogue_step");
        var cTag = tag.getCompound("counters");
        for (String k : cTag.getAllKeys()) p.counters.put(k, cTag.getInt(k));
        return p;
    }
}