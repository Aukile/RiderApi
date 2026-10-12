package net.ankrya.rider_api.quest.event;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.quest.data.WaveSpawnDef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务系统的全部事件。
 * 监听方式：
 *   @SubscribeEvent public static void on(QuestEvents.AcceptPre e) { ... }
 * 所有事件都带 player() 与 eventId()（形如 rider_api:quest_accept_pre）。
 */
public final class QuestEvents {

    private QuestEvents() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(RiderApi.MODID, path);
    }

    // ===================== 基类 =====================

    public static abstract class Base extends Event {
        private final ResourceLocation eventId;
        private final ServerPlayer player;
        @Nullable private final ResourceLocation questId;

        protected Base(ResourceLocation eventId, ServerPlayer player,
                       @Nullable ResourceLocation questId) {
            this.eventId = eventId;
            this.player = player;
            this.questId = questId;
        }

        /** 事件类型 id */
        public ResourceLocation eventId() { return eventId; }
        /** 触发事件的玩家 */
        public ServerPlayer player() { return player; }
        /** 相关任务 id，可能为 null（如纯打招呼对话） */
        @Nullable public ResourceLocation questId() { return questId; }
    }

    // ===================== 接取任务 =====================

    public static final ResourceLocation ACCEPT_PRE = id("quest_accept_pre");
    public static final ResourceLocation ACCEPT_POST = id("quest_accept_post");

    /** 接取前，可取消 */
    @Cancelable
    public static class AcceptPre extends Base {
        public AcceptPre(ServerPlayer p, ResourceLocation q) { super(ACCEPT_PRE, p, q); }
    }

    /** 接取成功后 */
    public static class AcceptPost extends Base {
        public AcceptPost(ServerPlayer p, ResourceLocation q) { super(ACCEPT_POST, p, q); }
    }

    // ===================== 任务变为可接取 =====================

    public static final ResourceLocation AVAILABLE = id("quest_available");

    public static class Available extends Base {
        public Available(ServerPlayer p, ResourceLocation q) { super(AVAILABLE, p, q); }
    }

    // ===================== 完成任务 =====================

    public static final ResourceLocation COMPLETE_PRE = id("quest_complete_pre");
    public static final ResourceLocation COMPLETE_POST = id("quest_complete_post");

    /** 所有目标已达成、即将结算，可取消（取消则保持 ACTIVE） */
    @Cancelable
    public static class CompletePre extends Base {
        public CompletePre(ServerPlayer p, ResourceLocation q) { super(COMPLETE_PRE, p, q); }
    }

    public static class CompletePost extends Base {
        public CompletePost(ServerPlayer p, ResourceLocation q) { super(COMPLETE_POST, p, q); }
    }

    // ===================== 奖励 =====================

    public static final ResourceLocation REWARD_PRE = id("quest_reward_pre");
    public static final ResourceLocation REWARD_POST = id("quest_reward_post");

    /** 发奖前：可改经验、增删物品，可取消 */
    @Cancelable
    public static class RewardPre extends Base {
        private int xp;
        private final List<ItemStack> items;

        public RewardPre(ServerPlayer p, ResourceLocation q, int xp, List<ItemStack> items) {
            super(REWARD_PRE, p, q);
            this.xp = xp;
            this.items = items;
        }

        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }
        /** 可直接修改的物品列表 */
        public List<ItemStack> getItems() { return items; }
    }

    /** 发奖后（实际发出的内容） */
    public static class RewardPost extends Base {
        private final int xp;
        private final List<ItemStack> items;

        public RewardPost(ServerPlayer p, ResourceLocation q, int xp, List<ItemStack> items) {
            super(REWARD_POST, p, q);
            this.xp = xp;
            this.items = List.copyOf(items);
        }
        public int getXp() { return xp; }
        public List<ItemStack> getItems() { return items; }
    }

    // ===================== 目标进度 =====================

    public static final ResourceLocation OBJECTIVE_PROGRESS = id("objective_progress");
    public static final ResourceLocation OBJECTIVE_COMPLETE = id("objective_complete");

    /** 计数即将变化，可取消，也可改写新值 */
    @Cancelable
    public static class ObjectiveProgress extends Base {
        private final String objectiveId;
        private final int oldValue;
        private int newValue;

        public ObjectiveProgress(ServerPlayer p, ResourceLocation q,
                                 String objectiveId, int oldValue, int newValue) {
            super(OBJECTIVE_PROGRESS, p, q);
            this.objectiveId = objectiveId;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String objectiveId() { return objectiveId; }
        public int getOldValue() { return oldValue; }
        public int getNewValue() { return newValue; }
        public void setNewValue(int v) { this.newValue = v; }
    }

    /** 某个目标刚刚达成 */
    public static class ObjectiveComplete extends Base {
        private final String objectiveId;
        public ObjectiveComplete(ServerPlayer p, ResourceLocation q, String objectiveId) {
            super(OBJECTIVE_COMPLETE, p, q);
            this.objectiveId = objectiveId;
        }
        public String objectiveId() { return objectiveId; }
    }

    // ===================== 对话 =====================

    public static final ResourceLocation DIALOGUE_OPEN = id("dialogue_open");
    public static final ResourceLocation DIALOGUE_COMPLETE = id("dialogue_complete");

    /** 服务端准备给玩家打开对话框，可取消 */
    @Cancelable
    public static class DialogueOpen extends Base {
        private final ResourceLocation npcId;
        private final boolean greeting;
        public DialogueOpen(ServerPlayer p, @Nullable ResourceLocation q,
                            ResourceLocation npcId, boolean greeting) {
            super(DIALOGUE_OPEN, p, q);
            this.npcId = npcId;
            this.greeting = greeting;
        }
        public ResourceLocation npcId() { return npcId; }
        public boolean isGreeting() { return greeting; }
    }

    public static class DialogueComplete extends Base {
        private final ResourceLocation npcId;
        public DialogueComplete(ServerPlayer p, ResourceLocation q, ResourceLocation npcId) {
            super(DIALOGUE_COMPLETE, p, q);
            this.npcId = npcId;
        }
        public ResourceLocation npcId() { return npcId; }
    }

    // ===================== 波次 =====================

    public static final ResourceLocation WAVE_PRE = id("wave_spawn_pre");
    public static final ResourceLocation WAVE_POST = id("wave_spawn_post");
    public static final ResourceLocation WAVE_MOB_SPAWNED = id("wave_mob_spawned");
    public static final ResourceLocation WAVE_CLEARED = id("wave_cleared");

    public static abstract class WaveBase extends Base {
        private final WaveSpawnDef wave;
        protected WaveBase(ResourceLocation eid, ServerPlayer p, ResourceLocation q, WaveSpawnDef w) {
            super(eid, p, q);
            this.wave = w;
        }
        public WaveSpawnDef wave() { return wave; }
        public String waveId() { return wave.id; }
    }

    /** 波次生成前：可取消，可改生成中心点 */
    @Cancelable
    public static class WaveSpawnPre extends WaveBase {
        private BlockPos center;
        public WaveSpawnPre(ServerPlayer p, ResourceLocation q, WaveSpawnDef w, BlockPos center) {
            super(WAVE_PRE, p, q, w);
            this.center = center;
        }
        public BlockPos getCenter() { return center; }
        public void setCenter(BlockPos pos) { this.center = pos; }
    }

    /** 每只怪生成后（已加入世界、已上属性和 buff） */
    public static class WaveMobSpawned extends WaveBase {
        private final Mob mob;
        public WaveMobSpawned(ServerPlayer p, ResourceLocation q, WaveSpawnDef w, Mob mob) {
            super(WAVE_MOB_SPAWNED, p, q, w);
            this.mob = mob;
        }
        public Mob mob() { return mob; }
    }

    /** 整波生成完毕 */
    public static class WaveSpawnPost extends WaveBase {
        private final List<Mob> mobs;
        public WaveSpawnPost(ServerPlayer p, ResourceLocation q, WaveSpawnDef w, List<Mob> mobs) {
            super(WAVE_POST, p, q, w);
            this.mobs = new ArrayList<>(mobs);
        }
        public List<Mob> mobs() { return mobs; }
    }

    /** 一波全部被清空 */
    public static class WaveCleared extends WaveBase {
        public WaveCleared(ServerPlayer p, ResourceLocation q, WaveSpawnDef w) {
            super(WAVE_CLEARED, p, q, w);
        }
    }
}