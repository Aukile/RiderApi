package net.ankrya.rider_api.entity.renderer;

import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.entity.model.SpecialEffectModel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
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

public class SpecialEffectEntityRenderer<T extends SpecialEffectEntity> extends GeoEntityRenderer<T> {
    private final GeoRenderLayer<T> glowLayer;
    protected final GeoModel<T> modelProvider;
    protected Matrix4f dispatchedMat = new Matrix4f();
    protected Matrix4f renderEarlyMat = new Matrix4f();
    protected T animatable;

    public SpecialEffectEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.modelProvider = model;
        this.glowLayer = new AutoGlowingGeoLayer<>(this);
        this.addRenderLayer(this.glowLayer);
    }

    public SpecialEffectEntityRenderer(EntityRendererProvider.Context renderManager){
        this(renderManager, new SpecialEffectModel<>());
    }

    @Override
    public void render(@NotNull T animatable, float yaw, float partialTick, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        BakedGeoModel model = this.modelProvider.getBakedModel(this.modelProvider.getModelResource(animatable, this));
        this.dispatchedMat = poseStack.last().pose();

        poseStack.pushPose();
        LivingEntity owner = animatable.getOwner();
        boolean hasOwner = owner != null;
        float lerpBodyRot = Mth.rotLerp(partialTick, hasOwner ? owner.yRotO : animatable.yRotO, hasOwner ? owner.getYRot() : animatable.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-lerpBodyRot));

        AnimationState<T> predicate = new AnimationState<>(animatable, 0, 0, partialTick, false);

        ResourceLocation textureResource = this.modelProvider.getTextureResource(animatable, this);
        modelProvider.setCustomAnimations(animatable, getInstanceId(animatable), predicate);
        RenderSystem.setShaderTexture(0, textureResource);
        poseStack.translate(0, -0.01f, 0);
        Color renderColor = getRenderColor(animatable, partialTick, packedLight);
        RenderType renderType = getRenderType(animatable, textureResource, bufferSource, partialTick);

        if (renderType != null){
            VertexConsumer buffer = bufferSource.getBuffer(renderType);
            actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, false, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderColor.argbInt());
            getRenderLayers().forEach(layer -> {
                if (animatable.autoGlow() || (!animatable.autoGlow() && layer != this.glowLayer))
                    layer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, getPackedOverlay(animatable, 0.0f));
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

    public static <T extends SpecialEffectEntity> int getPackedOverlay(T entity, float uIn) {
        return OverlayTexture.pack(OverlayTexture.u(uIn), OverlayTexture.v(false));
    }

    @Override
    public long getInstanceId(T animatable) {
        return animatable.getUUID().hashCode();
    }
}
