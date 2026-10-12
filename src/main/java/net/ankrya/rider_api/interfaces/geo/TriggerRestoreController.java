package net.ankrya.rider_api.interfaces.geo;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.state.BoneSnapshot;

import java.util.Map;

/**
 * 可以“恢复”触发动画的 AnimationController。<br>
 * GeckoLib 的 triggerAnim 只是一次性的网络包，客户端重启后控制器是全新的，什么都不记得；
 * 这个控制器配合 {@link GeoItemAnimData#TRIGGERED_ANIMATION}（物品 NBT） 在客户端重新触发上次的动画，
 * 对于 hold_on_last_frame 的动画会直接跳到最后一帧（期间的音效/粒子/自定义关键帧不会重复触发）
 */
public class TriggerRestoreController<T extends GeoAnimatable> extends AnimationController<T> {
    private boolean skipToEnd = false;

    public TriggerRestoreController(T animatable, String name, int transitionTickTime, AnimationStateHandler<T> animationHandler) {
        super(animatable, name, transitionTickTime, animationHandler);
    }

    /**
     * 恢复一个已注册的触发动画
     * @param animName  动画名（必须已用 triggerableAnim 注册）
     * @param jumpToEnd 是否直接跳到最后一帧（用于 hold_on_last_frame 的动画）
     * @return 是否成功
     */
    public boolean restoreTriggered(String animName, boolean jumpToEnd) {
        if (!super.tryTriggerAnimation(animName)) return false;
        this.skipToEnd = jumpToEnd;
        return true;
    }

    /** 是否还在等待跳到最后一帧 */
    public boolean isRestoring() {
        return skipToEnd;
    }

    @Override
    public boolean tryTriggerAnimation(String animName) {
        // 正常触发新的动画时取消恢复
        this.skipToEnd = false;
        return super.tryTriggerAnimation(animName);
    }

    @Override
    protected double adjustTick(double tick) {
        double adjusted = super.adjustTick(tick);
        if (this.skipToEnd && this.animationState == State.RUNNING && this.currentAnimation != null
                && this.triggeredAnimation != null && this.currentRawAnimation == this.triggeredAnimation) {
            double speed = getAnimationSpeed();
            double length = this.currentAnimation.animation().length();
            if (speed > 0 && adjusted < length) {
                // 把起始时间往前挪一整段动画的长度，相当于动画已经播完
                this.tickOffset = tick - (length + 1) / speed;
                adjusted = speed * Math.max(tick - this.tickOffset, 0);
            }
        }
        return adjusted;
    }

    @Override
    public void process(CoreGeoModel<T> model, AnimationState<T> state, Map<String, CoreGeoBone> bones, Map<String, BoneSnapshot> snapshots, double seekTime, boolean crashWhenCantFindBone) {
        boolean mute = this.skipToEnd;
        SoundKeyframeHandler<T> sound = this.soundKeyframeHandler;
        ParticleKeyframeHandler<T> particle = this.particleKeyframeHandler;
        CustomKeyframeHandler<T> custom = this.customKeyframeHandler;
        if (mute) {
            // 跳帧时关键帧会被一次性全部判定为“已执行”，这里换成空处理器，避免音效、粒子一下子全放出来
            if (sound != null) this.soundKeyframeHandler = event -> {};
            if (particle != null) this.particleKeyframeHandler = event -> {};
            if (custom != null) this.customKeyframeHandler = event -> {};
        }
        try {
            super.process(model, state, bones, snapshots, seekTime, crashWhenCantFindBone);
        } finally {
            if (mute) {
                this.soundKeyframeHandler = sound;
                this.particleKeyframeHandler = particle;
                this.customKeyframeHandler = custom;
            }
        }

        // 已经停在最后一帧（hold_on_last_frame 会把状态设为 PAUSED），或者触发动画被清掉了，恢复结束
        // 注意不能用 STOPPED 判断：刚调用 restoreTriggered 的那一帧控制器还是 STOPPED
        if (this.skipToEnd && (this.animationState == State.PAUSED || this.triggeredAnimation == null))
            this.skipToEnd = false;
    }
}
