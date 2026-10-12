package net.ankrya.rider_api.client.sound;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.LoopSoundMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class DelayPlaySound {
    public static final Map<Player, Map<ResourceLocation, DelaySound>> sounds;

    public static void add(Player player, LoopSoundMessage sound, int delay) {
        sounds.computeIfAbsent(player, p -> new HashMap<>()).put(sound.getSound(), new DelaySound(player, sound, delay));
    }

    public static void cancel(Player player,ResourceLocation location){
        if (sounds.containsKey(player)){
            sounds.get(player).remove(location);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 原来在遍历 values() 的同时调用 cancel() 删除元素，声音一触发就会 ConcurrentModificationException 崩服
            // 另外玩家对象永远不会从 Map 里移除，会一直占着内存
            java.util.Iterator<Map.Entry<Player, Map<ResourceLocation, DelaySound>>> players = sounds.entrySet().iterator();
            while (players.hasNext()) {
                Map.Entry<Player, Map<ResourceLocation, DelaySound>> entry = players.next();
                Player player = entry.getKey();
                boolean gone = (player instanceof net.minecraft.server.level.ServerPlayer sp && sp.hasDisconnected())
                        || (player.isRemoved() && player.getRemovalReason() != net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION);
                if (gone) {
                    players.remove();
                    continue;
                }
                java.util.Iterator<DelaySound> it = entry.getValue().values().iterator();
                while (it.hasNext()) {
                    DelaySound delaySound = it.next();
                    if (delaySound.delay > 0) {
                        delaySound.tick();
                    } else {
                        it.remove();
                        MessageLoader.getApiLoader().sendToPlayersNearbyAndSelf(delaySound.sound, delaySound.player);
                    }
                }
                if (entry.getValue().isEmpty()) players.remove();
            }
        }
    }

    static {
        sounds = new HashMap<>();
    }

    public static class DelaySound{
        final Player player;
        final LoopSoundMessage sound;
        int delay;

        public DelaySound(Player player, LoopSoundMessage sound, int delay) {
            this.player = player;
            this.sound = sound;
            this.delay = delay;
        }

        public void tick() {
            this.delay = (this.delay > 0) ? (this.delay - 1) : this.delay;
        }
    }
}
