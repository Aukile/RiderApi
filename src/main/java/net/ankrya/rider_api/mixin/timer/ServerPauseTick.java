package net.ankrya.rider_api.mixin.timer;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ServerPauseTick {
    @Mixin({EndDragonFight.class})
    public static abstract class EndDragonFightMixin {
        @Shadow
        @Final
        private ServerLevel level;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        private void freezeTick(CallbackInfo ci) {
            if (level != null) {
                int time_state = Variables.getVariable(level, ModVariable.TIME_STATUS);
                if (time_state == 2) {
                    ci.cancel();
                }
            }
        }
    }

    @Mixin({Raids.class})
    public static abstract class RaidsMixin{
        @Final
        @Shadow private  ServerLevel level;

        @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
        public void tick(CallbackInfo ci){
            if (level != null) {
                int time_state = Variables.getVariable(level, ModVariable.TIME_STATUS);
                if (time_state == 2) {
                    ci.cancel();
                }
            }
        }
    }

    @Mixin({Level.class})
    public static abstract class LevelMixin {
        @Inject(method = {"tickBlockEntities"}, at = {@At("HEAD")}, cancellable = true)
        private void freezeTick(CallbackInfo ci) {
            Level level = (Level)(Object) this;
            if (level != null) {
                int time_state = Variables.getVariable(level, ModVariable.TIME_STATUS);
                if (time_state == 2) {
                    ci.cancel();
                }
            }
        }
    }

    @Mixin({LevelChunk.class})
    public static abstract class LevelChunkMixin {

        @Shadow
        @Final Level level;

        @Inject(method = {"isTicking"}, at = {@At("HEAD")}, cancellable = true)
        private void isTicking(CallbackInfoReturnable<Boolean> cir) {
            if (this.level != null) {
                int time_state = Variables.getVariable(level, ModVariable.TIME_STATUS);
                if (time_state == 2) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
