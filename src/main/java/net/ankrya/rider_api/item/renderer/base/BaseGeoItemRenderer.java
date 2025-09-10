package net.ankrya.rider_api.item.renderer.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.ankrya.rider_api.item.model.base.BaseGeoItemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;

public class BaseGeoItemRenderer<T extends Item & IGeoItem> extends GeoItemRenderer<T> {
    IGeoItem item;

    public BaseGeoItemRenderer() {
        super(new BaseGeoItemModel<>());
    }

    @SuppressWarnings("unchecked")
    public BaseGeoItemRenderer(IGeoItem item) {
        this();
        this.item = item;
        for (GeoRenderLayer<?> layer : item.getRenderLayers(this))
            this.addRenderLayer((GeoRenderLayer<T>) layer);
        if (item.autoGlow()) this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (getGeoItemInterface() != null) {
            Map<String, Boolean> set = getGeoItemInterface().visibilityBones(this);
            if (!set.isEmpty()) set.forEach((boneName, visible) -> this.model.getBone(boneName).ifPresent(bone -> bone.setHidden(visible)));
        }
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable.getRenderType(texture) == null)
            return super.getRenderType(animatable, texture, bufferSource, partialTick);
        return animatable.getRenderType(texture);
    }

    private IGeoItem getGeoItemInterface() {
        return item == null ? this.getAnimatable() : item;
    }
}
