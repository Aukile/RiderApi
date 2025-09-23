package net.ankrya.rider_api.entity.model;

import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpecialEffectModel<T extends SpecialEffectEntity> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(SpecialEffectEntity entity) {
        return entity.model();
    }

    @Override
    public ResourceLocation getTextureResource(SpecialEffectEntity entity) {
        return entity.texture();
    }

    @Override
    public ResourceLocation getAnimationResource(SpecialEffectEntity entity) {
        return entity.animationFile();
    }
}
