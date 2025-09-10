package net.ankrya.rider_api.init;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.client.particle.base.AdvancedParticleBase;
import net.ankrya.rider_api.client.particle.base.ParticleRibbon;
import net.ankrya.rider_api.client.particle.base.SpreadBase;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.client.shaber.ModShaders;
import net.ankrya.rider_api.client.shaber.model.base.CosmicModelLoader;
import net.ankrya.rider_api.compat.animation.PlayerAnimator;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RiderApi.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterClientEvent {

    @SubscribeEvent
    public static void registerPlayerAnimator(final FMLClientSetupEvent event) {
        PlayerAnimator.instance().init();
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(AdvancedParticleData.getParticleType(), AdvancedParticleBase.Factory::new);
        event.registerSpriteSet(RibbonParticleData.getRibbonParticleType(), ParticleRibbon.Factory::new);
        event.registerSpriteSet(ApiRegister.getRegisterObject("case_spread", ParticleType.class).get(), SpreadBase.CaseSpreadProvider::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event) {
        ModShaders.onRegisterShaders(event);
    }

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cosmic", CosmicModelLoader.INSTANCE);
    }
}
