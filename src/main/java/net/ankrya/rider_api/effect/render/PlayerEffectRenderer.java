package net.ankrya.rider_api.effect.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.ankrya.rider_api.client.render.RenderBase;
import net.ankrya.rider_api.effect.PlayerEffect;
import net.ankrya.rider_api.effect.PlayerEffectConfig;
import net.ankrya.rider_api.effect.model.PlayerEffectModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerEffectRenderer implements GeoRenderer<PlayerEffect> {

    private static PlayerEffectRenderer INSTANCE;
    public static PlayerEffectRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new PlayerEffectRenderer();
        return INSTANCE;
    }

    private final GeoModel<PlayerEffect> model = new PlayerEffectModel();
    private final List<GeoRenderLayer<PlayerEffect>> renderLayers = new ArrayList<>();
    private final GeoRenderLayer<PlayerEffect> glowLayer = new AutoGlowingGeoLayer<>(this);

    private PlayerEffect currentEffect;

    private PlayerEffectRenderer() {
        renderLayers.add(glowLayer);
    }

    // ──────────────────── 核心渲染入口 ────────────────────

    public void render(PlayerEffect effect, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, float partialTick) {
        this.currentEffect = effect;
        PlayerEffectConfig cfg = effect.getConfig();

        poseStack.pushPose();
        poseStack.scale(-1, -1, -1);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        if (cfg.scale() != 1.0f) {
            poseStack.scale(cfg.scale(), cfg.scale(), cfg.scale());
        }

        BakedGeoModel bakedModel = model.getBakedModel(model.getModelResource(effect));
        ResourceLocation textureLocation = model.getTextureResource(effect);

        long instanceId = getInstanceId(effect);
        AnimationState<PlayerEffect> animationState = new AnimationState<>(effect, 0, 0, partialTick, false);
        model.handleAnimations(effect, instanceId, animationState);
        model.setCustomAnimations(effect, instanceId, animationState);

        Color color = Color.WHITE;
        RenderType renderType = getRenderType(effect, textureLocation, bufferSource, partialTick);
        if (renderType != null) {
            if (!firePreRenderEvent(poseStack, bakedModel, bufferSource, partialTick, packedLight)) {
                VertexConsumer buffer = bufferSource.getBuffer(renderType);

                actuallyRender(poseStack, effect, bakedModel, renderType,
                        bufferSource, buffer, false, partialTick,
                        packedLight,
                        OverlayTexture.NO_OVERLAY, color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

                for (GeoRenderLayer<PlayerEffect> layer : renderLayers) {
                    if (!cfg.autoGlow() && layer == glowLayer) continue;
                    layer.render(poseStack, effect, bakedModel, renderType,
                            bufferSource, buffer, partialTick, packedLight,
                            OverlayTexture.NO_OVERLAY);
                }

                firePostRenderEvent(poseStack, bakedModel, bufferSource, partialTick, packedLight);
            }
        }

        poseStack.popPose();
        this.currentEffect = null;
    }

    @Override
    public GeoModel<PlayerEffect> getGeoModel() {
        return model;
    }

    @Override
    public PlayerEffect getAnimatable() {
        return currentEffect;
    }

    @Override
    public List<GeoRenderLayer<PlayerEffect>> getRenderLayers() {
        return renderLayers;
    }

    @Override
    public long getInstanceId(PlayerEffect animatable) {
        UUID uuid = animatable.getOwnerUUID();
        if (uuid == null) {
            return animatable.getEffectId().hashCode();
        }
        return (long) uuid.hashCode() * 31 + animatable.getEffectId().hashCode();
    }

    @Override
    public @Nullable RenderType getRenderType(PlayerEffect animatable, ResourceLocation texture,
                                              @Nullable MultiBufferSource bufferSource,
                                              float partialTick) {
        if (animatable.getConfig().autoGlow())
            return RenderBase.glowing(texture);
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public Color getRenderColor(PlayerEffect animatable, float partialTick, int packedLight) {
        return Color.WHITE;
    }

    // ──────────────────── 事件钩子 ────────────────────

    @Override
    public void fireCompileRenderLayersEvent() {}

    @Override
    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel bakedGeoModel,
                                      MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return false;
    }

    @Override
    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel bakedGeoModel,
                                    MultiBufferSource bufferSource, float partialTick, int packedLight) {}

    @Override
    public void updateAnimatedTextureFrame(PlayerEffect animatable) {
        if (animatable == null) return;
        ResourceLocation texture = model.getTextureResource(animatable);
        AnimatableTexture.setAndUpdate(texture);
    }
}