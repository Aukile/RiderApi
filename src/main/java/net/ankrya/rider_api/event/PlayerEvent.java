package net.ankrya.rider_api.event;

import net.ankrya.rider_api.api.event.ArmorBrokenEvent;
import net.ankrya.rider_api.api.event.RiderArmorRemoveEvent;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 灾厄极狐的普攻~
 */
@Mod.EventBusSubscriber
public class PlayerEvent {
    /**普攻冷却*/
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END){
            int hit = Variables.getVariable(event.player, ModVariable.HIT_COOLING);
            if (hit > 0) Variables.setVariable(event.player, ModVariable.HIT_COOLING, hit - 1);
        }
    }

    @SubscribeEvent
    public static void afterArmorUnequip(RiderArmorRemoveEvent event){
        LivingEntity entity = event.getEntity();
        GJ.ToEntity.fixHealth(entity);
    }

    @SubscribeEvent
    public static void onArmorBroken(ArmorBrokenEvent event) {
        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof BaseRiderArmor armor && event.getEntity() instanceof Player player) {
            ItemStack backupArmor = BaseRiderArmor.getBackupArmor(stack);
            player.setItemSlot(armor.getSlot(), backupArmor);
        }
    }

    @SubscribeEvent
    public static void onPlayerDead(LivingDeathEvent event){
        LivingEntity entity = event.getEntity();
        if ((int) Variables.getVariable(entity, ModVariable.TIME_STATUS) != 0 && hasTimerUser(entity)){
            GJ.TimerControl.timerStartUp(entity.level(), 0);
        }
    }

    private static boolean hasTimerUser(Entity entity){
        boolean has = false;
        List<LivingEntity> entities = GJ.ToWorld.rangeFind(entity.level(), entity.position(), 10);
        for (LivingEntity livingEntity : entities) {
            if (GJ.TimerControl.isSlowEntity(livingEntity)) has = true;
            if (GJ.TimerControl.isPauseEntity(livingEntity)) has = true;
        }
        return has;
    }
}
