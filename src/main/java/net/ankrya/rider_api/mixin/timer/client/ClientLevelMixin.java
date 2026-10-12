package net.ankrya.rider_api.mixin.timer.client;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.help.TimeRateHelper;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.interfaces.timer.TimerClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level implements TimerClientLevel {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow @Final
    EntityTickList tickingEntities;

    /** 本 mixin 主动调用 tickNonPassenger / tickPassenger 时，注入点看到这个实体就放行 */
    @Unique
    @Nullable
    private Entity rider_api$bypass = null;

    /** 最近是否见过单独时速的实体；没有的话正常状态下每帧就不用遍历实体了 */
    @Unique
    private boolean rider_api$customRateSeen = false;

    /** 正在按单独时速 tick 的嵌套层数；此时玩家乘客不跟着载具的速率走 */
    @Unique
    private int rider_api$rateDepth = 0;

    protected ClientLevelMixin(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Shadow
    protected abstract void tickPassenger(Entity p104640, Entity entity);

    @Shadow
    public abstract void tickNonPassenger(Entity p_104640_);

    @Inject(method = {"animateTick", "tick", "tickTime"}, at = {@At("HEAD")}, cancellable = true)
    private void pauseClientTick(CallbackInfo ci) {
        if (this.minecraft != null){
            int time_state = Variables.getVariable(this, ModVariable.TIME_STATUS);
            if (time_state == 2)
                ci.cancel();
        }
    }

    @Inject(method = "tickNonPassenger",at = @At("HEAD"),cancellable = true)
    public void tickNonPassengerM(Entity entity, CallbackInfo ci) {
        if (entity == this.rider_api$bypass) {
            this.rider_api$bypass = null;
            return;
        }
        if (this.minecraft == null) return;

        int time_state = GJ.TimerControl.getWorldTime(this);
        if (time_state == ITimer.timeStop) {
            if (!GJ.TimerControl.isPauseEntity(entity)) {
                for (Entity passenger : entity.getPassengers()) {
                    this.tickPassenger(entity, passenger);
                }
                ci.cancel();
            }
            return;
        }

        if (time_state == ITimer.timeSlow && GJ.TimerControl.isSlowEntity(entity)) {
            for (Entity passenger : entity.getPassengers()) {
                this.tickPassenger(entity, passenger);
            }
            ci.cancel();
            return;
        }

        // 单独时速的实体改为每帧按自己的速率 tick（见 rider_api$tickEntitiesByRate），这里跳过
        if (GJ.TimerControl.getCustomTimeRate(entity, time_state) >= 0f) {
            this.rider_api$customRateSeen = true;
            // 骑在上面的玩家仍按自己的速度走
            for (Entity passenger : entity.getPassengers()) {
                if (passenger instanceof Player) this.tickPassenger(entity, passenger);
            }
            ci.cancel();
        }
    }

    @Inject(method = "tickPassenger",at = @At("HEAD"),cancellable = true)
    public void tickPassengerM(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if (passenger == this.rider_api$bypass) {
            this.rider_api$bypass = null;
            return;
        }
        if (this.minecraft == null) return;
        if (this.rider_api$rateDepth > 0 && passenger instanceof Player) {
            ci.cancel(); // 玩家不跟着单独时速的载具一起变速
            return;
        }

        int time_state = GJ.TimerControl.getWorldTime(this);
        if (time_state == ITimer.timeStop) {
            if (!GJ.TimerControl.isPauseEntity(passenger)) ci.cancel();
            return;
        }

        if (time_state == ITimer.timeSlow && GJ.TimerControl.isSlowEntity(passenger)) {
            ci.cancel();
            return;
        }

        if (GJ.TimerControl.getCustomTimeRate(passenger, time_state) >= 0f) {
            this.rider_api$customRateSeen = true;
            ci.cancel();
        }
    }

    // ---------------- 单独时速：每帧执行 ----------------

    @Unique
    @Override
    public void rider_api$tickEntitiesByRate(float realTicks) {
        int timeState = GJ.TimerControl.getWorldTime(this);
        if (timeState == ITimer.timeStop) return;
        if (timeState == ITimer.timeNormal && !this.rider_api$customRateSeen) return;

        boolean[] found = {false};
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("rider_api_time_rate");
        this.tickingEntities.forEach(entity -> {
            if (entity.isRemoved() || entity.isPassenger()) return;

            float rate = GJ.TimerControl.getCustomTimeRate(entity, timeState);
            if (rate >= 0f) {
                found[0] = true;
                this.guardEntityTick(e -> this.rider_api$tickRootByRate(e, rate * realTicks, realTicks, timeState, found), entity);
            } else if (!entity.getPassengers().isEmpty()) {
                this.guardEntityTick(e -> this.rider_api$visitCustomPassengers(e, realTicks, timeState, found), entity);
            }
        });
        profilerfiller.pop();
        this.rider_api$customRateSeen = found[0];
    }

    @Unique
    private void rider_api$tickRootByRate(Entity entity, float amount, float realTicks, int timeState, boolean[] found) {
        int steps = TimeRateHelper.consume(entity, amount);
        for (int i = 0; i < steps && !entity.isRemoved(); i++) {
            this.rider_api$bypass = entity;
            this.rider_api$rateDepth++;
            try {
                this.tickNonPassenger(entity);
            } finally {
                this.rider_api$rateDepth--;
                this.rider_api$bypass = null;
            }
        }
        // 身上有自己速率的乘客，按乘客自己的速率走
        this.rider_api$visitCustomPassengers(entity, realTicks, timeState, found);
    }

    @Unique
    private void rider_api$visitCustomPassengers(Entity vehicle, float realTicks, int timeState, boolean[] found) {
        for (Entity passenger : vehicle.getPassengers()) {
            float rate = GJ.TimerControl.getCustomTimeRate(passenger, timeState);
            if (rate >= 0f) {
                found[0] = true;
                int steps = TimeRateHelper.consume(passenger, rate * realTicks);
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
            this.rider_api$visitCustomPassengers(passenger, realTicks, timeState, found);
        }
    }

    // ---------------- 时缓免疫实体（原有逻辑） ----------------

    @Unique
    @Override
    public void rider_api$tickEntitiesTimeSlow() {
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("entities");
        this.tickingEntities.forEach((p_194183_) -> {
            if (!p_194183_.isRemoved() && !p_194183_.isPassenger()) {
                this.guardEntityTick(this::rider_api$tickNonPassengerTimeSlow, p_194183_);
            }

        });
        profilerfiller.pop();

    }

    @Unique
    @Override
    public void rider_api$tickNonPassengerTimeSlow(Entity entity1) {
        if (GJ.TimerControl.isSlowEntity(entity1)){
            entity1.setOldPosAndRot();
            ++entity1.tickCount;
            this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            if (entity1.canUpdate()) {
                entity1.tick();
            }

            this.getProfiler().pop();
        }

        for (Entity entity : entity1.getPassengers()) {
            this.rider_api$tickPassengerTimeSlow(entity1, entity);
        }
    }

    @Unique
    @Override
    public void rider_api$tickPassengerTimeSlow(Entity vehicle, Entity entity1) {
        if (!GJ.TimerControl.isSlowEntity(entity1)) {
            // 原来直接 return，免疫实体骑在“普通乘客”身上时就不会被 tick
            for (Entity entity : entity1.getPassengers()) {
                this.rider_api$tickPassengerTimeSlow(entity1, entity);
            }
            return;
        }

        if (!entity1.isRemoved() && entity1.getVehicle() == vehicle) {
            if (entity1 instanceof Player || this.tickingEntities.contains(entity1)) {
                entity1.setOldPosAndRot();
                ++entity1.tickCount;
                entity1.rideTick();

                for (Entity entity : entity1.getPassengers()) {
                    this.tickPassenger(entity1, entity);
                }
            }
        } else {
            entity1.stopRiding();
        }

    }


    @Unique
    @Override
    public void rider_api$tickEntitiesTimePause() {
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("entities");
        this.tickingEntities.forEach((p_194183_) -> {
            if (!p_194183_.isRemoved() && !p_194183_.isPassenger()) {
                this.guardEntityTick(this::rider_api$tickNonPassengerTimePause, p_194183_);
            }

        });
        profilerfiller.pop();

    }

    @Unique
    @Override
    public void rider_api$tickNonPassengerTimePause(Entity entity1) {

        if (GJ.TimerControl.isPauseEntity(entity1)){
            entity1.setOldPosAndRot();
            ++entity1.tickCount;
            this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            if (entity1.canUpdate()) {
                entity1.tick();
            }

            this.getProfiler().pop();
        }

        for (Entity entity : entity1.getPassengers()) {
            this.rider_api$tickPassengerTimePause(entity1, entity);
        }
    }

    @Unique
    @Override
    public void rider_api$tickPassengerTimePause(Entity vehicle, Entity entity1) {

        if (! GJ.TimerControl.isPauseEntity(entity1)) return;

        if (!entity1.isRemoved() && entity1.getVehicle() == vehicle) {
            if (entity1 instanceof Player || this.tickingEntities.contains(entity1)) {
                entity1.setOldPosAndRot();
                ++entity1.tickCount;
                entity1.rideTick();

                for (Entity entity : entity1.getPassengers()) {
                    this.tickPassenger(entity1, entity);
                }
            }
        } else {
            entity1.stopRiding();
        }

    }

}
