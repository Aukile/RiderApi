package net.ankrya.rider_api.item.model.base;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.item.base.armor.BaseGeoArmor;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BaseGeoArmorModel<T extends BaseGeoArmor> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(BaseGeoArmor armor) {
        return GJ.Easy.getApiResource("geo/" + armor.getModel() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseGeoArmor armor) {
        return GJ.Easy.getApiResource("textures/item/" + armor.getTexture() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseGeoArmor armor) {
        return GJ.Easy.getApiResource("animations/" + armor.getModel() + ".animation.json");
    }
}
