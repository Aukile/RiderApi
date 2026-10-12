package net.ankrya.rider_api.quest.manager;

import net.ankrya.rider_api.message.common.quest.QuestNetwork;
import net.ankrya.rider_api.quest.data.*;
import net.ankrya.rider_api.quest.event.QuestEvents;
import net.ankrya.rider_api.quest.registry.QuestAttachments;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class QuestManager {

    private QuestManager() {}

    // -------- 接受任务 --------
    public static boolean acceptQuest(ServerPlayer player, ResourceLocation questId) {
        Optional<QuestDefinition> defOpt = QuestRegistry.INSTANCE.get(questId);
        if (defOpt.isEmpty()) return false;
        QuestDefinition def = defOpt.get();

        var data = QuestAttachments.get(player);

        for (ResourceLocation parentId : def.parentIds()) {
            Optional<QuestProgress> parent = data.get(parentId);
            if (parent.isEmpty() || parent.get().getState() != QuestState.COMPLETED) return false;
        }

        Optional<QuestProgress> existing = data.get(questId);
        if (existing.isPresent()) {
            QuestState st = existing.get().getState();
            if (st == QuestState.ACTIVE) return false;
            if (st == QuestState.COMPLETED && !def.repeatable()) return false;
        }

        // ===== 事件：接取前 =====
        if (MinecraftForge.EVENT_BUS.post(new QuestEvents.AcceptPre(player, questId)))
            return false;

        QuestProgress progress = data.getOrCreate(questId);
        progress.reset();
        progress.setState(QuestState.ACTIVE);
        progress.resetDialogue();

        // 波次：ON_ACCEPT
        WaveSpawnManager.onQuestAccepted(player, def);

        String acceptKey = def.acceptMessageKey() != null ? def.acceptMessageKey() : "quest.api.accepted";
        player.sendSystemMessage(Component.translatable(acceptKey, Component.translatable(def.title())));

        MinecraftForge.EVENT_BUS.post(new QuestEvents.AcceptPost(player, questId));
        QuestNetwork.syncToPlayer(player);
        return true;
    }

    // -------- 完成任务 --------
    public static void tryCompleteQuest(ServerPlayer player, ResourceLocation questId) {
        Optional<QuestDefinition> defOpt = QuestRegistry.INSTANCE.get(questId);
        if (defOpt.isEmpty()) return;
        QuestDefinition def = defOpt.get();

        var data = QuestAttachments.get(player);
        Optional<QuestProgress> progOpt = data.get(questId);
        if (progOpt.isEmpty() || progOpt.get().getState() != QuestState.ACTIVE) return;

        QuestProgress progress = progOpt.get();
        for (QuestObjective obj : def.objectives())
            if (!obj.isCompleted(progress)) return;

        // ===== 事件：完成前 =====
        if (MinecraftForge.EVENT_BUS.post(new QuestEvents.CompletePre(player, questId)))
            return;

        progress.setState(QuestState.COMPLETED);
        giveReward(player, questId, def.reward());

        // 清理未清空的波次怪
        WaveSpawnManager.cancelQuestWaves(player, questId, false);

        String completeKey = def.completeMessageKey() != null
                ? def.completeMessageKey() : "quest.api.completed";
        player.sendSystemMessage(
                Component.translatable(completeKey, Component.translatable(def.title())));

        MinecraftForge.EVENT_BUS.post(new QuestEvents.CompletePost(player, questId));

        markAvailableQuests(player, questId);
        QuestNetwork.syncToPlayer(player);
    }

    private static void markAvailableQuests(ServerPlayer player, ResourceLocation completedId) {
        var data = QuestAttachments.get(player);
        boolean changed = false;

        for (QuestDefinition candidate : QuestRegistry.INSTANCE.all()) {
            if (!candidate.parentIds().contains(completedId)) continue;
            var existing = data.get(candidate.id());
            if (existing.isPresent() && existing.get().getState() != QuestState.LOCKED) continue;

            boolean allParentsDone = candidate.parentIds().stream().allMatch(pid ->
                    data.get(pid).map(p -> p.getState() == QuestState.COMPLETED).orElse(false));

            if (allParentsDone) {
                data.getOrCreate(candidate.id()).setState(QuestState.AVAILABLE);
                MinecraftForge.EVENT_BUS.post(new QuestEvents.Available(player, candidate.id()));
                changed = true;
            }
        }
        if (changed) QuestNetwork.syncToPlayer(player);
    }

    // ==================================================================
    // 统一的计数改写入口（带事件 + 波次触发）
    // ==================================================================

    /** @return 是否真的改变了 */
    private static boolean applyCounter(ServerPlayer player, QuestDefinition def,
                                        QuestProgress prog, QuestObjective obj, int newValue) {
        int old = prog.getCounter(obj.getId());
        if (old == newValue) return false;

        var e = new QuestEvents.ObjectiveProgress(player, def.id(), obj.getId(), old, newValue);
        if (MinecraftForge.EVENT_BUS.post(e)) return false;
        int finalValue = e.getNewValue();
        if (finalValue == old) return false;

        boolean wasDone = obj.isCompleted(prog);
        prog.setCounter(obj.getId(), finalValue);

        if (!wasDone && obj.isCompleted(prog)) {
            MinecraftForge.EVENT_BUS.post(
                    new QuestEvents.ObjectiveComplete(player, def.id(), obj.getId()));
            // 波次：ON_OBJECTIVE
            WaveSpawnManager.onObjectiveCompleted(player, def, obj.getId());
        }
        return true;
    }

    // -------- 收集刷新 --------
    public static void refreshCollectObjectives(ServerPlayer player) {
        var data = QuestAttachments.get(player);
        boolean changed = false;

        for (QuestProgress prog : data.active()) {
            var defOpt = QuestRegistry.INSTANCE.get(prog.getQuestId());
            if (defOpt.isEmpty()) continue;
            var def = defOpt.get();
            for (QuestObjective obj : def.objectives())
                if (obj instanceof CollectObjective co)
                    changed |= applyCounter(player, def, prog, obj, co.countInInventory(player));
        }

        if (changed) {
            new ArrayList<>(data.active()).forEach(p -> tryCompleteQuest(player, p.getQuestId()));
            QuestNetwork.syncToPlayer(player);
        }
    }

    // -------- 击杀 --------
    public static void onKill(ServerPlayer player, ResourceLocation killedType) {
        var data = QuestAttachments.get(player);
        boolean changed = false;

        for (QuestProgress prog : data.active()) {
            var defOpt = QuestRegistry.INSTANCE.get(prog.getQuestId());
            if (defOpt.isEmpty()) continue;
            var def = defOpt.get();
            for (QuestObjective obj : def.objectives())
                if (obj instanceof KillObjective ko
                        && ko.getEntityType().equals(killedType)
                        && !ko.isCompleted(prog))
                    changed |= applyCounter(player, def, prog, obj, prog.getCounter(obj.getId()) + 1);
        }

        if (changed) {
            new ArrayList<>(data.active()).forEach(p -> tryCompleteQuest(player, p.getQuestId()));
            QuestNetwork.syncToPlayer(player);
        }
    }

    // -------- 收集（增量） --------
    public static void onCollect(ServerPlayer player, ResourceLocation itemId, int count) {
        var data = QuestAttachments.get(player);
        for (QuestProgress prog : data.active()) {
            var defOpt = QuestRegistry.INSTANCE.get(prog.getQuestId());
            if (defOpt.isEmpty()) continue;
            var def = defOpt.get();
            for (QuestObjective obj : def.objectives())
                if (obj instanceof CollectObjective co
                        && co.getItemId().equals(itemId)
                        && !co.isCompleted(prog))
                    applyCounter(player, def, prog, obj, prog.getCounter(obj.getId()) + count);
        }
        new ArrayList<>(data.active()).forEach(p -> tryCompleteQuest(player, p.getQuestId()));
        QuestNetwork.syncToPlayer(player);
    }

    // -------- 对话 --------
    public static void onDialogueComplete(ServerPlayer player, ResourceLocation questId,
                                          ResourceLocation npcId) {
        var data = QuestAttachments.get(player);
        data.get(questId).ifPresent(prog ->
                QuestRegistry.INSTANCE.get(questId).ifPresent(def -> {
                    for (QuestObjective obj : def.objectives())
                        if (obj instanceof DialogueObjective dobj
                                && dobj.getNpcId().equals(npcId)
                                && !dobj.isCompleted(prog))
                            applyCounter(player, def, prog, obj, 1);

                    MinecraftForge.EVENT_BUS.post(
                            new QuestEvents.DialogueComplete(player, questId, npcId));
                    tryCompleteQuest(player, questId);
                }));
        QuestNetwork.syncToPlayer(player);
    }

    // -------- 奖励 --------
    private static void giveReward(ServerPlayer player, ResourceLocation questId, QuestReward reward) {
        List<ItemStack> items = new ArrayList<>();
        reward.items.forEach(s -> items.add(s.copy()));

        var pre = new QuestEvents.RewardPre(player, questId, reward.xp, items);
        if (MinecraftForge.EVENT_BUS.post(pre)) return;

        if (pre.getXp() > 0) player.giveExperiencePoints(pre.getXp());
        for (ItemStack stack : pre.getItems()) {
            if (stack.isEmpty()) continue;
            if (!player.getInventory().add(stack.copy()))
                player.drop(stack.copy(), false);
        }

        MinecraftForge.EVENT_BUS.post(
                new QuestEvents.RewardPost(player, questId, pre.getXp(), pre.getItems()));
    }
}