package net.ankrya.rider_api.item.base;

import net.minecraft.resources.ResourceLocation;

public class EasyGeoItem extends BaseGeoItem {
    ResourceLocation model;
    ResourceLocation texture;
    ResourceLocation animation;
    public EasyGeoItem(Properties properties, ResourceLocation model, ResourceLocation animation, ResourceLocation texture) {
        super(properties);
        this.model = model;
        this.texture = texture;
        this.animation = animation;
    }

    @Override
    public ResourceLocation getModel() {
        return model;
    }

    @Override
    public ResourceLocation getAnimationFile() {
        return animation;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }
}
