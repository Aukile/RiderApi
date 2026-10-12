package net.ankrya.rider_api.mixin.timer;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.help.TimeRateHelper;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.interfaces.timer.TimerServerLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements TimerServerLevel {

    /**
     * 时缓时“世界本身”的 tick 累加器。<br>
     * 原来是写死的 {@code 计数器 % 10}，和客户端用的 TIME_SLOW_TIMER 对不上（改了时缓倍率两端就不同步），
     * 现在按 {@link GJ.TimerControl#getWorldSlowRate} 累加，默认 500ms 时依然是每 10 tick 走一次
     */
    @Unique
    private float rider_api$worldTimeAcc = 0f;

    /** 本 mixin 主动调用 tickNonPassenger / tickPassenger 时，注入点看到这个实体就直接放行，防止递归 */
    @Unique
    @Nullable
    private Entity rider_api$bypass = null;

    /** 正在按单独时速 tick 的嵌套层数；此时玩家乘客不跟着载具的速率走 */
    @Unique
    private int rider_api$rateDepth = 0;

    @Final
    @Shadow
    EntityTickList entityTickList;

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract @NotNull ServerChunkCache getChunkSource();

    @Shadow
    protected abstract boolean shouldDiscardEntity(Entity p_143343_);

    @Shadow
    protected abstract void tickPassenger(Entity p_8663_, Entity p_8664_);

    @Shadow
    public abstract void tickNonPassenger(Entity p_8648_);

    @Inject(method = {"tickBlock", "tickChunk", "tickFluid", "tickTime", "tickCustomSpawners", "blockUpdated", "advanceWeatherCycle", "runBlockEvents", "gameEvent"}, at = {@At("HEAD")}, cancellable = true)
    private void freezeServerTick(CallbackInfo ci) {
        if ((int) Variables.getVariable(this, ModVariable.TIME_STATUS) == ITimer.timeStop) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        int timeState = GJ.TimerControl.getWorldTime(this);
        if (timeState == ITimer.timeSlow) {
            // 免疫实体、单独时速实体：每个真实 tick 都处理
            this.rider_api$specialEntityTimeSlow(hasTimeLeft);

            this.rider_api$worldTimeAcc += GJ.TimerControl.getWorldSlowRate(this);
            if (this.rider_api$worldTimeAcc >= 1f) {
                this.rider_api$worldTimeAcc -= 1f; // 这个 tick 世界正常走一次
            } else ci.cancel();
        } else {
            this.rider_api$worldTimeAcc = 0f;
        }
    }

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    public void tickNonPassengerM(Entity entity, CallbackInfo ci) {
        if (entity == this.rider_api$bypass) {
            this.rider_api$bypass = null;
            return;
        }

        int timeState = GJ.TimerControl.getWorldTime(this);
        if (timeState == ITimer.timeStop) {
            if (!GJ.TimerControl.isPauseEntity(entity)) {
                for (Entity passenger : entity.getPassengers()) {
                    this.tickPassenger(entity, passenger);
                }
                ci.cancel();
            }
            return;
        }

        if (timeState == ITimer.timeSlow && GJ.TimerControl.isSlowEntity(entity)) {
            // 免疫实体已经在 specialEntityTimeSlow 里按正常速度 tick 过了，这里只让乘客按世界速度走
            for (Entity passenger : entity.getPassengers()) {
                this.tickPassenger(entity, passenger);
            }
            ci.cancel();
            return;
        }

        float rate = GJ.TimerControl.getCustomTimeRate(entity, timeState);
        if (rate >= 0f) {
            this.rider_api$tickRootByRate(entity, rate);
            // 骑在上面的玩家仍按自己的速度走
            for (Entity passenger : entity.getPassengers()) {
                if (passenger instanceof Player) this.tickPassenger(entity, passenger);
            }
            ci.cancel();
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    public void tickPassengerM(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if (passenger == this.rider_api$bypass) {
            this.rider_api$bypass = null;
            return;
        }
        if (this.rider_api$rateDepth > 0 && passenger instanceof Player) {
            ci.cancel(); // 玩家不跟着单独时速的载具一起变速
            return;
        }

        int timeState = GJ.TimerControl.getWorldTime(this);
        if (timeState == ITimer.timeStop) {
            if (!GJ.TimerControl.isPauseEntity(passenger)) ci.cancel();
            return;
        }

        if (timeState == ITimer.timeSlow && GJ.TimerControl.isSlowEntity(passenger)) {
            ci.cancel();
            return;
        }

        float rate = GJ.TimerControl.getCustomTimeRate(passenger, timeState);
        if (rate >= 0f) {
            this.rider_api$tickPassengerByRate(vehicle, passenger, rate);
            ci.cancel();
        }
    }

    // ---------------- 单独时速 ----------------

    @Unique
    private long rider_api$serverTick() {
        return this.getServer() == null ? this.getGameTime() : this.getServer().getTickCount();
    }

    /** 按实体自己的速率 tick 一个“根”实体（不是乘客的实体），同一个服务器 tick 只累加一次 */
    @Unique
    private void rider_api$tickRootByRate(Entity entity, float rate) {
        if (!TimeRateHelper.markTicked(entity, rider_api$serverTick())) return;

        int steps = TimeRateHelper.consume(entity, rate);
        for (int i = 0; i < steps && !entity.isRemoved(); i++) {
            this.rider_api$bypass = entity;
            this.rider_api$rateDepth++;
            try {
                this.tickNonPassenger(entity); // 走原版逻辑（含其他模组的注入），乘客也会跟着 tick
            } finally {
                this.rider_api$rateDepth--;
                this.rider_api$bypass = null;
            }
        }
        // 载具这个 tick 可能一次都没动，身上有单独时速的乘客也要按自己的速率走
        this.rider_api$visitSpecialPassengers(entity, GJ.TimerControl.getWorldTime(this));
    }

    @Unique
    private void rider_api$tickPassengerByRate(Entity vehicle, Entity passenger, float rate) {
        if (!TimeRateHelper.markTicked(passenger, rider_api$serverTick())) return;

        int steps = TimeRateHelper.consume(passenger, rate);
        for (int i = 0; i < steps && !passenger.isRemoved() && passenger.getVehicle() == vehicle; i++) {
            this.rider_api$bypass = passenger;
            this.rider_api$rateDepth++;
            try {
                this.tickPassenger(vehicle, passenger);
            } finally {
                this.rider_api$rateDepth--;
                this.rider_api$bypass = null;
            }
        }
    }

    /**
     * 递归处理载具身上需要“特殊对待”的乘客：
     * 单独时速的乘客按自己的速率；时缓中（timeState == 1）免疫的乘客按正常速度
     */
    @Unique
    private void rider_api$visitSpecialPassengers(Entity vehicle, int timeState) {
        for (Entity passenger : vehicle.getPassengers()) {
            if (timeState == ITimer.timeSlow && GJ.TimerControl.isSlowEntity(passenger)) {
                this.rider_api$tickPassengerTimeSlow(vehicle, passenger);
            } else {
                float rate = GJ.TimerControl.getCustomTimeRate(passenger, timeState);
                if (rate >= 0f) this.rider_api$tickPassengerByRate(vehicle, passenger, rate);
            }
            this.rider_api$visitSpecialPassengers(passenger, timeState);
        }
    }

    // ---------------- 时缓：每个真实 tick 都执行 ----------------

    @Unique
    @Override
    public void rider_api$specialEntityTimeSlow(BooleanSupplier booleanSupplier) {
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("rider_api_time_slow");

        this.entityTickList.forEach(entity -> {
            if (entity.isRemoved() || entity instanceof PartEntity) return;

            boolean immune = GJ.TimerControl.isSlowEntity(entity);
            if (immune && this.shouldDiscardEntity(entity)) {
                entity.discard();
                return;
            }
            if (!this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong())) return;

            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                if (!vehicle.isRemoved() && vehicle.hasPassenger(entity)) return; // 乘客跟着根实体一起处理
                entity.stopRiding();
            }

            if (immune) {
                profilerfiller.push("checkDespawn");
                entity.checkDespawn();
                profilerfiller.pop();
            }
            if (entity.isRemoved()) return;

            if (immune) {
                this.guardEntityTick(this::rider_api$tickNonPassengerTimeSlow, entity);
            } else {
                float rate = GJ.TimerControl.getCustomTimeRate(entity, ITimer.timeSlow);
                if (rate >= 0f) this.guardEntityTick(e -> this.rider_api$tickRootByRate(e, rate), entity);
                else if (!entity.getPassengers().isEmpty())
                    this.guardEntityTick(e -> this.rider_api$visitSpecialPassengers(e, ITimer.timeSlow), entity);
            }
        });

        profilerfiller.pop();
    }

    @Unique
    public void rider_api$tickNonPassengerTimeSlow(Entity entity1) {
        if (GJ.TimerControl.isSlowEntity(entity1)) {
            entity1.setOldPosAndRot();
            ProfilerFiller profilerfiller = this.getProfiler();
            ++entity1.tickCount;
            profilerfiller.push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            if (entity1.canUpdate()) {
                entity1.tick();
            }
            profilerfiller.pop();
        }

        this.rider_api$visitSpecialPassengers(entity1, ITimer.timeSlow);
    }

    @Unique
    private void rider_api$tickPassengerTimeSlow(Entity vehicle, Entity entity1) {
        if (!GJ.TimerControl.isSlowEntity(entity1)) return;

        if (!entity1.isRemoved() && entity1.getVehicle() == vehicle) {
            if (entity1 instanceof Player || this.entityTickList.contains(entity1)) {
                entity1.setOldPosAndRot();
                ++entity1.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
                profilerfiller.incrementCounter("tickPassenger");
                if (entity1.canUpdate()) {
                    entity1.rideTick();
                }
                profilerfiller.pop();

                for (Entity entity : entity1.getPassengers()) {
                    this.tickPassenger(entity1, entity);
                }
            }
        } else {
            entity1.stopRiding();
        }
    }

    // ---------------- 时停（目前没有地方调用，保留接口；原来的实现复制粘贴错成了时缓的判断，已改正） ----------------

    @Unique
    @Override
    public void rider_api$specialEntityTimePause(BooleanSupplier booleanSupplier) {
        this.entityTickList.forEach(entity -> {
            if (entity.isRemoved() || entity instanceof PartEntity) return;
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                if (!vehicle.isRemoved() && vehicle.hasPassenger(entity)) return;
                entity.stopRiding();
            }
            if (this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong())) {
                this.guardEntityTick(this::rider_api$tickNonPassengerTimePause, entity);
            }
        });
    }

    @Unique
    public void rider_api$tickNonPassengerTimePause(Entity entity1) {
        if (GJ.TimerControl.isPauseEntity(entity1)) {
            entity1.setOldPosAndRot();
            ProfilerFiller profilerfiller = this.getProfiler();
            ++entity1.tickCount;
            profilerfiller.push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            if (entity1.canUpdate()) {
                entity1.tick();
            }
            profilerfiller.pop();
        }

        for (Entity entity : entity1.getPassengers()) {
            this.rider_api$tickPassengerTimePause(entity1, entity);
        }
    }

    @Unique
    private void rider_api$tickPassengerTimePause(Entity vehicle, Entity entity1) {
        if (!GJ.TimerControl.isPauseEntity(entity1)) return;

        if (!entity1.isRemoved() && entity1.getVehicle() == vehicle) {
            if (entity1 instanceof Player || this.entityTickList.contains(entity1)) {
                entity1.setOldPosAndRot();
                ++entity1.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
                profilerfiller.incrementCounter("tickPassenger");
                if (entity1.canUpdate()) {
                    entity1.rideTick();
                }
                profilerfiller.pop();

                for (Entity entity : entity1.getPassengers()) {
                    this.rider_api$tickPassengerTimePause(entity1, entity);
                }
            }
        } else {
            entity1.stopRiding();
        }
    }
}
