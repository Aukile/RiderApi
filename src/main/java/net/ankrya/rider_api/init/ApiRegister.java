package net.ankrya.rider_api.init;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.entity.ArrowSource;
import net.ankrya.rider_api.entity.LongTimeEffect;
import net.ankrya.rider_api.entity.SpecialArrow;
import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.item.LogoItem;
import net.ankrya.rider_api.item.RenderTest;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.function.Supplier;

public class ApiRegister extends ClassRegister{
    private static ApiRegister register;
    private ApiRegister(){}

    public static ApiRegister get(){
        if (register == null)
            register = new ApiRegister();
        return register;
    }

    @Override
    protected String modid() {
        return RiderApi.MODID;
    }

    public static void init(IEventBus bus){
        ApiRegister apiRegister = ApiRegister.get();
        Class<?> particleTypeClass = ParticleType.class;
        apiRegister.registerSource(particleTypeClass);
        apiRegister.register(particleTypeClass, "case_spread", () -> new SimpleParticleType(false));
        apiRegister.register(particleTypeClass, "advanced_particle", AdvancedParticleData::createParticleType);
        apiRegister.register(particleTypeClass, "ribbon_particle", RibbonParticleData::createRibbonParticleType);

        Class<?> typeClass = DataComponentType.class;
        apiRegister.registerSource(typeClass, "data_component_type");
        apiRegister.register(typeClass, "backup_armor", () -> BaseRiderArmor.BACKUP_ARMOR);

        Class<?> entityTypeClass = EntityType.class;
        apiRegister.registerSource(entityTypeClass);
        apiRegister.register(entityTypeClass, SpecialEffectEntity.NAME, () -> EntityType.Builder.of(SpecialEffectEntity::new, MobCategory.MISC).sized(0.1F, 0.5F).setShouldReceiveVelocityUpdates(true).updateInterval(3).build("special_effects"));
        apiRegister.register(entityTypeClass, LongTimeEffect.LONG, () -> EntityType.Builder.of(LongTimeEffect::new, MobCategory.MISC).sized(0.1F, 0.5F).setShouldReceiveVelocityUpdates(true).updateInterval(3).build("long_time_special_effect"));
        apiRegister.register(entityTypeClass, ArrowSource.NAME, () -> EntityType.Builder.<ArrowSource>of(ArrowSource::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5F, 0.5F).build("arrow_source"));
        apiRegister.register(entityTypeClass, SpecialArrow.NAME, () -> EntityType.Builder.<SpecialArrow>of(SpecialArrow::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5F, 0.5F).build("special_arrow"));

        Class<?> itemClass = Item.class;
        apiRegister.registerSource(itemClass);
        apiRegister.register(itemClass, "logo", () -> new LogoItem(new Item.Properties()));
        apiRegister.register(itemClass, "render", RenderTest::new);

        apiRegister.initSource(bus);
    }
}
