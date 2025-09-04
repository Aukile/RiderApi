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
import net.ankrya.rider_api.help.GJ;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(modid = RiderApi.MODID, value = Dist.CLIENT)
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
        event.register(GJ.Easy.getApiResource("cosmic"), CosmicModelLoader.INSTANCE);
    }
}
