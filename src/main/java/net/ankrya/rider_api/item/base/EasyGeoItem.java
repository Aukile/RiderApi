package net.ankrya.rider_api.item.base;

import net.minecraft.resources.ResourceLocation;

public class EasyGeoItem extends BaseGeoItem {
    ResourceLocation model;
    ResourceLocation animation;
    ResourceLocation texture;
    public EasyGeoItem(Properties properties, ResourceLocation model, ResourceLocation animation, ResourceLocation texture) {
        super(properties);
        this.model = model;
        this.animation = animation;
        this.texture = texture;
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
