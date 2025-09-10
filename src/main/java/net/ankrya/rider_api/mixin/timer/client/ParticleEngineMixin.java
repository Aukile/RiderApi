package net.ankrya.rider_api.mixin.timer.client;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Shadow protected ClientLevel level;

    @Shadow @Final private Queue<Particle> particlesToAdd;

    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;

//    @Inject(method = "tick",at = @At("HEAD"),cancellable = true)
//    public void tick(CallbackInfo ci) {
//        if (this.level != null){
//            int timer_state = (int) Variables.getVariable(this.level, ModVariable.TIME_STATUS);
//            if (timer_state == 2){
//                Particle particle;
//                if (!this.particlesToAdd.isEmpty()) {
//                    while((particle = (Particle)this.particlesToAdd.poll()) != null) {
//                        ((Queue)this.particles.computeIfAbsent(particle.getRenderType(), (p_107347_) -> {
//                            return EvictingQueue.create(16384);
//                        })).add(particle);
//                    }
//                }
//                ci.cancel();
//            }
//        }
//    }

    @Inject(method = {"tickParticleList"}, at = {@At("HEAD")}, cancellable = true)
    private void pauseTickParticle(CallbackInfo ci) {
        if (this.level != null){
            int timer_state = (int) Variables.getVariable(this.level, ModVariable.TIME_STATUS);
            if (timer_state == 2){
                ci.cancel();
            }
        }
    }

    @Inject(method = {"close"}, at = {@At("HEAD")}, cancellable = true)
    public void close(CallbackInfo ci){
        if (this.level != null){
            int timer_state = (int) Variables.getVariable(this.level, ModVariable.TIME_STATUS);
            if (timer_state == 2){
                ci.cancel();
            }
        }
    }
}
