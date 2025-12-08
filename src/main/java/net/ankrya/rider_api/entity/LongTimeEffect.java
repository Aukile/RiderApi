package net.ankrya.rider_api.entity;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class LongTimeEffect extends SpecialEffectEntity {
    public static final String LONG = "long_time_special_effect";

    private Runnable runnable;
    public LongTimeEffect(EntityType<?> type, Level level) {
        super(type, level);
    }

    public LongTimeEffect(EntityType<?> type, Level level, Player owner, String modid, String model, String texture, int dead) {
        super(type, level, owner, modid, model, texture, dead);
    }

    public LongTimeEffect(Level level, Player owner, String model, String texture, int dead) {
        this(ApiRegister.get().getRegisterObject(LONG, EntityType.class).get(), level, owner, RiderApi.MODID, model, texture, dead);
    }

    public static LongTimeEffect createEffect(Level level, Player player, String namePath, String name){
        clearEffect(player);
        LongTimeEffect effect = new LongTimeEffect(level, player, namePath + name, namePath + name, 99);
        effect.setAutoClear(false);
        if (player != null) effect.setPos(player.position());
        level.addFreshEntity(effect);
        return effect;
    }

    public static List<SpecialEffectEntity> rangeFind(Level level, Vec3 center, int radius) {
        return level.getEntitiesOfClass(SpecialEffectEntity.class, new AABB(center, center)
                        .inflate(radius / 2d), e -> true).stream()
                .sorted(Comparator
                        .comparingDouble(entity ->
                                entity.distanceToSqr(center)))
                .toList();
    }

    public static LongTimeEffect findLongEffect(Player player){
        List<SpecialEffectEntity> effectEntityList = rangeFind(player.level(), player.position(), 4);
        for (SpecialEffectEntity effectEntity : effectEntityList)
            if (effectEntity.getOwner() == player && effectEntity instanceof LongTimeEffect longTimeEffect) return longTimeEffect;
        return null;
    }

    public static SpecialEffectEntity findRiderEffect(Player player){
        List<SpecialEffectEntity> effectEntityList = rangeFind(player.level(), player.position(), 4);
        for (SpecialEffectEntity effectEntity : effectEntityList)
            if (effectEntity.getOwner() == player) return effectEntity;
        return null;
    }

    public static boolean haveEffect(Player player){
        return findRiderEffect(player) != null;
    }

    public static void clearEffect(Player player){
        if (player != null){
            SpecialEffectEntity effectEntity = findRiderEffect(player);
            if (effectEntity != null) effectEntity.discard();
        }
    }

    public void setRunnable(Runnable runnable){
        this.runnable = runnable;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (runnable != null)
            runnable.run();
    }

    public static LongTimeEffect setTime(LongTimeEffect effect, int time){
        effect.setDeadTime(time);
        effect.setAutoClear(true);
        return effect;
    }
}
