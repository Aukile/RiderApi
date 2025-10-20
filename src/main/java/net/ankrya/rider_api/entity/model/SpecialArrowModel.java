package net.ankrya.rider_api.entity.model;

import net.ankrya.rider_api.entity.SpecialArrow;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpecialArrowModel<T extends SpecialArrow> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(animatable.getModid(), "geo/"+ animatable.getModel()+".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(animatable.getModid(), "textures/"+ animatable.getTexture()+".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(animatable.getModid(), "animations/"+ animatable.getModel()+".animation.json");
    }
}
