package net.ankrya.rider_api.init;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.entity.SpecialEffectEntity;
import net.ankrya.rider_api.item.LogoItem;
import net.ankrya.rider_api.item.RenderTest;
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
    String modid() {
        return RiderApi.MODID;
    }

    /**是否已注册类型*/
    public static boolean isRegistered(Class<?> clazz){
        return get().registers.containsKey(clazz);
    }

    /**应该用不到*/
    public static Map<Class<?>, DeferredRegister<?>> getRegisters() {
        return get().registers;
    }

    /**获取模组中此类型注册的全部东西*/
    public static Map<String, Supplier<?>> getRegisterObjects(Class<?> clazz) {
        return get().registerObjects.get(clazz);
    }

    /**获取注册的东西*/
    @SuppressWarnings("unchecked")
    public static  <T> Supplier<T> getRegisterObject(String name, Class<T> clazz){
        return (Supplier<T>) getRegisterObjects(clazz).get(name);
    }

    public static void init(IEventBus bus){
        ApiRegister apiRegister = ApiRegister.get();
        Class<?> particleTypeClass = ParticleType.class;
        apiRegister.registerSource(particleTypeClass);
        apiRegister.register(particleTypeClass, "case_spread", () -> new SimpleParticleType(false));
        apiRegister.register(particleTypeClass, "advanced_particle", AdvancedParticleData::createParticleType);
        apiRegister.register(particleTypeClass, "ribbon_particle", RibbonParticleData::createRibbonParticleType);

        apiRegister.onceRegister(EntityType.class, "special_effects", () -> EntityType.Builder.of(SpecialEffectEntity::new, MobCategory.MISC).sized(0.1F, 0.5F).setShouldReceiveVelocityUpdates(true).updateInterval(3).build("special_effects"));

        Class<?> itemClass = Item.class;
        apiRegister.registerSource(itemClass);
        apiRegister.register(itemClass, "logo", () -> new LogoItem(new Item.Properties()));
        apiRegister.register(itemClass, "render", RenderTest::new);

        apiRegister.initSource(bus);
    }
}
