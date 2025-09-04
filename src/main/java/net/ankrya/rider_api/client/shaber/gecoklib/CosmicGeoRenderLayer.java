package net.ankrya.rider_api.client.shaber.gecoklib;

import net.ankrya.rider_api.client.shaber.ModShaders;
import net.ankrya.rider_api.client.shaber.model.base.PerspectiveModelState;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**乱写的，用不了*/
public class CosmicGeoRenderLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private final List<ResourceLocation> maskTextures;
    GeoRenderer<T> renderer;

    public CosmicGeoRenderLayer(GeoRenderer<T> renderer, List<ResourceLocation> maskTextures) {
        super(renderer);
        this.renderer = renderer;
        this.maskTextures = maskTextures;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel,
                       @Nullable RenderType renderType, MultiBufferSource bufferSource,
                       @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        LinkedList<BakedQuad> quads = new LinkedList<>();
        if (maskTextures != null && !maskTextures.isEmpty()) {
            List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
            for (ResourceLocation res : maskTextures)
                atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
            atlasSprite.forEach(sprite -> ITEM_MODEL_GENERATOR.processFrames(atlasSprite.indexOf(sprite), "layer" + atlasSprite.indexOf(sprite), sprite.contents()).forEach(element ->{
                for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(), new PerspectiveModelState(ImmutableMap.of()), element.rotation, element.shade));
                }
            }));
        }

        if (renderer instanceof GeoItemRenderer<?> itemRenderer && !itemRenderer.getCurrentItemStack().isEmpty() && buffer != null)
            mc.getItemRenderer().renderQuadList(poseStack, buffer, quads, itemRenderer.getCurrentItemStack(), packedLight, packedOverlay);

        RenderType cosmicRenderType = ModShaders.COSMIC_RENDER_TYPE;
        VertexConsumer cosmicConsumer = bufferSource.getBuffer(cosmicRenderType);

        renderCosmicEffect(poseStack, bakedModel, cosmicConsumer, packedLight);
    }

    private void renderCosmicEffect(PoseStack poseStack, BakedGeoModel model,
                                    VertexConsumer consumer, int packedLight) {

        poseStack.pushPose();

        for (GeoBone bone : model.topLevelBones()) {
            renderBoneCosmicEffect(bone, poseStack, consumer, packedLight);
        }

        poseStack.popPose();
    }

    private void renderBoneCosmicEffect(GeoBone bone, PoseStack poseStack,
                                        VertexConsumer consumer, int packedLight) {

        poseStack.pushPose();

        poseStack.translate(bone.getPosX(), bone.getPosY(), bone.getPosZ());
        poseStack.mulPose(Axis.ZP.rotation(bone.getRotZ()));
        poseStack.mulPose(Axis.YP.rotation(bone.getRotY()));
        poseStack.mulPose(Axis.XP.rotation(bone.getRotX()));
        poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

        for (GeoCube cube : bone.getCubes()) {
            renderCubeWithCosmicEffect(cube, poseStack, consumer, packedLight);
        }

        for (GeoBone childBone : bone.getChildBones()) {
            renderBoneCosmicEffect(childBone, poseStack, consumer, packedLight);
        }

        poseStack.popPose();
    }

    private void renderCubeWithCosmicEffect(GeoCube cube, PoseStack poseStack,
                                            VertexConsumer consumer, int packedLight) {

        for (GeoQuad quad : cube.quads()) {
            renderQuadWithCosmicEffect(quad, poseStack, consumer, packedLight);
        }
    }

    private void renderQuadWithCosmicEffect(GeoQuad quad, PoseStack poseStack,
                                            VertexConsumer consumer, int packedLight) {

        GeoVertex[] vertices = new GeoVertex[]{
                quad.vertices()[0],
                quad.vertices()[1],
                quad.vertices()[2],
                quad.vertices()[3]
        };

        Vector3f normal = calculateNormal(
                vertices[0].position(),
                vertices[1].position(),
                vertices[2].position()
        );

        for (int i = 0; i < 4; i++) {
            GeoVertex vertex = vertices[i];
            Vector3f position = vertex.position();

            consumer.addVertex(poseStack.last(), position.x(), position.y(), position.z())
                    .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                    .setUv(vertex.texU(), vertex.texV())
                    .setUv2(packedLight & '\uffff', packedLight >> 16 & '\uffff')
                    .setNormal(normal.x(), normal.y(), normal.z());
        }
    }

    private Vector3f calculateNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = new Vector3f(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
        Vector3f edge2 = new Vector3f(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());

        Vector3f normal = new Vector3f(
                edge1.y() * edge2.z() - edge1.z() * edge2.y(),
                edge1.z() * edge2.x() - edge1.x() * edge2.z(),
                edge1.x() * edge2.y() - edge1.y() * edge2.x()
        );

        return normal.normalize();
    }
}