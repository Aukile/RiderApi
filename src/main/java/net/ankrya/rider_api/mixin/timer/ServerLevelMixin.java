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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements TimerServerLevel {

    @Unique
    private int rider_api$slowTickCounter = 0; // 计数器

    @Final
    @Shadow
    EntityTickList entityTickList;

    protected ServerLevelMixin(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Shadow
    public abstract @NotNull ServerChunkCache getChunkSource();

    @Shadow
    protected abstract boolean shouldDiscardEntity(Entity p_143343_);


    @Shadow protected abstract void tickPassenger(Entity p_8663_, Entity p_8664_);

    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;


    @Shadow private boolean handlingTick;

    @Shadow @Final private SleepStatus sleepStatus;

    @Shadow @Final private List<ServerPlayer> players;

    @Shadow public abstract void setDayTime(long p_8616_);

    @Shadow protected abstract void wakeUpAllPlayers();

    @Shadow protected abstract void resetWeatherCycle();

    @Inject(method = {"tickBlock", "tickChunk", "tickFluid", "tickTime", "tickCustomSpawners", "blockUpdated", "advanceWeatherCycle", "runBlockEvents", "gameEvent"}, at = {@At("HEAD")}, cancellable = true)
    private void freezeServerTick(CallbackInfo ci) {
        if((int) Variables.getVariable(this, ModVariable.TIME_STATUS) == 2){
            ci.cancel();
        }
    }


    @Inject(method = "tick",at = @At("HEAD"),cancellable = true)
    public void tick(BooleanSupplier p_8794_, CallbackInfo ci) {

        int time_state = (int) Variables.getVariable(this, ModVariable.TIME_STATUS);



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
        int time_state = (int) Variables.getVariable(this, ModVariable.TIME_STATUS);

        if (time_state == 1 ){
            if (GJ.TimerControl.isSlowEntity(p_8648_)){
                Iterator var3M = p_8648_.getPassengers().iterator();

                while(var3M.hasNext()) {
                    Entity entity = (Entity)var3M.next();
                    this.tickPassenger(p_8648_, entity);
                }
                ci.cancel();
            }
        }else if (time_state == 2 ) {
            if (!GJ.TimerControl.isPauseEntity(p_8648_)){
                Iterator var3M = p_8648_.getPassengers().iterator();

                while(var3M.hasNext()) {
                    Entity entity = (Entity)var3M.next();
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
    public void rider_api$tickNonPassengerTimeSlow(Entity p_8648_) {

        if (GJ.TimerControl.isSlowEntity(p_8648_)) {
            p_8648_.setOldPosAndRot();
            ProfilerFiller profilerfiller = this.getProfiler();
            ++p_8648_.tickCount;
            this.getProfiler().push(() -> {
                return BuiltInRegistries.ENTITY_TYPE.getKey(p_8648_.getType()).toString();
            });
            profilerfiller.incrementCounter("tickNonPassenger");
            if (p_8648_.canUpdate()) {
                p_8648_.tick();
            }

            this.getProfiler().pop();
        }
        Iterator var3 = p_8648_.getPassengers().iterator();

        while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            this.rider_api$tickPassengerTimeSlow(p_8648_, entity);
        }

    }

    @Unique
    private void rider_api$tickPassengerTimeSlow(Entity p_8663_, Entity p_8664_) {

        if (!GJ.TimerControl.isSlowEntity(p_8664_)) return;

        if (!p_8664_.isRemoved() && p_8664_.getVehicle() == p_8663_) {
            if (p_8664_ instanceof Player || this.entityTickList.contains(p_8664_)) {
                p_8664_.setOldPosAndRot();
                ++p_8664_.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() -> {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(p_8664_.getType()).toString();
                });
                profilerfiller.incrementCounter("tickPassenger");
                if (p_8664_.canUpdate()) {
                    p_8664_.rideTick();
                }

                profilerfiller.pop();
                Iterator var4 = p_8664_.getPassengers().iterator();

                while(var4.hasNext()) {
                    Entity entity = (Entity)var4.next();
                    this.tickPassenger(p_8664_, entity);
                }
            }
        } else {
            p_8664_.stopRiding();
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
    public void rider_api$tickNonPassengerTimePause(Entity p_8648_) {

        if (GJ.TimerControl.isSlowEntity(p_8648_)) {
            p_8648_.setOldPosAndRot();
            ProfilerFiller profilerfiller = this.getProfiler();
            ++p_8648_.tickCount;
            this.getProfiler().push(() -> {
                return BuiltInRegistries.ENTITY_TYPE.getKey(p_8648_.getType()).toString();
            });
            profilerfiller.incrementCounter("tickNonPassenger");
            if (p_8648_.canUpdate()) {
                p_8648_.tick();
            }

            this.getProfiler().pop();
        }
        Iterator var3 = p_8648_.getPassengers().iterator();

        while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            this.rider_api$tickPassengerTimeSlow(p_8648_, entity);
        }

    }

    @Unique
    private void rider_api$tickPassengerTimePause(Entity p_8663_, Entity p_8664_) {

        if (!GJ.TimerControl.isSlowEntity(p_8664_)) return;

        if (!p_8664_.isRemoved() && p_8664_.getVehicle() == p_8663_) {
            if (p_8664_ instanceof Player || this.entityTickList.contains(p_8664_)) {
                p_8664_.setOldPosAndRot();
                ++p_8664_.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() -> {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(p_8664_.getType()).toString();
                });
                profilerfiller.incrementCounter("tickPassenger");
                if (p_8664_.canUpdate()) {
                    p_8664_.rideTick();
                }

                profilerfiller.pop();
                Iterator var4 = p_8664_.getPassengers().iterator();

                while(var4.hasNext()) {
                    Entity entity = (Entity)var4.next();
                    this.tickPassenger(p_8664_, entity);
                }
            }
        } else {
            p_8664_.stopRiding();
        }

    }

}
