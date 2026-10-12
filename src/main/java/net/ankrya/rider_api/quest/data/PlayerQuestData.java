package net.ankrya.rider_api.quest.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class PlayerQuestData {

    private final Map<ResourceLocation, QuestProgress> progresses = new LinkedHashMap<>();

    // -------- 查询 --------
    public Optional<QuestProgress> get(ResourceLocation questId) {
        return Optional.ofNullable(progresses.get(questId));
    }
    public Map<ResourceLocation, QuestProgress> all() { return Collections.unmodifiableMap(progresses); }

    public List<QuestProgress> active() {
        return progresses.values().stream()
                .filter(p -> p.getState() == QuestState.ACTIVE)
                .toList();
    }

    // -------- 修改 --------
    public QuestProgress getOrCreate(ResourceLocation questId) {
        return progresses.computeIfAbsent(questId, QuestProgress::new);
    }
    public void put(QuestProgress p) { progresses.put(p.getQuestId(), p); }
    public void remove(ResourceLocation questId) { progresses.remove(questId); }

    // -------- NBT --------
    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        var list = new ListTag();
        progresses.values().forEach(p -> list.add(p.toNbt()));
        tag.put("quests", list);
        return tag;
    }

    public static PlayerQuestData fromNbt(CompoundTag tag) {
        var d = new PlayerQuestData();
        if (tag.contains("quests")) {
            for (Tag t : tag.getList("quests", Tag.TAG_COMPOUND)) {
                QuestProgress p = QuestProgress.fromNbt((CompoundTag) t);
                d.progresses.put(p.getQuestId(), p);
            }
        }
        return d;
    }

    // -------- 网络 --------
    public void writeBuf(FriendlyByteBuf buf) {
        var list = progresses.values().stream()
                .filter(p -> p.getState() != QuestState.LOCKED)
                .toList();
        buf.writeInt(list.size());
        list.forEach(p -> p.writeBuf(buf));
    }

    public static PlayerQuestData fromBuf(FriendlyByteBuf buf) {
        var d = new PlayerQuestData();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            QuestProgress p = QuestProgress.fromBuf(buf);
            d.progresses.put(p.getQuestId(), p);
        }
        return d;
    }

    public void clearAll() { progresses.clear(); }
}