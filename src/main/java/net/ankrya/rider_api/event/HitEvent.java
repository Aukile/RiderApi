package net.ankrya.rider_api.event;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**普攻冷却*/
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class HitEvent {

    @SubscribeEvent
    public static void onHit(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null){
            if ((int)Variables.getVariable(player, ModVariable.HIT_COOLING) > 0) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }
}