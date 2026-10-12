package net.ankrya.rider_api.quest.editor;

import net.ankrya.rider_api.quest.registry.QuestRegistry;

import java.util.List;
import java.util.stream.Collectors;

public final class QuestSuggestionSource {
    private QuestSuggestionSource() {}

    /** 所有已加载任务的 ID，供父任务补全 */
    public static List<String> allQuestIds() {
        return QuestRegistry.INSTANCE.all().stream()
                .map(def -> def.id().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    /** 排除某个 ID（避免任务把自己设成父任务） */
    public static List<String> allQuestIdsExcept(String selfId) {
        return QuestRegistry.INSTANCE.all().stream()
                .map(def -> def.id().toString())
                .filter(id -> !id.equals(selfId))
                .sorted()
                .collect(Collectors.toList());
    }
}