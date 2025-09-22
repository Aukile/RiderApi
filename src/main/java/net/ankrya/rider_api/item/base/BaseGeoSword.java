package net.ankrya.rider_api.item.base;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 算是作为使用{@link IGeoItem}案例了<br>
 * 因为还有AxeItem什么什么的
 */
public abstract class BaseGeoSword extends SwordItem implements IGeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BaseGeoSword(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    /**
     * @param uses 耐久
     * @param speed 破坏方块速度
     * @param attackDamage 额外伤害值（会加上基础值，基础值一般为3）
     * @param level 破坏方块等级
     * @param enchantmentValue 附魔值
     * @param repairIngredient 维修物品
     * @return 创建Tier
     */
    public static Tier createTier(int uses, float speed, float attackDamage, int level, int enchantmentValue, Ingredient repairIngredient){
        return createTier(uses, speed, attackDamage, level, enchantmentValue, repairIngredient, null);
    }

    public static Tier createTier(int uses, float speed, float attackDamage, int level, int enchantmentValue, Ingredient repairIngredient, TagKey<Block> tag){
        return new Tier() {
            @Override
            public int getUses() {
                return uses;
            }

            @Override
            public float getSpeed() {
                return speed;
            }

            @Override
            public float getAttackDamageBonus() {
                return attackDamage;
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public int getEnchantmentValue() {
                return enchantmentValue;
            }

            @Override
            public @NotNull Ingredient getRepairIngredient() {
                return repairIngredient;
            }

            @Override
            public @Nullable TagKey<Block> getTag() {
                if (tag == null)
                    return Tier.super.getTag();
                return tag;
            }
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
