package net.ankrya.rider_api.quest.data;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 任务日志过滤配置，注册物品时绑定。
 *三种模式：
 *   ALL- 显示全部模组的任务，不标注来源
 *   ALL_WITH_LABELS - 显示全部模组的任务，每条标注来源模组名
 *   ONLY_MOD        - 只显示指定 modId 的任务
 */
public final class QuestFilter {

    public enum Mode { ALL, ALL_WITH_LABELS, ONLY_MOD }

    public final Mode mode;
    @Nullable public final String modId;

    private QuestFilter(Mode mode, @Nullable String modId) {
        this.mode = mode;
        this.modId = modId;
    }

    //===== 工厂方法 =====

    /** 显示全部模组任务，不标注来源 */
    public static QuestFilter all() {
        return new QuestFilter(Mode.ALL, null);
    }

    /** 显示全部模组任务，每条标注来源模组 */
    public static QuestFilter allWithLabels() {
        return new QuestFilter(Mode.ALL_WITH_LABELS, null);
    }

    /** 只显示指定 modId 的任务 */
    public static QuestFilter only(String modId) {
        return new QuestFilter(Mode.ONLY_MOD, modId);
    }

    // ===== 过滤逻辑 =====

    public List<QuestProgress> apply(List<QuestProgress> all) {
        if (mode == Mode.ONLY_MOD && modId != null) {
            return all.stream()
                    .filter(p -> p.getQuestId().getNamespace().equals(modId))
                    .toList();
        }
        return all;
    }

    public boolean showModLabel() {
        return mode == Mode.ALL_WITH_LABELS;
    }

    /**
     * 根据任务 ResourceLocation 的namespace 获取模组显示名。
     * 优先取 FML 的 displayName，取不到就用namespace本身。
     */
    public static Component modDisplayName(String namespace) {
        return Component.literal(
                ModList.get()
                        .getModContainerById(namespace)
                        .map(c -> c.getModInfo().getDisplayName())
                        .orElse(namespace)
        );
    }

    /**
     * 返回过滤器自身的描述（用于物品 tooltip）。
     */
    public Component describe() {
        return switch (mode) {
            case ALL -> Component.translatable("gui.api.journal.filter.all");
            case ALL_WITH_LABELS -> Component.translatable("gui.api.journal.filter.all_labeled");
            case ONLY_MOD -> Component.translatable("gui.api.journal.filter.only",
                    modDisplayName(modId));
        };
    }
}