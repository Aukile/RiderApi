package net.ankrya.rider_api.mixin.timer;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> {


    public MinecraftServerMixin(String p_18765_) {
        super(p_18765_);
    }

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();



    @Shadow private ProfilerFiller profiler;

    @Shadow public abstract ServerFunctionManager getFunctions();

    @Shadow(remap = false) protected abstract ServerLevel[] getWorldArray();

    @Shadow private int tickCount;

    @Shadow private PlayerList playerList;

    @Shadow @Nullable public abstract ServerConnectionListener getConnection();

    @Shadow @Final private List<Runnable> tickables;

    @Shadow protected abstract boolean initServer() throws IOException;

    @Shadow protected long nextTickTime;

    @Shadow @Final private ServerStatus status;

    @Shadow @Nullable private String motd;

    @Shadow private volatile boolean running;

    @Shadow private long lastOverloadWarning;

    @Shadow @Final private static Logger LOGGER;

    @Shadow private boolean debugCommandProfilerDelayStart;


    @Shadow public abstract boolean saveEverything(boolean p_195515_, boolean p_195516_, boolean p_195517_);

    @Unique
    protected long rider_api$nextTickTime;
    @Unique
    protected long rider_api$lastOverloadWarning;

    @Unique
    private  final  static long rider_api$50L = 25L;

    @Unique
    private long rider_api$delayedTasksMaxNextTickTime;

    @Unique
    private boolean rider_api$mayHaveDelayedTasks;

    @Unique
    protected double flag_tick = 0;

    @ModifyConstant(method = {"runServer"}, constant = {@Constant(longValue = 50L)})
    private long modifyTickRate(long original) {

        return original;

    }

    // 在 runServer 方法的 while 循环里 tickServer 调用前插入
//    @Inject(method = "runServer", at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/network/protocol/status/ServerStatus;setDescription(Lnet/minecraft/network/chat/Component;)V",
//            shift = At.Shift.AFTER
//    ), cancellable = true)
//    private void initTick(CallbackInfo ci) {
//        this.rider_api$nextTickTime = Util.getMillis();
//    }
//
//    // 在 runServer 方法的 while 循环里 tickServer 调用前插入
//    @Inject(method = "runServer", at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V",
//            shift = At.Shift.BEFORE
//    ), cancellable = true)
//    private void beforeTick(CallbackInfo ci) {
//
//        if (true) {
//            rider_api$tickCount++;
//            long i_rider_api = Util.getMillis() - this.rider_api$nextTickTime;
//            if (i_rider_api > 2000L && this.rider_api$nextTickTime - this.lastOverloadWarning >= 15000L) {
//                long j_rider_api = i_rider_api / rider_api$50L;
//
//                this.rider_api$nextTickTime += j_rider_api * rider_api$50L;
//                this.rider_api$lastOverloadWarning = this.rider_api$nextTickTime;
//            }
//
//            this.rider_api$nextTickTime += rider_api$50L;
//
//            ServerLevel[] var2 = this.getWorldArray();
//            int var3 = var2.length;
//
//            for(int var4 = 0; var4 < var3; ++var4) {
//                ServerLevel serverlevel = var2[var4];
//                long tickStart = Util.getNanos();
//                if(Variables.getVariable(serverlevel, ModVariable.TIME_STATUS) == 1)
//                {
//
//                        ( (TimerServerLevel) serverlevel ).rider_api$specialEntityTickStart(this::rider_api$haveTime);
//
//                }
//
//            }
//
//        }
//    }
//
//    @Inject(method = "runServer", at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V",
//            shift = At.Shift.AFTER
//    ), cancellable = true)
//    private void afterTick(CallbackInfo ci) {
//
//        if (true) {
//            this.rider_api$mayHaveDelayedTasks = true;
//
//            this.rider_api$delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + rider_api$50L, this.rider_api$nextTickTime);
//        }
//    }
//
//    @Unique
//    private boolean rider_api$haveTime() {
//        return this.runningTask() || Util.getMillis() < (this.rider_api$mayHaveDelayedTasks ? this.rider_api$delayedTasksMaxNextTickTime : this.rider_api$nextTickTime);
//    }

//    @Inject(method = "waitUntilNextTick",at = @At("HEAD"),cancellable = true)
//    protected void waitUntilNextTick(CallbackInfo ci) {
//        this.runAllTasks();
//        this.managedBlock(() -> {
//            return !rider_api$haveTime();
//        });
//        ci.cancel();
//    }

//    @Inject(
//            method = "pollTask()Z",
//            at = @At("RETURN"),
//            locals = LocalCapture.CAPTURE_FAILHARD
//    )
//    private void onPollTaskReturn(CallbackInfoReturnable<Boolean> cir, boolean flag) {
//        this.rider_api$mayHaveDelayedTasks = flag;
//    }


//    @Unique
//    private double rider_api$tickCount;

//    @Inject(method = "tickServer",at = @At("HEAD"),cancellable = true)
//    private void tickServerM(CallbackInfo ci) {
//        if (this.rider_api$tickCount % 2 ==0) ci.cancel();
//    }
}
