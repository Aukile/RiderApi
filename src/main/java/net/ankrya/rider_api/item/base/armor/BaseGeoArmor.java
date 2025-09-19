package net.ankrya.rider_api.item.base.armor;

import net.ankrya.rider_api.interfaces.geo.IGeoArmor;
import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseGeoArmor extends ArmorItem implements IGeoArmor {
    public String animation = "idle";
    public RenderType renderType = null;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BaseGeoArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    @Override
    public RenderType getRenderType(ResourceLocation texture) {
        return renderType;
    }

    @Override
    public String getAnimation() {
        return animation;
    }
}
