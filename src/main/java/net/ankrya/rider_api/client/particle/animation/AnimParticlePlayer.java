package net.ankrya.rider_api.client.particle.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * 沿动画骨骼轨迹生成粒子，支持 pivot 感知的父级 FK 变换。
 */
public class AnimParticlePlayer {

    private final IEventBus bus;
    private final UUID playerUUID;
    private final BoneTrack track;

    /** 目标骨骼自身 pivot（方块单位，Z 已按 MC 取反） */
    private final Vec3 selfPivot;
    /** 父骨骼轨道，顺序 [直接父级 → 根] */
    private final BoneTrack[] parents;
    /** 与 parents 对应的 pivot */
    private final Vec3[] parentPivots;

    private final BiConsumer<Level, Vec3> spawner;

    private float tick = 0;
    private boolean active = true;

    public AnimParticlePlayer(UUID playerUUID, ResourceLocation animation,
                              String boneName,
                              BiConsumer<Level, Vec3> spawner){
        this(playerUUID, animation, boneName, new String[0], new Vec3[0], spawner);
    }

    public AnimParticlePlayer(UUID playerUUID, ResourceLocation animation,
                              String boneName,
                              String[] parentBones, Vec3[] parentPivots,
                              BiConsumer<Level, Vec3> spawner){
        this(playerUUID, animation, boneName, Vec3.ZERO, parentBones, parentPivots, spawner);
    }

    /**
     * @param boneName      采样位置的目标骨骼
     * @param selfPivot     目标骨骼在 geo 里的 pivot（原始 Blockbench 值，本类内部会 /16 且 Z 取反）
     * @param parentBones   父骨骼名，从直接父级到根
     * @param parentPivots  与 parentBones 一一对应的 geo pivot（原始 Blockbench 值）
     */
    public AnimParticlePlayer(UUID playerUUID, ResourceLocation animation,
                              String boneName, Vec3 selfPivot,
                              String[] parentBones, Vec3[] parentPivots,
                              BiConsumer<Level, Vec3> spawner) {
        this(MinecraftForge.EVENT_BUS, playerUUID, animation, boneName, selfPivot,
                parentBones, parentPivots, spawner);
    }

    public AnimParticlePlayer(IEventBus bus, UUID playerUUID, ResourceLocation animation,
                              String boneName, Vec3 selfPivot,
                              String[] parentBones, Vec3[] parentPivots,
                              BiConsumer<Level, Vec3> spawner) {
        this.bus = bus;
        this.playerUUID = playerUUID;
        this.spawner = spawner;
        this.track = AnimationBoneLoader.load(animation, boneName);
        this.selfPivot = toMc(selfPivot);

        if (track == null) {
            this.parents = new BoneTrack[0];
            this.parentPivots = new Vec3[0];
            this.active = false;
            return;
        }

        this.parents = new BoneTrack[parentBones.length];
        this.parentPivots = new Vec3[parentBones.length];
        for (int i = 0; i < parentBones.length; i++) {
            this.parents[i] = AnimationBoneLoader.load(animation, parentBones[i]);
            this.parentPivots[i] = toMc(parentPivots[i]);
        }

        bus.register(this);
    }

    /** Blockbench pivot → MC 空间（/16，Z 取反），与 loader 位置约定保持一致 */
    private static Vec3 toMc(Vec3 bbPivot) {
        if (bbPivot == null) return Vec3.ZERO;
        return new Vec3(bbPivot.x / 16.0, bbPivot.y / 16.0, -bbPivot.z / 16.0);
    }

    public void stop() {
        if (!active) return;
        active = false;
        bus.unregister(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.level == null) return;

        Player player = mc.level.getPlayerByUUID(playerUUID);
        if (player == null) { stop(); return; }

        // 目标骨骼局部位置：动画位移是相对自身 pivot 的偏移
        // 起点放到自身 pivot 处，再加动画位移
        Vec3 local = selfPivot.add(track.samplePosition(tick));

        // 依次套父级：绕父 pivot 旋转，再加父 pivot 与父动画位移
        for (int i = 0; i < parents.length; i++) {
            BoneTrack parent = parents[i];
            if (parent == null) continue;
            Vec3 pivot = parentPivots[i];
            Vec3 euler = parent.sampleRotation(tick);
            Vec3 animPos = parent.samplePosition(tick);

            // p = pivot + R·(p - pivot) + animPos
            Vec3 rel = local.subtract(pivot);
            rel = applyEuler(rel, euler);
            local = rel.add(pivot).add(animPos);
        }

        // 按玩家朝向摆放到世界
        Vec3 world = local.yRot(-player.getYRot() * Mth.DEG_TO_RAD)
                .add(player.getX(), player.getY(), player.getZ());

        spawner.accept(mc.level, world);

        tick++;
        if (tick >= track.maxTick) stop();
    }

    /** Blockbench 欧拉顺序 Z→Y→X（loader 已对旋转 Z 取反） */
    private static Vec3 applyEuler(Vec3 p, Vec3 euler) {
        float rx = (float) Math.toRadians(euler.x);
        float ry = (float) Math.toRadians(euler.y);
        float rz = (float) Math.toRadians(euler.z);
        return p.zRot(rz).yRot(ry).xRot(rx);
    }
}