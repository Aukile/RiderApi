package net.ankrya.rider_api.item;

import net.ankrya.rider_api.client.particle.base.SpreadBase;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.help.runnable.WaitToRun;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 图标物品真的是图标物品吗<br>
 * 这里不是绝佳的测试处<br>
 * go go go
 */
public class LogoItem extends Item {
    public LogoItem(Properties properties) {
        super(properties);
    }

    @Override
    @Deprecated
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        InteractionResultHolder<ItemStack> use = super.use(level, player, usedHand);

//        if (level instanceof ServerLevel serverLevel){
//            GJ.ToParticle.particle(serverLevel, (SimpleParticleType) SpreadBase.CaseSpreadProvider.getCaseSpread(), player.position().add(0, 3, 0), new Vec3(0, 0, 0), 0.5, 1);
//        }
//        new WaitToRun(()-> GJ.AdvancedParticleHelper.addCaseRobbin(level
//                , AdvancedParticleData.getParticleType(), RibbonParticleData.getRibbonParticleType()
//                , player.position().add(0, 3, 0), 2, 4, 15, 15
//                , 40, 0.12f, 1, -3), 10);
        if (GJ.TimerControl.getWorldTime(level) == ITimer.timeNormal)
            GJ.TimerControl.timerStartUp(level, player, ITimer.timeSlow);
        else if (GJ.TimerControl.getWorldTime(level) == ITimer.timeSlow)
            GJ.TimerControl.timerStartUp(level, player, ITimer.timeStop);
        else if (GJ.TimerControl.getWorldTime(level) == ITimer.timeStop)
            GJ.TimerControl.timerStartUp(level, player, ITimer.timeNormal);
        player.getCooldowns().addCooldown(this, 10);
        return use;
    }
}
