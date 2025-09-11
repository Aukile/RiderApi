package net.ankrya.rider_api.mixin.timer;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
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
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
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

    @Unique
    private int rider_api$slowTickCounter = 0; // 计数器

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


    @Shadow protected abstract void tickPassenger(Entity p_8663_, Entity p_8664_);

    @Inject(method = {"tickBlock", "tickChunk", "tickFluid", "tickTime", "tickCustomSpawners", "blockUpdated", "advanceWeatherCycle", "runBlockEvents", "gameEvent"}, at = {@At("HEAD")}, cancellable = true)
    private void freezeServerTick(CallbackInfo ci) {
        if((int) Variables.getVariable(this, ModVariable.TIME_STATUS) == 2){
            ci.cancel();
        }
    }


    @Inject(method = "tick",at = @At("HEAD"),cancellable = true)
    public void tick(BooleanSupplier p_8794_, CallbackInfo ci) {

        int time_state = Variables.getVariable(this, ModVariable.TIME_STATUS);



        if (time_state != 0) {
            if (time_state  == 1) {
                this.rider_api$specialEntityTimeSlow(p_8794_);
                rider_api$slowTickCounter++;
                if (rider_api$slowTickCounter % 10 ==0) {
                    rider_api$slowTickCounter = 0;
                }else ci.cancel();
            }
        }




    }

    @Inject(method = "tickNonPassenger",at = @At("HEAD"),cancellable = true)
    public void tickNonPassengerM(Entity p_8648_,CallbackInfo ci) {
        int time_state = Variables.getVariable(this, ModVariable.TIME_STATUS);

        if (time_state == 1 ){
            if (GJ.TimerControl.isSlowEntity(p_8648_)){

                for (Entity entity : p_8648_.getPassengers()) {
                    this.tickPassenger(p_8648_, entity);
                }
                ci.cancel();
            }
        }else if (time_state == 2 ) {
            if (!GJ.TimerControl.isPauseEntity(p_8648_)){

                for (Entity entity : p_8648_.getPassengers()) {
                    this.tickPassenger(p_8648_, entity);
                }
                ci.cancel();
            }
        }

    }

    @Inject(method = "tickPassenger",at = @At("HEAD"),cancellable = true)
    public void tickPassengerM(Entity p_8663_, Entity p_8664_,CallbackInfo ci) {
        int time_state = (int) Variables.getVariable(this, ModVariable.TIME_STATUS);
        if (time_state == 1) {

            if (GJ.TimerControl.isSlowEntity(p_8664_)){
                ci.cancel();
            }
        }else if (time_state == 2) {
            if (!GJ.TimerControl.isPauseEntity(p_8664_)){
                ci.cancel();
            }
        }
    }

    @Unique
    @Override
    public void rider_api$specialEntityTimeSlow(BooleanSupplier booleanSupplier){

        ProfilerFiller profilerfiller = this.getProfiler();

        this.entityTickList.forEach((p_184065_) -> {
            if(!GJ.TimerControl.isSlowEntity(p_184065_)){
                if (!p_184065_.isRemoved() && !(p_184065_ instanceof PartEntity)) {
                    this.guardEntityTick(this::rider_api$tickNonPassengerTimeSlow, p_184065_);
                }
                return;
            }
            if (!p_184065_.isRemoved()) {
                if (this.shouldDiscardEntity(p_184065_)) {
                    p_184065_.discard();
                } else {
                    profilerfiller.push("checkDespawn");
                    p_184065_.checkDespawn();
                    profilerfiller.pop();
                    if (this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(p_184065_.chunkPosition().toLong())) {
                        Entity entity = p_184065_.getVehicle();
                        if (entity != null) {
                            if (!entity.isRemoved() && entity.hasPassenger(p_184065_)) {
                                return;
                            }

                            p_184065_.stopRiding();
                        }

                        profilerfiller.push("tick");
                        if (!p_184065_.isRemoved() && !(p_184065_ instanceof PartEntity)) {
                            this.guardEntityTick(this::rider_api$tickNonPassengerTimeSlow, p_184065_);
                        }

                        profilerfiller.pop();
                    }
                }
            }

        });


    }


    @Unique
    public void rider_api$tickNonPassengerTimeSlow(Entity entity1) {

        if (GJ.TimerControl.isSlowEntity(entity1)) {
            entity1.setOldPosAndRot();
            ProfilerFiller profilerfiller = this.getProfiler();
            ++entity1.tickCount;
            this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            if (!EventHooks.fireEntityTickPre(entity1).isCanceled()) {
                entity1.tick();
                EventHooks.fireEntityTickPost(entity1);
            }

            this.getProfiler().pop();
        }

        for (Entity entity : entity1.getPassengers()) {
            this.rider_api$tickPassengerTimeSlow(entity1, entity);
        }

    }

    @Unique
    private void rider_api$tickPassengerTimeSlow(Entity vehicle, Entity entity1) {

        if (!GJ.TimerControl.isSlowEntity(entity1)) return;

        if (!entity1.isRemoved() && entity1.getVehicle() == vehicle) {
            if (entity1 instanceof Player || this.entityTickList.contains(entity1)) {
                entity1.setOldPosAndRot();
                ++entity1.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() -> {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString();
                });
                profilerfiller.incrementCounter("tickPassenger");
                if (!EventHooks.fireEntityTickPre(entity1).isCanceled()) {
                    entity1.tick();
                    EventHooks.fireEntityTickPost(entity1);
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

    @Unique
    @Override
    public void rider_api$specialEntityTimePause(BooleanSupplier booleanSupplier){

        ProfilerFiller profilerfiller = this.getProfiler();

        this.entityTickList.forEach((p_184065_) -> {
            if(!GJ.TimerControl.isPauseEntity(p_184065_)){
                if (!p_184065_.isRemoved() && !(p_184065_ instanceof PartEntity)) {
                    this.guardEntityTick(this::rider_api$tickNonPassengerTimePause, p_184065_);
                }
                return;
            }
            if (!p_184065_.isRemoved()) {
                if (this.shouldDiscardEntity(p_184065_)) {
                    p_184065_.discard();
                } else {
                    profilerfiller.push("checkDespawn");
                    p_184065_.checkDespawn();
                    profilerfiller.pop();
                    if (this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(p_184065_.chunkPosition().toLong())) {
                        Entity entity = p_184065_.getVehicle();
                        if (entity != null) {
                            if (!entity.isRemoved() && entity.hasPassenger(p_184065_)) {
                                return;
                            }

                            p_184065_.stopRiding();
                        }

                        profilerfiller.push("tick");
                        if (!p_184065_.isRemoved() && !(p_184065_ instanceof PartEntity)) {
                            this.guardEntityTick(this::rider_api$tickNonPassengerTimePause, p_184065_);
                        }

                        profilerfiller.pop();
                    }
                }
            }

        });


    }


    @Unique
    public void rider_api$tickNonPassengerTimePause(Entity entity1) {
        if (GJ.TimerControl.isSlowEntity(entity1)) {
            entity1.setOldPosAndRot();
            ProfilerFiller profilerfiller = this.getProfiler();
            ++entity1.tickCount;
            this.getProfiler().push(() -> {
                return BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString();
            });
            profilerfiller.incrementCounter("tickNonPassenger");
            if (!EventHooks.fireEntityTickPre(entity1).isCanceled()) {
                entity1.tick();
                EventHooks.fireEntityTickPost(entity1);
            }

            this.getProfiler().pop();
        }

        for (Entity entity : entity1.getPassengers()) {
            this.rider_api$tickPassengerTimeSlow(entity1, entity);
        }

    }

    @Unique
    private void rider_api$tickPassengerTimePause(Entity vehicle, Entity entity1) {

        if (!GJ.TimerControl.isSlowEntity(entity1)) return;

        if (!entity1.isRemoved() && entity1.getVehicle() == vehicle) {
            if (entity1 instanceof Player || this.entityTickList.contains(entity1)) {
                entity1.setOldPosAndRot();
                ++entity1.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() -> {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString();
                });
                profilerfiller.incrementCounter("tickPassenger");
                if (!EventHooks.fireEntityTickPre(entity1).isCanceled()) {
                    entity1.tick();
                    EventHooks.fireEntityTickPost(entity1);
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

}
