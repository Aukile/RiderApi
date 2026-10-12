package net.ankrya.rider_api.init;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.mob_effect.SimpleEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class RiderEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, RiderApi.MODID);

    // 禁锢：完全无法移动
    public static final RegistryObject<SimpleEffect> ROOT =
            EFFECTS.register("root",
                    () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x3355AA));

    // 眩晕：玩家无法操作
    public static final RegistryObject<SimpleEffect> STUN =
            EFFECTS.register("stun",
                    () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0xAA33AA));
}
