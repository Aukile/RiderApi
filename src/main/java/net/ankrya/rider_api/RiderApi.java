package net.ankrya.rider_api;

import com.mojang.logging.LogUtils;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.init.ApiRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RiderApi.MODID)
public class RiderApi {
    public static final String MODID = "rider_api";
    public static final Logger LOGGER = LogUtils.getLogger();
    public RiderApi(IEventBus bus, ModContainer container) {
        ApiRegister.init(bus);
        Variables.ATTACHMENT_TYPES.register(bus);
    }
}
