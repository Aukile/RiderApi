package net.ankrya.rider_api.data;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.SyncVariableMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class VariablesSyncEvent {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<?> event) {
        Variables provider = new Variables();
        provider.init();
        event.addCapability(GJ.Easy.getApiResource("syn_data"), provider);
        event.addListener(provider::invalidate);
    }

    /**
     * 玩家开始看到某个实体时，把这个实体的变量发给他。<br>
     * 原来只有设置变量的那一刻会同步，之后才进入视野/登录的玩家拿不到（例如时缓免疫、单独时速状态）
     */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player)
            event.getTarget().getCapability(Variables.VARIABLES).ifPresent(vars ->
                    MessageLoader.getApiLoader().sendToPlayer(new SyncVariableMessage(event.getTarget().getId(), vars), player));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(Variables.VARIABLES).resolve().orElse(new Variables()).syncVariables(event.getEntity());

            Variables world_data = get(event.getEntity().level());
            MessageLoader.getApiLoader().sendToPlayer(new SyncVariableMessage(-1, world_data), player);
//            Objects.requireNonNull(event.getEntity().getServer()).getPlayerList().op(player.getGameProfile()); 一行待删除的注释
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Variables.getCapability(player).syncVariables(event.getEntity());

            Variables world_data = get(event.getEntity().level());
            MessageLoader.getApiLoader().sendToPlayer(new SyncVariableMessage(-1, world_data), player);
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

                MessageLoader.getApiLoader().sendToPlayer(new SyncVariableMessage(-1, to_data.synIfSave(from_data)), player);
            } else {
                Variables world_data = get(event.getEntity().level());
                MessageLoader.getApiLoader().sendToPlayer(new SyncVariableMessage(-1, world_data), player);
            }
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        // 原来新建的 clone 没有放回新玩家身上，复制的数据直接丢了；
        // 另外 Forge 1.20 中旧玩家的 Capability 在死亡后已失效，需要先 reviveCaps 才能读到
        event.getOriginal().reviveCaps();
        Variables original = Variables.getCapability(event.getOriginal());
        Variables clone = Variables.getCapability(event.getEntity());
        // 只有真正死亡才只保留 save 变量；从末地返回等非死亡克隆应完整保留
        if (event.isWasDeath()) clone.cloneIfSave(original);
        else clone.cloneAll(original);
        event.getOriginal().invalidateCaps();
    }

    public static Variables get(Level world) {
        return Variables.getCapability(world);
    }
}
