package net.ankrya.rider_api.effect;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class PlayerEffectManager {

    private static final PlayerEffectManager INSTANCE = new PlayerEffectManager();
    public static PlayerEffectManager get() { return INSTANCE; }

    // 跟随玩家的特效
    private final Map<UUID, List<PlayerEffect>> playerEffects = new HashMap<>();

    // 世界坐标特效（不绑定玩家）
    private final List<PlayerEffect> worldEffects = new ArrayList<>();

    private PlayerEffectManager() {}

    public void addEffect(@Nullable UUID ownerUUID, PlayerEffectConfig config) {
        PlayerEffect effect = new PlayerEffect(ownerUUID, config);

        if (effect.isWorldPositioned()) {
            // 世界坐标特效：直接加入独立列表
            worldEffects.add(effect);
        } else {
            // 跟随玩家特效
            if (ownerUUID == null) {
                throw new IllegalArgumentException("Follow-player effect requires ownerUUID");
            }
            List<PlayerEffect> list = playerEffects.computeIfAbsent(ownerUUID, k -> new ArrayList<>());
            list.removeIf(e -> e.getEffectId().equals(config.effectId()));
            list.add(effect);
        }
    }

    public void removeEffect(@Nullable UUID ownerUUID, @Nullable String effectId) {
        if (ownerUUID != null) {
            List<PlayerEffect> list = playerEffects.get(ownerUUID);
            if (list == null) return;
            if (effectId == null) {
                list.forEach(PlayerEffect::forceRemove);
            } else {
                list.stream()
                        .filter(e -> e.getEffectId().equals(effectId))
                        .forEach(PlayerEffect::forceRemove);
            }
        } else if (effectId != null) {
            // 移除世界坐标特效（通过 effectId）
            worldEffects.stream()
                    .filter(e -> e.getEffectId().equals(effectId))
                    .forEach(PlayerEffect::forceRemove);
        }
    }

    /** 获取某玩家的跟随特效 */
    public List<PlayerEffect> getPlayerEffects(UUID ownerUUID) {
        return Collections.unmodifiableList(
                playerEffects.getOrDefault(ownerUUID, Collections.emptyList()));
    }

    /** 获取所有世界坐标特效 */
    public List<PlayerEffect> getWorldEffects() {
        return Collections.unmodifiableList(worldEffects);
    }

    public void tick() {
        playerEffects.values().forEach(list ->
                list.removeIf(PlayerEffect::tickAndCheckDone));
        playerEffects.entrySet().removeIf(e -> e.getValue().isEmpty());

        worldEffects.removeIf(PlayerEffect::tickAndCheckDone);
    }

    public void clearAll() {
        playerEffects.clear();
        worldEffects.clear();
    }
}