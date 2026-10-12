package net.ankrya.rider_api.quest.event;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.message.common.quest.QuestNetwork;
import net.ankrya.rider_api.quest.client.style.GuiStyleRegistry;
import net.ankrya.rider_api.quest.manager.QuestManager;
import net.ankrya.rider_api.quest.manager.WaveSpawnManager;
import net.ankrya.rider_api.quest.registry.NpcRegistry;
import net.ankrya.rider_api.quest.registry.QuestAttachments;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RiderApi.MODID)
public class QuestEventHandler {

    /** 注册 JSON 重载监听 */
    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent e) {
        e.addListener(QuestRegistry.INSTANCE);
        e.addListener(NpcRegistry.INSTANCE);
        e.addListener(GuiStyleRegistry.INSTANCE);
    }

    /** 服务端启动后同步所有在线玩家（单机/ 重载后恢复） */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent e) {
        e.getServer().getPlayerList().getPlayers()
                .forEach(QuestNetwork::syncToPlayer);
    }

    /** 服务器关闭时清空静态的波次数据 */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent e) {
        WaveSpawnManager.clear();
    }

    // 拾取物品后
    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp)
            QuestManager.refreshCollectObjectives(sp);
    }

    // 合成产出
    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp)
            QuestManager.refreshCollectObjectives(sp);
    }

    // 熔炼取出
    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp)
            QuestManager.refreshCollectObjectives(sp);
    }

    // 容器关闭后（从箱子拿东西）
    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close e) {
        if (e.getEntity() instanceof ServerPlayer sp)
            QuestManager.refreshCollectObjectives(sp);
    }

    // 登录时刷一次（命令给的物品、离线状态变化）
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        // 自动接受 auto_start 任务
        var data = QuestAttachments.get(sp);
        for (var def : QuestRegistry.INSTANCE.all())
            if (def.autoStart() && data.get(def.id()).isEmpty())
                QuestManager.acceptQuest(sp, def.id());
        // 刷新收集进度
        QuestManager.refreshCollectObjectives(sp);
        QuestNetwork.syncToPlayer(sp);
        // 原来写了 onPlayerLogin（重连后恢复波次血条）但从没调用过
        WaveSpawnManager.onPlayerLogin(sp);
    }

    /** 服务端 tick：驱动延迟波次和血条 */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        WaveSpawnManager.tick(e.getServer());
    }

    /** 击杀事件（原有 + 波次追踪） */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.level() instanceof ServerLevel sl)
            WaveSpawnManager.onMobDeath(sl.getServer(), dead);   // 先做波次追踪

        if (!(e.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (dead instanceof Player) return;

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType());
        QuestManager.onKill(player, typeId);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) WaveSpawnManager.onPlayerLogout(sp);
    }
}