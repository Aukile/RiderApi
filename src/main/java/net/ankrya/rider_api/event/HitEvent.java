package net.ankrya.rider_api.event;

import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

/**普攻冷却*/
@EventBusSubscriber(value = Dist.CLIENT)
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