package net.ankrya.rider_api.effect;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class PlayerEffect implements GeoAnimatable {

    private final UUID ownerUUID;  // 世界坐标模式下为 null
    private final PlayerEffectConfig config;
    private int remainingTicks;
    private boolean finished;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PlayerEffect(@Nullable UUID ownerUUID, PlayerEffectConfig config) {
        this.ownerUUID = ownerUUID;
        this.config = config;
        this.remainingTicks = config.durationTicks();
    }

    // ──────────────────── 生命周期 ────────────────────

    public boolean tickAndCheckDone() {
        if (finished) return true;
        if (config.durationTicks() == PlayerEffectConfig.INFINITE) return false;
        remainingTicks--;
        if (remainingTicks <= 0) {
            finished = true;
            return true;
        }
        return false;
    }

    public void forceRemove() {
        this.finished = true;
    }

    // ──────────────────── 访问器 ────────────────────

    @Nullable
    public UUID getOwnerUUID() { return ownerUUID; }
    public PlayerEffectConfig getConfig() { return config; }
    public String getEffectId() { return config.effectId(); }
    public boolean isFinished() { return finished; }

    /** 是否跟随玩家（包括带偏移的跟随）*/
    public boolean isFollowingPlayer() {
        return config.anchor() instanceof EffectAnchor.FollowPlayer;
    }

    /** 是否固定世界坐标 */
    public boolean isWorldPositioned() {
        return config.anchor() instanceof EffectAnchor.WorldPosition;
    }

    public ResourceLocation getModelLocation() {
        return new ResourceLocation(config.modid(),
                "geo/" + config.model() + ".geo.json");
    }

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(config.modid(),
                "textures/" + config.texture() + ".png");
    }

    public ResourceLocation getAnimationLocation() {
        return new ResourceLocation(config.modid(),
                "animations/" + config.model() + ".animation.json");
    }

    // ──────────────────── GeoAnimatable ────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            Animation.LoopType loop = config.loop()
                    ? Animation.LoopType.LOOP
                    : Animation.LoopType.PLAY_ONCE;
            state.setAnimation(RawAnimation.begin().then(config.animation(), loop));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(@Nullable Object o) {
        int elapsed = config.durationTicks() == PlayerEffectConfig.INFINITE
                ? (int)(System.currentTimeMillis() / 50)
                : config.durationTicks() - remainingTicks;
        return elapsed;
    }
}