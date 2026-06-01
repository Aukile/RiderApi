package net.ankrya.rider_api.item.base.armor;

import net.ankrya.rider_api.interfaces.geo.IGeoArmor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseGeoArmor extends ArmorItem implements IGeoArmor {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BaseGeoArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
