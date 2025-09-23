package net.ankrya.rider_api.entity.model;

import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpecialEffectModel<T extends SpecialEffectEntity> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(SpecialEffectEntity specialEffectEntity) {
        return ResourceLocation.fromNamespaceAndPath(specialEffectEntity.modid(), "geo/"+ specialEffectEntity.model()+".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpecialEffectEntity specialEffectEntity) {
        return ResourceLocation.fromNamespaceAndPath(specialEffectEntity.modid(), "textures/entities/"+ specialEffectEntity.texture()+".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpecialEffectEntity specialEffectEntity) {
        return ResourceLocation.fromNamespaceAndPath(specialEffectEntity.modid(), "animations/"+ specialEffectEntity.model()+".animation.json");
    }
}
