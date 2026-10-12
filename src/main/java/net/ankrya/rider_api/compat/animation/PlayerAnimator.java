package net.ankrya.rider_api.compat.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.NMessageCreater;
import net.ankrya.rider_api.message.ex_message.AllPackt;
import net.ankrya.rider_api.message.ex_message.PlayerAnimationMessage;
import net.ankrya.rider_api.message.ex_message.PlayerAnimationStopMessage;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

/**
 * 玩家动画的东东<br>
 * {@link PlayerAnimator#playerAnimation} 是播放的方法 <br>
 * {@link PlayerAnimator#stopAnimation} 是停止的方法 <br>
 * {@link PlayerAnimator#isAnimationPlaying} 是判断动画是否在播放中 <br>
 * <p>
 * {@link PlayerAnimator#ANIMATION} 一般播放动画的层级 <br>
 * {@link PlayerAnimator#RIDE} 播放骑乘动画的层级
 */
public class PlayerAnimator {
    public static final ResourceLocation GEO_ARMOR_BEND_OWNER_KEY =
            new ResourceLocation("player_animator", "geo_armor_bend_owner");

    public final ResourceLocation ANIMATION = GJ.Easy.getApiResource("animation");
    public final ResourceLocation RIDE = GJ.Easy.getApiResource("ride");
    private final String PLAYER_ANIMATION = "playeranimator";
    private boolean INSTALLED = false;
    public static PlayerAnimator instance;

    public static PlayerAnimator instance(){
        if (instance == null)
            instance = new PlayerAnimator();
        return instance;
    }

    public void init() {
        INSTALLED = ModList.get().isLoaded(PLAYER_ANIMATION);
        if (installed()){
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION, 100, player -> new ModifierLayer<>());
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(RIDE, 99, player -> new ModifierLayer<>());
        }
    }

    public static void playerAnimation(Player player, String modid, String animation, boolean showRightArm, boolean showLeftArm, boolean override){
        instance().playerAnimation(player, modid + ":" + animation, showRightArm, showLeftArm, override);
    }

    public void playerAnimation(Player player, String animation, boolean showRightArm, boolean showLeftArm, boolean override){
        playerAnimation(player, ANIMATION, animation, showRightArm, showLeftArm, override);
    }

    public void playerRiderAnimation(Player player, String animation, boolean override){
        playerRiderAnimation(player, animation, true, true, override);
    }

    public void playerRiderAnimation(Player player, String animation, boolean showRightArm, boolean showLeftArm, boolean override){
        playerAnimation(player, RIDE, animation, showRightArm, showLeftArm, override);
    }

    public static void stopAnimation(Player player){
        instance().stopAnimation(player, instance().ANIMATION, 8);
    }

    public void stopRiderAnimation(Player player){
        stopAnimation(player, RIDE, 8);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isAnimationPlaying(Player player){
        if (player instanceof AbstractClientPlayer clientPlayer){
            return isAnimationPlaying(clientPlayer, ANIMATION);
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isRiderAnimationPlaying(Player player){
        if (player instanceof AbstractClientPlayer clientPlayer){
            return isAnimationPlaying(clientPlayer, RIDE);
        }
        return false;
    }

    public void playerAnimation(Player player, ResourceLocation dataId, String animation, boolean showRightArm, boolean showLeftArm, boolean override){
        INMessage animationMessage = new PlayerAnimationMessage(player.getUUID(), dataId, animation, showRightArm, showLeftArm, override);
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel)
            MessageLoader.getApiLoader().sendToPlayersInDimension(new NMessageCreater(animationMessage), serverLevel);
        else if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerAnimationMessage.playerAnimation(clientPlayer, dataId, animation, showRightArm, showLeftArm, override);
            // 原来客户端调用只在本地播放，其他玩家看不到。现在本地先播放，再经服务器转发给周围玩家
            if (player.isLocalPlayer())
                MessageLoader.getApiLoader().sendToServer(new NMessageCreater(new AllPackt(animationMessage)));
        }
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public boolean isAnimationPlaying(AbstractClientPlayer player, ResourceLocation dataId){
        if (installed()){
            var associatedData = PlayerAnimationAccess.getPlayerAssociatedData(player);
            var modifierLayer = (ModifierLayer<IAnimation>) associatedData.get(dataId);
            if (modifierLayer == null) {
                return false;
            }
            IAnimation animation = modifierLayer.getAnimation();
            return animation != null && animation.isActive();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void playAnimation(AbstractClientPlayer player, ResourceLocation dataId, IAnimation keyframeAnimation, boolean override){
        playAnimation(player, dataId, keyframeAnimation, override, false);
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void playAnimation(AbstractClientPlayer player, ResourceLocation dataId,
                              IAnimation keyframeAnimation, boolean override, boolean blender) {
        if (!installed()) return;
        var associatedData = PlayerAnimationAccess.getPlayerAssociatedData(player);
        var modifierLayer = (ModifierLayer<IAnimation>) associatedData.get(dataId);
        if (modifierLayer == null) return;

        IAnimation current = modifierLayer.getAnimation();
        if (current == null || override || !current.isActive()) {
            AbstractFadeModifier fade = AbstractFadeModifier.standardFadeIn(8, Ease.INOUTSINE);
            modifierLayer.replaceAnimationWithFade(fade,
                    new LimbBendAnimation(keyframeAnimation, blender));
            associatedData.set(GEO_ARMOR_BEND_OWNER_KEY, modifierLayer);
        }
    }

    public void stopAnimation(Player player, ResourceLocation dataId, int fadeTime){
        PlayerAnimationStopMessage stopMessage = new PlayerAnimationStopMessage(player.getUUID(), dataId, fadeTime);
        if (player.level() instanceof ServerLevel serverLevel)
            MessageLoader.getApiLoader().sendToPlayersInDimension(new NMessageCreater(stopMessage), serverLevel);
        else if (player instanceof AbstractClientPlayer clientPlayer) {
            // 原来在客户端调用什么都不做
            stopAnimation(clientPlayer, dataId, fadeTime);
            if (player.isLocalPlayer())
                MessageLoader.getApiLoader().sendToServer(new NMessageCreater(new AllPackt(stopMessage)));
        }
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    public void stopAnimation(AbstractClientPlayer player, ResourceLocation dataId, int fadeTime) {
        if (installed()){
            var associatedData = PlayerAnimationAccess.getPlayerAssociatedData(player);
            var modifierLayer = (ModifierLayer<IAnimation>) associatedData.get(dataId);
            if (modifierLayer != null && modifierLayer.isActive()) {
                AbstractFadeModifier fadeModifier = AbstractFadeModifier.standardFadeIn(fadeTime, Ease.INOUTSINE);
                modifierLayer.replaceAnimationWithFade(fadeModifier, null);
            }
        }
    }

    public boolean installed() {
        return INSTALLED;
    }

    private static final java.lang.StackWalker CALLER_WALKER = java.lang.StackWalker.getInstance();

    private static final class LimbBendAnimation implements IAnimation {
        private static final float EPSILON = 0.00001F;
        private final IAnimation delegate;
        private final boolean blender;

        private LimbBendAnimation(IAnimation delegate, boolean blender) {
            this.delegate = delegate;
            this.blender = blender;
        }

        @Override public void tick() { this.delegate.tick(); }
        @Override public boolean isActive() { return this.delegate.isActive(); }

        @Override
        public dev.kosmx.playerAnim.core.util.Vec3f get3DTransform(String modelName,
                                                                   dev.kosmx.playerAnim.api.TransformType type, float tickDelta,
                                                                   dev.kosmx.playerAnim.core.util.Vec3f value0) {
            dev.kosmx.playerAnim.core.util.Vec3f transformed =
                    this.delegate.get3DTransform(modelName, type, tickDelta, value0);

            // BEND 逻辑
            if (type == dev.kosmx.playerAnim.api.TransformType.BEND) {
                String[] lowerPartNames = lowerPartNames(modelName);
                if (lowerPartNames == null)
                    return transformed;

                dev.kosmx.playerAnim.core.util.Vec3f lowerRotation = null;
                for (String lowerPartName : lowerPartNames) {
                    dev.kosmx.playerAnim.core.util.Vec3f candidate = this.delegate.get3DTransform(
                            lowerPartName, dev.kosmx.playerAnim.api.TransformType.ROTATION,
                            tickDelta, dev.kosmx.playerAnim.core.util.Vec3f.ZERO);
                    if (lengthSquared(candidate) > EPSILON * EPSILON) {
                        lowerRotation = candidate;
                        break;
                    }
                }
                if (lowerRotation == null)
                    return transformed;

                dev.kosmx.playerAnim.core.util.Vec3f combined =
                        combineBendAndLowerRotation(transformed, lowerRotation);
                if (("rightArm".equals(modelName) || "leftArm".equals(modelName)) && isHeldItemTransformCall())
                    return new dev.kosmx.playerAnim.core.util.Vec3f(
                            -combined.getX(), combined.getY(), combined.getZ());
                return combined;
            }

            // 物品骨骼坐标系修正
            if (!blender){
                if ("rightItem".equals(modelName))
                    return fixItemTransform(type, transformed, false);
                if ("leftItem".equals(modelName))
                    return fixItemTransform(type, transformed, true);
            }

            return transformed;
        }

        private static String[] lowerPartNames(String modelName) {
            if ("rightArm".equals(modelName))
                return new String[]{"rightForearm", "rightforearm", "right_forearm"};
            if ("leftArm".equals(modelName))
                return new String[]{"leftForearm", "leftforearm", "left_forearm"};
            if ("rightLeg".equals(modelName))
                return new String[]{"rightForeleg", "rightforeleg", "right_foreleg"};
            if ("leftLeg".equals(modelName))
                return new String[]{"leftForeleg", "leftforeleg", "left_foreleg"};
            return null;
        }

        private static dev.kosmx.playerAnim.core.util.Vec3f fixItemTransform(
                dev.kosmx.playerAnim.api.TransformType type,
                dev.kosmx.playerAnim.core.util.Vec3f v,
                boolean isLeft) {

            if (type == dev.kosmx.playerAnim.api.TransformType.ROTATION) {
                return new Vec3f(-v.getX(), -v.getZ(), -v.getY());
            }

            if (type == dev.kosmx.playerAnim.api.TransformType.POSITION) {
                return new Vec3f(-v.getX(), -v.getZ(), -v.getY());
            }

            return new Vec3f(v.getX(), v.getZ(), v.getY());
        }

        @Override
        public void setupAnim(float tickDelta) { this.delegate.setupAnim(tickDelta); }

        @Override
        public dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode getFirstPersonMode(float tickDelta) {
            return this.delegate.getFirstPersonMode(tickDelta);
        }

        @Override
        public dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration getFirstPersonConfiguration(float tickDelta) {
            return this.delegate.getFirstPersonConfiguration(tickDelta);
        }
    }

    private static float lengthSquared(dev.kosmx.playerAnim.core.util.Vec3f vector) {
        return vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ();
    }

    private static boolean isHeldItemTransformCall() {
        return CALLER_WALKER.walk(frames -> frames.anyMatch(frame ->
                "net.minecraft.client.renderer.entity.layers.ItemInHandLayer".equals(frame.getClassName())));
    }

    private static dev.kosmx.playerAnim.core.util.Vec3f combineBendAndLowerRotation(
            dev.kosmx.playerAnim.core.util.Vec3f bend,
            dev.kosmx.playerAnim.core.util.Vec3f lowerRotation) {
        float bendAxis = bend.getX();
        org.joml.Quaternionf total = new org.joml.Quaternionf()
                .rotateAxis(bend.getY(), (float)Math.cos(bendAxis), 0, (float)Math.sin(bendAxis))
                .mul(new org.joml.Quaternionf().rotationZYX(
                        lowerRotation.getZ(), lowerRotation.getY(), lowerRotation.getX()))
                .normalize();

        float twistLength = (float)Math.sqrt(total.y() * total.y() + total.w() * total.w());
        org.joml.Quaternionf twist = twistLength > 0.00001F
                ? new org.joml.Quaternionf(0, total.y() / twistLength, 0, total.w() / twistLength)
                : new org.joml.Quaternionf();
        org.joml.Quaternionf swing = new org.joml.Quaternionf(total)
                .mul(new org.joml.Quaternionf(twist).conjugate())
                .normalize();

        if (swing.w() < 0)
            swing.set(-swing.x(), -swing.y(), -swing.z(), -swing.w());

        float sinHalfAngle = (float)Math.sqrt(swing.x() * swing.x() + swing.z() * swing.z());
        if (sinHalfAngle < 0.00001F)
            return dev.kosmx.playerAnim.core.util.Vec3f.ZERO;

        float axis = (float)Math.atan2(swing.z(), swing.x());
        float angle = 2F * (float)Math.atan2(sinHalfAngle, swing.w());
        return new dev.kosmx.playerAnim.core.util.Vec3f(axis, angle, 0);
    }
}
