package net.ankrya.rider_api.item.base;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 算是作为使用{@link IGeoItem}案例了<br>
 * 因为还有AxeItem什么什么的
 */
public abstract class BaseGeoSword extends SwordItem implements IGeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BaseGeoSword(Tier tier, Properties properties) {
        super(tier, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
