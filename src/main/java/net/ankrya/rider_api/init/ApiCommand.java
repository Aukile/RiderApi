package net.ankrya.rider_api.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.message.MessageCreater;
import net.ankrya.rider_api.message.MessageLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RiderApi.MODID)
public class ApiCommand {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("riderapi")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("time")
                        .then(Commands.literal("stop")
                                .executes(context -> timeCommend(context, ITimer.timeStop)))
                        .then(Commands.literal("slow")
                                .executes(context -> timeCommend(context, ITimer.timeSlow)))
                        .then(Commands.literal("normal")
                                .executes(context -> timeCommend(context, ITimer.timeNormal))))
                .then(Commands.literal("arrow")
                        .executes(ApiCommand::arrowCommend)
                        .then(Commands.argument("use", BoolArgumentType.bool())
                                .executes(context -> arrowCommend(context, BoolArgumentType.getBool(context, "use"))))
                )
                .then(Commands.literal("test").executes(ApiCommand::testCommend))
        );
    }

    private static int testCommend(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        return 0;
    }

    private static int timeCommend(CommandContext<CommandSourceStack> context, int state) {
        Level world = context.getSource().getUnsidedLevel();
        GJ.TimerControl.timerStartUp(world, (LivingEntity) getEntity(context), state);
        return 0;
    }

    private static int arrowCommend(CommandContext<CommandSourceStack> context){
        return arrowCommend(context, !(boolean)Variables.getVariable(getEntity(context), ModVariable.ARROW_DROP_MODE));
    }

    private static int arrowCommend(CommandContext<CommandSourceStack> context, boolean open) {
        Variables.setVariable(getEntity(context), ModVariable.ARROW_DROP_MODE, open);
        return 0;
    }

    private static Entity getEntity(CommandContext<CommandSourceStack> context) {
        Level world = context.getSource().getUnsidedLevel();
        Entity entity = context.getSource().getEntity();
        if (entity == null && world instanceof ServerLevel serverLevel)
            entity = FakePlayerFactory.getMinecraft(serverLevel);
        return entity;
    }
}
