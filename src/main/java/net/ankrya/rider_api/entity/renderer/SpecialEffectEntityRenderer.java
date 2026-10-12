package net.ankrya.rider_api.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.entity.model.SpecialEffectModel;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GeckoLib 特效实体的渲染器<br>
 * <br>
 * 跟随主人：渲染位置使用主人本帧的插值位置（{@link SpecialEffectEntity#getRenderPosition}），
 * 朝向使用主人身体朝向的插值（{@link SpecialEffectEntity#getRenderYaw}），和玩家渲染器一致，
 * 不再依赖实体本身的位置同步，所以不会抖动，也不需要乘骑玩家。<br>
 * <br>
 * 资源检查：模型未设置/模型文件缺失/动画文件缺失时不再让游戏崩溃，而是跳过渲染并输出错误日志
 * （开发环境下还会在聊天栏提示一次）；贴图缺失时给出警告。
 */
public class SpecialEffectEntityRenderer<T extends SpecialEffectEntity> extends GeoEntityRenderer<T> {
    /** 已经提示过的资源问题，每个只提示一次，避免刷屏 */
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private final GeoRenderLayer<T> glowLayer;
    protected final GeoModel<T> modelProvider;
    protected Matrix4f dispatchedMat = new Matrix4f();
    protected Matrix4f renderEarlyMat = new Matrix4f();
    protected T animatable;

    public SpecialEffectEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.modelProvider = model;
        // 只有实体开启 autoGlow 时才渲染发光层（原来是在自定义的 defaultRender 里手动跳过的）
        this.glowLayer = new AutoGlowingGeoLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                if (animatable.autoGlow())
                    super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            }
        };
        this.addRenderLayer(this.glowLayer);
    }

    public SpecialEffectEntityRenderer(EntityRendererProvider.Context renderManager){
        this(renderManager, new SpecialEffectModel<>());
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!checkResources(entity)) return;

        // 记录实体原点的矩阵（骨骼世界坐标计算用）
        this.dispatchedMat = new Matrix4f(poseStack.last().pose());

        poseStack.pushPose();
        // EntityRenderDispatcher 已经把 poseStack 移到了"实体自己的插值位置"，
        // 这里再补上"主人插值位置 - 实体插值位置"的差值，让特效严格贴在主人身上
        Vec3 target = entity.getRenderPosition(partialTick);
        poseStack.translate(target.x - net.minecraft.util.Mth.lerp(partialTick, entity.xOld, entity.getX()),
                target.y - net.minecraft.util.Mth.lerp(partialTick, entity.yOld, entity.getY()),
                target.z - net.minecraft.util.Mth.lerp(partialTick, entity.zOld, entity.getZ()));
        // 原来的实现会把 GeckoLib 自带的 0.01 抬高抵消掉，这里保持一致
        poseStack.translate(0, -0.01f, 0);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    /**
     * 保留这个方法（子类可能重写或调用过），现在直接走 GeckoLib 的标准渲染流程。<br>
     * 原来自定义的流程跳过了 preRender，导致 {@link SpecialEffectEntity#boneByRenderType} 从来不生效、
     * withScale 缩放无效，并且骨骼开启矩阵追踪时会空指针
     */
    @Override
    public void defaultRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }

    /**
     * 朝向：GeckoLib 对非生物实体传入的 rotationYaw 固定是 0，这里换成主人身体朝向。
     * 最终旋转为 180 - yaw，和原来的实现（以及玩家渲染器）是同一个约定，已有模型不需要改朝向
     */
    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, animatable.getRenderYaw(partialTick), partialTick);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.isTrackingMatrices() && this.animatable != null) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, this.dispatchedMat);

            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.renderEarlyMat));
            localMatrix.translate(getRenderOffset(this.animatable, 1).toVector3f());
            bone.setLocalSpaceMatrix(localMatrix);

            Matrix4f worldState = new Matrix4f(localMatrix);
            worldState.translate(this.animatable.position().toVector3f());
            bone.setWorldSpaceMatrix(worldState);
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.renderEarlyMat = new Matrix4f(poseStack.last().pose());
        this.animatable = animatable;
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public static <T extends SpecialEffectEntity> int getPackedOverlay(T entity, float uIn) {
        return OverlayTexture.pack(OverlayTexture.u(uIn), OverlayTexture.v(false));
    }

    @Override
    public long getInstanceId(T animatable) {
        return animatable.getUUID().hashCode();
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        RenderType renderType = animatable.getRenderType(texture, bufferSource, partialTick);
        if (renderType != null) return renderType;
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (animatable == null) {
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            return;
        }
        String name = bone.getName();
        Map<String, RenderType> map = animatable.boneByRenderType(this);
        if (map.containsKey(name)) {
            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer newBuffer = source.getBuffer(map.get(name));
            super.renderCubesOfBone(poseStack, bone, newBuffer, packedLight, packedOverlay, red, green, blue, alpha);
        } else super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    // ==================================================================
    // 资源检查
    // ==================================================================

    /**
     * 检查模型/动画/贴图是否存在。
     * 模型或动画文件缺失时 GeckoLib 会直接抛异常让游戏崩溃，所以缺失时跳过渲染，并提示一次
     * @return 是否可以渲染
     */
    protected boolean checkResources(T entity) {
        GeoModel<T> geoModel = getGeoModel();
        String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();

        if (geoModel instanceof SpecialEffectModel<?> && (isUnset(entity.model()))) {
            report("unset_model:" + entityName + ":" + entity.modid(),
                    "特效实体 " + entityName + " 没有设置模型（model 为空），已跳过渲染。请在创建时传入 model 参数",
                    "SpecialEffectEntity {} has no model defined (model = \"{}\"), skipped rendering. Pass a model path when creating it.",
                    entityName, entity.model());
            return false;
        }

        ResourceLocation modelRes;
        try {
            modelRes = geoModel.getModelResource(entity, this);
        } catch (Exception e) {
            report("bad_model:" + entityName + ":" + entity.modid() + ":" + entity.model(),
                    "特效实体 " + entityName + " 的模型路径无效：" + entity.modid() + ":" + entity.model(),
                    "SpecialEffectEntity {} has an invalid model path {}:{} ({})", entityName, entity.modid(), entity.model(), e.toString());
            return false;
        }
        if (modelRes == null || !GeckoLibCache.getBakedModels().containsKey(modelRes)) {
            report("missing_model:" + modelRes,
                    "特效实体 " + entityName + " 的模型文件缺失：assets/" + (modelRes == null ? "?" : modelRes.getNamespace() + "/" + modelRes.getPath()) + "，已跳过渲染",
                    "SpecialEffectEntity {} model file not found: {} (expected at assets/<namespace>/geo/<model>.geo.json), skipped rendering.",
                    entityName, modelRes);
            return false;
        }

        ResourceLocation animRes = geoModel.getAnimationResource(entity);
        if (animRes != null && !GeckoLibCache.getBakedAnimations().containsKey(animRes)) {
            boolean fallbackFound = false;
            for (ResourceLocation fallback : geoModel.getAnimationResourceFallbacks(entity))
                if (GeckoLibCache.getBakedAnimations().containsKey(fallback)) { fallbackFound = true; break; }
            if (!fallbackFound) {
                report("missing_anim:" + animRes,
                        "特效实体 " + entityName + " 的动画文件缺失：assets/" + animRes.getNamespace() + "/" + animRes.getPath() + "，已跳过渲染",
                        "SpecialEffectEntity {} animation file not found: {} (expected at assets/<namespace>/animations/<model>.animation.json), skipped rendering.",
                        entityName, animRes);
                return false;
            }
        }

        // 贴图缺失不会崩溃（显示紫黑格子），只警告
        ResourceLocation texRes;
        try {
            texRes = geoModel.getTextureResource(entity, this);
        } catch (Exception e) {
            report("bad_tex:" + entityName + ":" + entity.modid() + ":" + entity.texture(),
                    "特效实体 " + entityName + " 的贴图路径无效：" + entity.modid() + ":" + entity.texture(),
                    "SpecialEffectEntity {} has an invalid texture path {}:{} ({})", entityName, entity.modid(), entity.texture(), e.toString());
            return false;
        }
        if (texRes != null && Minecraft.getInstance().getResourceManager().getResource(texRes).isEmpty()) {
            report("missing_tex:" + texRes,
                    "特效实体 " + entityName + " 的贴图缺失：assets/" + texRes.getNamespace() + "/" + texRes.getPath(),
                    "SpecialEffectEntity {} texture not found: {}", entityName, texRes);
        }
        return true;
    }

    private static boolean isUnset(String value) {
        return value == null || value.isBlank() || "null".equals(value);
    }

    /** 每个问题只提示一次：日志一定输出，开发环境下额外在聊天栏提示 */
    protected static void report(String key, String chatMessage, String logFormat, Object... args) {
        if (!REPORTED.add(key)) return;
        RiderApi.LOGGER.error("[RiderApi] " + logFormat, args);
        if (GJ.Easy.isDevEnvironment()) {
            var player = Minecraft.getInstance().player;
            if (player != null)
                player.displayClientMessage(Component.literal("[RiderApi] " + chatMessage).withStyle(ChatFormatting.RED), false);
        }
    }

    /** 资源包重载后可以重新提示 */
    public static void clearReported() {
        REPORTED.clear();
    }
}
