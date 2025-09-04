package net.ankrya.rider_api.entity.model;

import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpecialEffectModel<T extends SpecialEffectEntity> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(SpecialEffectEntity specialEffectEntity) {
        return GJ.Easy.getApiResource("geo/"+ specialEffectEntity.model()+".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpecialEffectEntity specialEffectEntity) {
        return GJ.Easy.getApiResource("textures/entities/"+ specialEffectEntity.texture()+".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpecialEffectEntity specialEffectEntity) {
        return GJ.Easy.getApiResource("animations/"+ specialEffectEntity.model()+".animation.json");
    }
}
