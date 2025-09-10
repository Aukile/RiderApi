package net.ankrya.rider_api.mixin.timer.client;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.Timer.class)
public abstract class TimerMixin implements ITimer {

    @Mutable
    @Shadow @Final private float msPerTick;
    @Shadow public float partialTick;
    @Unique
    public float rider_api$partialTick;
    @Unique
    public float rider_api$tickDelta;
    @Unique
    private long rider_api$lastMs;
    @Mutable
    @Unique
    @Final
    private float rider_api$msPerTick;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modifyMsPerTick(float p_92523_, long p_92524_,CallbackInfo ci) {
        this.rider_api$msPerTick = 50f;
        this.rider_api$lastMs = p_92524_;
    }

    @Inject(method = "advanceTime",at = @At("HEAD"),cancellable = true)
    private void advanceTimeMixin(long p_92526_, CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().level != null){
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null){
                int time_ =    Variables.getVariable(mc.level, ModVariable.TIME_STATUS);
                if (time_ == 1){
                    this.msPerTick = 500f;
                }else if (time_ == 2){
                    cir.setReturnValue((int) partialTick);
                }else {
                    this.msPerTick = 50f ;
                }
            }
        }
    }

    @Unique
    @Override
    public int rider_api$advanceTime(long p_92526_) {

        this.rider_api$tickDelta = (float)(p_92526_ - this.rider_api$lastMs) / this.rider_api$msPerTick;
        this.rider_api$lastMs = p_92526_;
        this.rider_api$partialTick += this.rider_api$tickDelta;
        int $$1 = (int)this.rider_api$partialTick;
        this.rider_api$partialTick -= (float)$$1;
        return $$1;
    }

    @Unique
    @Override
    public  float rider_api$partialTick() {
        return rider_api$partialTick;
    }

    @Unique
    @Override
    public  float rider_api$tickDelta() {
        return rider_api$tickDelta;
    }

    @Unique
    @Override
    public void setrider_api$partialTick(float partialTick){
        this.rider_api$partialTick = partialTick;
    }

}
