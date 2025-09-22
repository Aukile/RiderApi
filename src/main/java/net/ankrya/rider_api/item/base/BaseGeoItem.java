package net.ankrya.rider_api.item.base;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 提供了一个只有模型的物品
 * @see EasyGeoItem
 */
public abstract class BaseGeoItem extends Item implements IGeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BaseGeoItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
