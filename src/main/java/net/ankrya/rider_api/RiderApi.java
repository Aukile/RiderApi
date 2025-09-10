package net.ankrya.rider_api;

import com.mojang.logging.LogUtils;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RiderApi.MODID)
public class RiderApi {
    public static final String MODID = "rider_api";
    private static final Logger LOGGER = LogUtils.getLogger();
    public RiderApi() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ApiRegister.init(bus);
    }
}
