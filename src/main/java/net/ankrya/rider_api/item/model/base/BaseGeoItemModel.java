package net.ankrya.rider_api.item.model.base;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.model.GeoModel;

public class BaseGeoItemModel<T extends Item & IGeoItem> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T item) {
        return item.getModel();
    }

    @Override
    public ResourceLocation getTextureResource(T item) {
        return item.getTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(T item) {
        return item.getAnimationFile();
    }
}
