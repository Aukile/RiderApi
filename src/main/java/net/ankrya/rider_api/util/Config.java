package net.ankrya.rider_api.util;

import net.ankrya.rider_api.RiderApi;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = RiderApi.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue RECIPE_TXT_OUT = BUILDER.comment(Component.translatable("config.rider_api.recipe_txt_out").getString()).define("recipe_txt_out", false);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean recipeTxtOut;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent configEvent) {
        recipeTxtOut = RECIPE_TXT_OUT.get();
    }
}
