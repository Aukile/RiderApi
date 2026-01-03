package net.ankrya.rider_api.entity.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.ankrya.rider_api.entity.SpecialArrow;
import net.ankrya.rider_api.entity.model.SpecialArrowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.util.RenderUtil;

public class SpecialArrowRenderer<T extends SpecialArrow> extends GeoEntityRenderer<T> {
    private final GeoRenderLayer<T> glowLayer;
    protected final GeoModel<T> modelProvider;
    protected Matrix4f dispatchedMat = new Matrix4f();
    protected Matrix4f renderEarlyMat = new Matrix4f();
    protected T animatable;

    public SpecialArrowRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.modelProvider = model;
        this.glowLayer = new AutoGlowingGeoLayer<>(this);
        this.addRenderLayer(this.glowLayer);
    }

    public SpecialArrowRenderer(EntityRendererProvider.Context renderManager){
        this(renderManager, new SpecialArrowModel<>());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        this.dispatchedMat = poseStack.last().pose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));

        AnimationState<T> predicate = new AnimationState<>(animatable, 0, 0, partialTick, false);

        ResourceLocation textureResource = this.modelProvider.getTextureResource(animatable, this);
        modelProvider.setCustomAnimations(animatable, getInstanceId(animatable), predicate);
        RenderSystem.setShaderTexture(0, textureResource);

        Color renderColor = getRenderColor(animatable, partialTick, packedLight);

        if (renderType != null){
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, false, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderColor.argbInt());
            getRenderLayers().forEach(layer -> {
                if (animatable.autoGlow() || (!animatable.autoGlow() && layer != this.glowLayer))
                    layer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, getPackedOverlay(animatable, 0.0f, partialTick));
            });
        }
        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = poseStack.last().pose();
            Matrix4f localMatrix = RenderUtil.invertAndMultiplyMatrices(poseState, this.dispatchedMat);

            bone.setModelSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, this.renderEarlyMat));
            localMatrix.translate(getRenderOffset(this.animatable, 1).toVector3f());
            bone.setLocalSpaceMatrix(localMatrix);

            Matrix4f worldState = localMatrix;

            worldState.translate(this.animatable.position().toVector3f());
            bone.setWorldSpaceMatrix(worldState);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        this.renderEarlyMat = poseStack.last().pose();
        this.animatable = animatable;
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public int getPackedOverlay(T animatable, float u, float partialTick) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(false));
    }

    @Override
    public long getInstanceId(T animatable) {
        return animatable.getUUID().hashCode();
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);
    }
}
