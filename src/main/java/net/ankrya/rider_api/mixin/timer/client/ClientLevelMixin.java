package net.ankrya.rider_api.mixin.timer.client;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ.TimerControl;
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
import net.neoforged.neoforge.event.EventHooks;
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

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final
    EntityTickList tickingEntities;

    protected ClientLevelMixin(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Shadow
    protected abstract void tickPassenger(Entity p104640, Entity entity);

    @Inject(method = {"animateTick", "tick", "tickTime"}, at = {@At("HEAD")}, cancellable = true)
    private void pauseClientTick(CallbackInfo ci) {
        if (this.minecraft != null){
            int time_state = Variables.getVariable(this, ModVariable.TIME_STATUS);
            if (time_state == 2)
                ci.cancel();
        }
    }

    @Inject(method = "tickNonPassenger",at = @At("HEAD"),cancellable = true)
    public void tickNonPassengerM(Entity p_104640_,CallbackInfo ci) {
        if(this.minecraft != null){
            int time_state = Variables.getVariable(this, ModVariable.TIME_STATUS);
            if (time_state == 1 ) {
                boolean speed_down = TimerControl.isSlowEntity(p_104640_);

                if(speed_down) {

                    for (Entity entity : p_104640_.getPassengers()) {
                        this.tickPassenger(p_104640_, entity);
                    }
                    ci.cancel();
                }

            } else if (time_state == 2) {
                boolean speed_down = TimerControl.isPauseEntity(p_104640_);

                if(!speed_down) {

                    for (Entity entity : p_104640_.getPassengers()) {
                        this.tickPassenger(p_104640_, entity);
                    }
                    ci.cancel();
                }
            }
        }
    }


    @Inject(method = "tickPassenger",at = @At("HEAD"),cancellable = true)
    public void tickPassengerM(Entity p_104642_, Entity p_104643_,CallbackInfo ci) {
        if(this.minecraft != null){
            int time_state = Variables.getVariable(this, ModVariable.TIME_STATUS);
            if (time_state == 1 ) {
                boolean speed_down = TimerControl.isSlowEntity(p_104643_);

                if(speed_down) {
                    ci.cancel();
                }
            } else if (time_state ==2) {
                boolean speed_down = TimerControl.isPauseEntity(p_104643_);

                if(!speed_down) {
                    ci.cancel();
                }
            }
        }
    }


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
        if (TimerControl.isSlowEntity(entity1)){
            entity1.setOldPosAndRot();
            ++entity1.tickCount;
            this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
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
    @Override
    public void rider_api$tickPassengerTimeSlow(Entity vehicle, Entity entity1) {

        if (! TimerControl.isSlowEntity(entity1)) return;

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

        if (TimerControl.isPauseEntity(entity1)){
            entity1.setOldPosAndRot();
            ++entity1.tickCount;
            this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity1.getType()).toString());
            if (!EventHooks.fireEntityTickPre(entity1).isCanceled()) {
                entity1.tick();
                EventHooks.fireEntityTickPost(entity1);
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

        if (! TimerControl.isPauseEntity(entity1)) return;

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
