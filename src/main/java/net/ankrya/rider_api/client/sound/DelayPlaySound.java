package net.ankrya.rider_api.client.sound;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.LoopSoundMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
        if (sounds.containsKey(player))
            sounds.get(player).put(sound.getSound(), new DelaySound(player, sound, delay));
        else {
            sounds.put(player, new HashMap<>());
            add(player, sound, delay);
        }
    }

    public static void cancel(Player player,ResourceLocation location){
        if (sounds.containsKey(player)){
            sounds.get(player).remove(location);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            sounds.forEach((player, delaySounds) -> {
                for (DelaySound delaySound : delaySounds.values()){
                    if (delaySound.delay > 0) {
                        delaySound.tick();
                    } else {
                        MessageLoader.getApiLoader().sendToPlayersNearbyAndSelf(delaySound.sound, delaySound.player);
                        DelayPlaySound.cancel(player, delaySound.sound.getSound());
                    }
                }
            });
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
