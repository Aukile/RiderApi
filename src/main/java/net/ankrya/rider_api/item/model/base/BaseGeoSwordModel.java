package net.ankrya.rider_api.item.model.base;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.item.base.BaseGeoSword;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BaseGeoSwordModel extends GeoModel<BaseGeoSword> {
    @Override
    public ResourceLocation getModelResource(BaseGeoSword item) {
        return item.getModel();
    }

    @Override
    public ResourceLocation getTextureResource(BaseGeoSword item) {
        return item.getTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(BaseGeoSword item) {
        return item.getAnimationFile();
    }
}
