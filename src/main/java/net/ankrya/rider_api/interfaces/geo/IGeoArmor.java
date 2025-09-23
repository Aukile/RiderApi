package net.ankrya.rider_api.interfaces.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ankrya.rider_api.item.base.armor.BaseGeoArmor;
import net.ankrya.rider_api.item.renderer.base.BaseGeoArmorRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**是的~就是不想新建一个渲染类*/
public interface IGeoArmor extends IGeoItem {


    /**隐藏块（盔甲）*/
    default Map<String, Boolean> visibilityBones(BaseGeoArmorRenderer<?> renderer) {return new HashMap<>();}

    /**渲染时使用*/
    default <T extends BaseGeoArmor> void withRender(BaseGeoArmorRenderer<T> renderer, PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour){}
    /**做披风物理用的*/
    default void transformations(BaseGeoArmorRenderer<?> renderer){}
    /**会让这个组里面的都发光*/
    default Set<String> lightBones(BaseGeoArmorRenderer<?> renderer){return new HashSet<>();}
}