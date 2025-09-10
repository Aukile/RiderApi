package net.ankrya.rider_api.data;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.SyncVariableMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class VariablesSyncEvent {

    @SubscribeEvent
    public static void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Variables.class);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(Variables.VARIABLES).resolve().orElse(new Variables()).syncVariables(event.getEntity());

            Variables world_data = get(event.getEntity().level());
            MessageLoader.getLoader().sendToPlayer(new SyncVariableMessage(-1, world_data), player);
            event.getEntity().getServer().getPlayerList().op(player.getGameProfile());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Variables.getCapability(player).syncVariables(event.getEntity());

            Variables world_data = get(event.getEntity().level());
            MessageLoader.getLoader().sendToPlayer(new SyncVariableMessage(-1, world_data), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Variables.getCapability(player).syncVariables(event.getEntity());


            Level from = player.server.getLevel(event.getFrom());
            Level to = player.server.getLevel(event.getTo());
            if (from != null && to != null){
                Variables from_data = get(from);
                Variables to_data = get(to);

                MessageLoader.getLoader().sendToPlayer(new SyncVariableMessage(-1, to_data.synIfSave(from_data)), player);
            } else {
                Variables world_data = get(event.getEntity().level());
                MessageLoader.getLoader().sendToPlayer(new SyncVariableMessage(-1, world_data), player);
            }
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        Variables original = Variables.getCapability(event.getOriginal());
        Variables clone = new Variables();
        clone.cloneIfSave(original);
        Variables.getCapability(event.getEntity());
    }

    public static Variables get(Level world) {
        return Variables.getCapability(world);
    }
}
