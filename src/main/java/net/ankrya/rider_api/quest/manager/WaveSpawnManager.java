package net.ankrya.rider_api.quest.manager;

import net.ankrya.rider_api.quest.data.MobEffectDef;
import net.ankrya.rider_api.quest.data.QuestDefinition;
import net.ankrya.rider_api.quest.data.WaveMobDef;
import net.ankrya.rider_api.quest.data.WaveSpawnDef;
import net.ankrya.rider_api.quest.event.QuestEvents;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

/**
 * 任务波次生成与追踪。
 * 状态保存在内存中（服务器重启后正在进行的波次不再追踪，怪物本身仍留在世界）。
 */
public final class WaveSpawnManager {

    private WaveSpawnManager() {}

    // ===== NBT 标记键 =====
    public static final String TAG_QUEST = "quest_id";
    public static final String TAG_WAVE = "quest_wave_id";
    public static final String TAG_OWNER = "quest_owner";

    /** 待生成的波次 */
    private record Pending(UUID playerId, ResourceLocation questId, String waveId, BlockPos center,
                           ResourceKey<Level> dim, int[] ticksLeft) {}

    /** 已生成、还活着的波次 */
    private static final class Active {
        final UUID playerId;
        final ResourceLocation questId;
        final WaveSpawnDef wave;
        final ResourceKey<Level> dim;
        final Set<UUID> alive = new HashSet<>();
        float totalMaxHealth;
        ServerBossEvent bar;

        Active(UUID playerId, ResourceLocation questId, WaveSpawnDef wave, ResourceKey<Level> dim) {
            this.playerId = playerId;
            this.questId = questId;
            this.wave = wave;
            this.dim = dim;
        }
    }

    private static final List<Pending> PENDING = new ArrayList<>();
    private static final List<Active> ACTIVE = new ArrayList<>();
    private static int tickCounter = 0;

    // ==================================================================
    // 触发入口
    // ==================================================================

    /** 接取任务时调用 */
    public static void onQuestAccepted(ServerPlayer player, QuestDefinition def) {
        for (WaveSpawnDef w : def.waves()) {
            if (w.trigger == WaveSpawnDef.Trigger.ON_ACCEPT)
                schedule(player, def.id(), w, player.blockPosition());
        }
    }

    /** 某个目标完成时调用 */
    public static void onObjectiveCompleted(ServerPlayer player, QuestDefinition def, String objectiveId) {
        for (WaveSpawnDef w : def.waves()) {
            if (w.trigger == WaveSpawnDef.Trigger.ON_OBJECTIVE
                    && objectiveId.equals(w.triggerObjectiveId))
                schedule(player, def.id(), w, player.blockPosition());
        }
    }

    /** 手动触发（命令 / 其他模组 API） */
    public static boolean triggerWave(ServerPlayer player, ResourceLocation questId, String waveId) {
        var def = QuestRegistry.INSTANCE.get(questId).orElse(null);
        if (def == null) return false;
        var wave = def.wave(waveId).orElse(null);
        if (wave == null) return false;
        schedule(player, questId, wave, player.blockPosition());
        return true;
    }

    /** 任务完成/放弃时清理该玩家该任务的波次 */
    public static void cancelQuestWaves(ServerPlayer player, ResourceLocation questId,
                                        boolean discardMobs) {
        PENDING.removeIf(p -> p.playerId.equals(player.getUUID()) && p.questId.equals(questId));
        ACTIVE.removeIf(a -> {
            if (!a.playerId.equals(player.getUUID()) || !a.questId.equals(questId)) return false;
            if (a.bar != null) a.bar.removeAllPlayers();
            if (discardMobs) {
                ServerLevel lvl = player.server.getLevel(a.dim);
                if (lvl != null) for (UUID id : a.alive) {
                    var e = lvl.getEntity(id);
                    if (e != null) e.discard();
                }
            }
            return true;
        });
    }

    private static void schedule(ServerPlayer player, ResourceLocation questId,
                                 WaveSpawnDef wave, BlockPos center) {
        if (wave.delayTicks <= 0) {
            spawnNow(player, questId, wave, center);
        } else {
            PENDING.add(new Pending(player.getUUID(), questId, wave.id, center,
                    player.level().dimension(), new int[]{ wave.delayTicks }));
        }
    }

    // ==================================================================
    // 生成
    // ==================================================================

    public static void spawnNow(ServerPlayer player, ResourceLocation questId,
                                WaveSpawnDef wave, BlockPos center) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // ==== 生成前事件（可取消 / 可改中心点） ====
        var pre = new QuestEvents.WaveSpawnPre(player, questId, wave, center);
        if (MinecraftForge.EVENT_BUS.post(pre)) return;
        BlockPos origin = pre.getCenter();

        Active active = new Active(player.getUUID(), questId, wave, level.dimension());
        List<Mob> spawned = new ArrayList<>();

        for (WaveMobDef md : wave.mobs) {
            var typeOpt = BuiltInRegistries.ENTITY_TYPE.getOptional(md.entityType);
            if (typeOpt.isEmpty()) continue;

            for (int i = 0; i < md.count; i++) {
                Entity raw = typeOpt.get().create(level);
                if (!(raw instanceof Mob mob)) {
                    if (raw != null) raw.discard();
                    continue;
                }

                placeMob(level, mob, origin, wave.spawnRadius);
                applyAttributes(mob, md);
                applyNbt(mob, md);
                applyName(mob, md);

                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                        MobSpawnType.EVENT, null, null);

                // buff 必须在 finalizeSpawn 之后加，避免被覆盖
                applyEffects(mob, md);

                if (md.persistent) mob.setPersistenceRequired();

                var pd = mob.getPersistentData();
                pd.putString(TAG_QUEST, questId.toString());
                pd.putString(TAG_WAVE, wave.id);
                pd.putString(TAG_OWNER, player.getUUID().toString());

                level.addFreshEntity(mob);
                spawned.add(mob);
                active.alive.add(mob.getUUID());
                active.totalMaxHealth += mob.getMaxHealth();

                MinecraftForge.EVENT_BUS.post(new QuestEvents.WaveMobSpawned(player, questId, wave, mob));
            }
        }

        if (spawned.isEmpty()) return;

        // 血条
        if (wave.showBossBar) {
            Component name = wave.bossBarNameKey.isEmpty()
                    ? QuestRegistry.INSTANCE.get(questId)
                    .map(d -> (Component) Component.translatable(d.title()))
                    .orElse(Component.literal(wave.id))
                    : Component.translatable(wave.bossBarNameKey);
            active.bar = new ServerBossEvent(name, BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10);
            active.bar.addPlayer(player);
            active.bar.setProgress(1.0f);
        }

        ACTIVE.add(active);

        if (!wave.announceKey.isEmpty())
            player.sendSystemMessage(Component.translatable(wave.announceKey, spawned.size()));

        MinecraftForge.EVENT_BUS.post(new QuestEvents.WaveSpawnPost(player, questId, wave, spawned));
    }

    private static void placeMob(ServerLevel level, Mob mob, BlockPos origin, double radius) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double ang = level.random.nextDouble() * Math.PI * 2;
            double dist = 2 + level.random.nextDouble() * radius;
            double x = origin.getX() + 0.5 + Math.cos(ang) * dist;
            double z = origin.getZ() + 0.5 + Math.sin(ang) * dist;
            int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(x, origin.getY(), z)).getY();
            // 优先靠近原始 Y，避免刷到地表以外
            if (Math.abs(y - origin.getY()) > 12) y = origin.getY();
            mob.moveTo(x, y, z, level.random.nextFloat() * 360f, 0);
            if (level.noCollision(mob)) return;
        }
        mob.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0, 0);
    }

    private static void applyAttributes(Mob m, WaveMobDef md) {
        set(m, Attributes.MAX_HEALTH, md.maxHealth);
        set(m, Attributes.ARMOR, md.armor);
        set(m, Attributes.ATTACK_DAMAGE, md.attackDamage);
        set(m, Attributes.MOVEMENT_SPEED, md.movementSpeed);
        set(m, Attributes.KNOCKBACK_RESISTANCE, md.knockbackResistance);
        set(m, Attributes.FOLLOW_RANGE, md.followRange);
        if (md.maxHealth > 0) m.setHealth((float) md.maxHealth);
    }

    private static void set(Mob m, Attribute attr, double value) {
        if (value < 0) return;
        var inst = m.getAttribute(attr);
        if (inst != null) inst.setBaseValue(value);
    }

    private static void applyEffects(Mob m, WaveMobDef md) {
        for (MobEffectDef def : md.effects) {
            var inst = def.create();
            if (inst != null) m.addEffect(inst);
        }
    }

    private static void applyName(Mob m, WaveMobDef md) {
        if (md.customNameKey.isEmpty()) return;
        m.setCustomName(Component.translatable(md.customNameKey));
        m.setCustomNameVisible(md.nameVisible);
    }

    private static void applyNbt(Mob m, WaveMobDef md) {
        if (md.nbt == null || md.nbt.equals("{}") || md.nbt.isBlank()) return;
        try {
            CompoundTag extra = TagParser.parseTag(md.nbt);
            CompoundTag full = new CompoundTag();
            m.save(full);
            full.merge(extra);
            m.load(full);
        } catch (Exception ignored) {}
    }

    // ==================================================================
    // Tick：延迟生成 + 血条刷新
    // ==================================================================

    public static void tick(MinecraftServer server) {
        // 延迟波次
        if (!PENDING.isEmpty()) {
            // 先收集到期的波次，遍历结束后再生成。
            // 原来在遍历 PENDING 时直接 spawnNow，生成过程中触发的事件（比如其他监听器又接了任务、
            // 排了新的波次）会修改 PENDING，导致 ConcurrentModificationException
            List<Pending> due = new ArrayList<>();
            Iterator<Pending> it = PENDING.iterator();
            while (it.hasNext()) {
                Pending p = it.next();
                if (--p.ticksLeft[0] > 0) continue;
                it.remove();
                due.add(p);
            }
            for (Pending p : due) {
                ServerPlayer sp = server.getPlayerList().getPlayer(p.playerId);
                if (sp == null) continue;
                QuestRegistry.INSTANCE.get(p.questId)
                        .flatMap(d -> d.wave(p.waveId))
                        .ifPresent(w -> spawnNow(sp, p.questId, w,
                                sp.level().dimension() == p.dim ? p.center : sp.blockPosition()));
            }
        }

        // 血条每半秒刷新一次
        if (++tickCounter % 10 != 0 || ACTIVE.isEmpty()) return;
        for (Active a : ACTIVE) {
            if (a.bar == null) continue;
            ServerLevel lvl = server.getLevel(a.dim);
            if (lvl == null) continue;
            float cur = 0;
            for (UUID id : a.alive) {
                var e = lvl.getEntity(id);
                if (e instanceof LivingEntity le) cur += le.getHealth();
            }
            a.bar.setProgress(a.totalMaxHealth <= 0 ? 0 : Math.min(1f, cur / a.totalMaxHealth));
        }
    }

    // ==================================================================
    // 死亡追踪
    // ==================================================================

    /** 从 LivingDeathEvent 调用 */
    public static void onMobDeath(MinecraftServer server, LivingEntity dead) {
        UUID id = dead.getUUID();
        Iterator<Active> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Active a = it.next();
            if (!a.alive.remove(id)) continue;
            if (!a.alive.isEmpty()) return;

            // 本波清空
            it.remove();
            if (a.bar != null) a.bar.removeAllPlayers();

            ServerPlayer owner = server.getPlayerList().getPlayer(a.playerId);
            if (owner == null) return;

            MinecraftForge.EVENT_BUS.post(new QuestEvents.WaveCleared(owner, a.questId, a.wave));

            // 触发链式下一波
            QuestRegistry.INSTANCE.get(a.questId).ifPresent(def -> {
                List<WaveSpawnDef> ws = def.waves();
                int idx = -1;
                for (int i = 0; i < ws.size(); i++)
                    if (ws.get(i).id.equals(a.wave.id)) { idx = i; break; }
                for (int i = idx + 1; i < ws.size(); i++) {
                    if (ws.get(i).trigger == WaveSpawnDef.Trigger.AFTER_PREVIOUS_CLEARED) {
                        schedule(owner, a.questId, ws.get(i), owner.blockPosition());
                        break;
                    }
                }
            });
            return;
        }
    }

    /** 玩家下线时收掉血条 */
    public static void onPlayerLogout(ServerPlayer player) {
        for (Active a : ACTIVE)
            if (a.bar != null && a.playerId.equals(player.getUUID()))
                a.bar.removePlayer(player);
    }

    /** 玩家上线时恢复血条显示 */
    /** 服务器关闭时清空。原来是静态列表且从不清理，单人游戏退出再进入别的存档时，旧存档的波次还会残留 */
    public static void clear() {
        for (Active a : ACTIVE)
            if (a.bar != null) a.bar.removeAllPlayers();
        ACTIVE.clear();
        PENDING.clear();
        tickCounter = 0;
    }

    public static void onPlayerLogin(ServerPlayer player) {
        for (Active a : ACTIVE)
            if (a.bar != null && a.playerId.equals(player.getUUID()))
                a.bar.addPlayer(player);
    }

    /** 查询某玩家某任务是否还有未清空的波次 */
    public static boolean hasUnclearedWaves(ServerPlayer p, ResourceLocation questId) {
        return ACTIVE.stream().anyMatch(a -> a.playerId.equals(p.getUUID())
                && a.questId.equals(questId) && a.wave.mustClear && !a.alive.isEmpty())
                || PENDING.stream().anyMatch(pd -> pd.playerId.equals(p.getUUID())
                && pd.questId.equals(questId));
    }
}