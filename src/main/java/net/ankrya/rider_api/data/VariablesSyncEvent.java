package net.ankrya.rider_api.data;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.SyncVariableMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public final class VariablesSyncEvent {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(Variables.VARIABLES).syncVariables(event.getEntity());

            Variables world_data = get(event.getEntity().level());
            MessageLoader.sendToPlayer(new SyncVariableMessage(-1, world_data), player);
            event.getEntity().getServer().getPlayerList().op(player.getGameProfile());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(Variables.VARIABLES).syncVariables(event.getEntity());

            Variables world_data = get(event.getEntity().level());
            MessageLoader.sendToPlayer(new SyncVariableMessage(-1, world_data), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(Variables.VARIABLES).syncVariables(event.getEntity());


            Level from = player.server.getLevel(event.getFrom());
            Level to = player.server.getLevel(event.getTo());
            if (from != null && to != null){
                Variables from_data = get(from);
                Variables to_data = get(to);

                MessageLoader.sendToPlayer(new SyncVariableMessage(-1, to_data.synIfSave(from_data)), player);
            } else {
                Variables world_data = get(event.getEntity().level());
                MessageLoader.sendToPlayer(new SyncVariableMessage(-1, world_data), player);
            }
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        Variables original = event.getOriginal().getData(Variables.VARIABLES);
        Variables clone = new Variables();
        clone.cloneIfSave(original);
        event.getEntity().setData(Variables.VARIABLES, clone);
    }

    public static Variables get(Level world) {
        return world.getData(Variables.VARIABLES);
    }
}
