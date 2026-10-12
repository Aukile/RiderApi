package net.ankrya.rider_api.effect.model;

import net.ankrya.rider_api.effect.PlayerEffect;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlayerEffectModel extends GeoModel<PlayerEffect> {

    @Override
    public ResourceLocation getModelResource(PlayerEffect effect) {
        return effect.getModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(PlayerEffect effect) {
        return effect.getTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerEffect effect) {
        return effect.getAnimationLocation();
    }
}