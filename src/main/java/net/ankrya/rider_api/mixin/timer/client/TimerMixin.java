package net.ankrya.rider_api.mixin.timer.client;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public abstract class TimerMixin implements ITimer {

    @Mutable
    @Shadow @Final private float msPerTick;
    @Shadow private float deltaTicks;
    @Unique
    public float rider_api$deltaTicks;
    @Unique
    public float rider_api$tickDelta;
    @Unique
    private long rider_api$lastMs;
    @Mutable
    @Unique
    @Final
    private float rider_api$msPerTick;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modifyMsPerTick(float ticksPerSecond, long time, FloatUnaryOperator targetMsptProvider, CallbackInfo ci) {
        this.rider_api$msPerTick = 50f;
        this.rider_api$lastMs = time;
    }

    @Inject(method = "advanceTime",at = @At("HEAD"),cancellable = true)
    private void advanceTimeMixin(long time, boolean advanceGameTime, CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().level != null){
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null){
                int time_ =    Variables.getVariable(mc.level, ModVariable.TIME_STATUS);
                if (time_ == 1){
                    this.msPerTick = 500f;
                }else if (time_ == 2){
                    cir.setReturnValue((int) deltaTicks);
                }else {
                    this.msPerTick = 50f ;
                }
            }
        }
    }

    @Unique
    @Override
    public int riderApi$advanceTime(long time) {

        this.rider_api$tickDelta = (float)(time - this.rider_api$lastMs) / this.rider_api$msPerTick;
        this.rider_api$lastMs = time;
        this.rider_api$deltaTicks += this.rider_api$tickDelta;
        int $$1 = (int)this.rider_api$deltaTicks;
        this.rider_api$deltaTicks -= (float)$$1;
        return $$1;
    }

    @Unique
    @Override
    public  float riderApi$tickDelta() {
        return rider_api$tickDelta;
    }

    @Unique
    @Override
    public void setRiderApi$deltaTick(float deltaTick) {
        this.rider_api$deltaTicks = deltaTicks;
    }

    @Unique
    @Override
    public float riderApi$deltaTick() {
        return rider_api$deltaTicks;
    }
}
