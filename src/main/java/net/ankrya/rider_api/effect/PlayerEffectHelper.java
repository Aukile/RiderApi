package net.ankrya.rider_api.effect;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.AddEffectPacket;
import net.ankrya.rider_api.message.common.RemoveEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 服务端调用接口。
 * 所有 play/stop 方法均可在服务端任意位置直接调用。
 */
public final class PlayerEffectHelper {

    private PlayerEffectHelper() {}

    /**
     * 在指定玩家身上播放特效（对该玩家周围的所有人可见）。
     * 若要只对本人可见，使用 playForSelf。
     */
    public static void play(ServerPlayer player, PlayerEffectConfig config) {
        MessageLoader.getApiLoader().sendToPlayersNearbyAndSelf(new AddEffectPacket(player.getUUID(), config), player);
    }

    /** 特效只对玩家本人可见。 */
    public static void playForSelf(ServerPlayer player, PlayerEffectConfig config) {
        MessageLoader.getApiLoader().sendToPlayer(new AddEffectPacket(player.getUUID(), config), player);
    }

    /** 停止某个特效（effectId=null 停止全部）。 */
    public static void stop(ServerPlayer player, String effectId) {
        MessageLoader.getApiLoader().sendToPlayersNearbyAndSelf(new RemoveEffectPacket(player.getUUID(), effectId), player);
    }

    public static void stopAll(ServerPlayer player) {
        stop(player, null);
    }

    public static void playAtWorld(PlayerEffectConfig config, Level level) {
        if (!(config.anchor() instanceof EffectAnchor.WorldPosition)) {
            throw new IllegalArgumentException("Config must use worldPos anchor");
        }
        if (level instanceof ServerLevel serverLevel)
            MessageLoader.getApiLoader().sendToPlayersInDimension(new AddEffectPacket(null, config), serverLevel);
    }

    /**
     * 在某位置播放特效，但只对指定玩家可见（用于私有标记）。
     */
    public static void playAtWorldFor(ServerPlayer viewer, PlayerEffectConfig config) {
        if (!(config.anchor() instanceof EffectAnchor.WorldPosition)) {
            throw new IllegalArgumentException("Config must use worldPos anchor");
        }
        MessageLoader.getApiLoader().sendToPlayer(new AddEffectPacket(null, config), viewer);
    }

    // ── 快捷构建 Config 的 Builder ──

    public static PlayerEffectConfig.Builder effect(String effectId) {
        return new PlayerEffectConfig.Builder(effectId);
    }
}