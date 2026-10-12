package net.ankrya.rider_api.quest.client;


import net.ankrya.rider_api.quest.data.PlayerQuestData;
import net.ankrya.rider_api.quest.data.QuestProgress;
import net.ankrya.rider_api.quest.data.QuestState;
import net.ankrya.rider_api.quest.registry.QuestRegistry;

import java.util.List;

/** 客户端单例，保存从服务端同步过来的任务进度 */
public class ClientQuestCache {

    public static final ClientQuestCache INSTANCE = new ClientQuestCache();
    private PlayerQuestData data = new PlayerQuestData();

    private ClientQuestCache() {}

    public void update(PlayerQuestData d) { this.data = d; }

    public List<QuestProgress> activeQuests() { return data.active(); }

    public List<QuestProgress> availableQuests() {
        return data.all().values().stream()
                .filter(p -> p.getState() == QuestState.AVAILABLE)
                .toList();
    }

    public List<QuestProgress> repeatableCompleted() {
        return data.all().values().stream()
                .filter(p -> p.getState() == QuestState.COMPLETED)
                .filter(p -> QuestRegistry.INSTANCE.get(p.getQuestId())
                        .map(d -> d.repeatable()).orElse(false))
                .toList();
    }

    /** 当前进行中任务数量 */
    public int activeCount() { return data.active().size(); }
}