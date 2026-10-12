package net.ankrya.rider_api.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.compat.animation.PlayerAnimator;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.message.MessageCreater;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.quest.QuestNetwork;
import net.ankrya.rider_api.message.ex_message.OpenAdvancementEditorScreen;
import net.ankrya.rider_api.message.ex_message.OpenQuestEditorScreen;
import net.ankrya.rider_api.message.ex_message.OpenStyleEditorScreen;
import net.ankrya.rider_api.quest.data.QuestState;
import net.ankrya.rider_api.quest.manager.QuestManager;
import net.ankrya.rider_api.quest.registry.QuestAttachments;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
                .then(Commands.literal("timer")
                        .then(Commands.argument("user", EntityArgument.entity())
                                .then(Commands.literal("stop")
                                        .executes(context -> timeUserCommend(context, ITimer.timeStop)))
                                .then(Commands.literal("slow")
                                        .executes(context -> timeUserCommend(context, ITimer.timeSlow)))
                                .then(Commands.literal("normal")
                                        .executes(context -> timeUserCommend(context, ITimer.timeNormal)))))
                .then(Commands.literal("arrow")
                        .executes(ApiCommand::arrowCommend)
                        .then(Commands.argument("use", BoolArgumentType.bool())
                                .executes(context -> arrowCommend(context, BoolArgumentType.getBool(context, "use")))))
                .then(Commands.literal("armorLock")
                        .then(Commands.argument("open", BoolArgumentType.bool())
                        .executes(ApiCommand::armorLockCommend)))
                .then(Commands.literal("test").executes(ApiCommand::testCommend))
                .then(Commands.literal("animation")
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("showRightHand", BoolArgumentType.bool())
                                                .then(Commands.argument("showLeftHand", BoolArgumentType.bool())
                                                        .executes(context -> animTest(context, StringArgumentType.getString(context, "modid"), StringArgumentType.getString(context, "name"), BoolArgumentType.getBool(context, "showLeftHand"), BoolArgumentType.getBool(context, "showRightHand")))))))
                )
                .then(Commands.literal("editor")
                        .then(Commands.literal("quest").executes(ctx -> {
                            MessageLoader.getApiLoader().sendToPlayer(new MessageCreater(new OpenQuestEditorScreen()), ctx.getSource().getPlayerOrException());
                            return 1;
                        }))
                        .then(Commands.literal("style").executes(ctx -> {
                            MessageLoader.getApiLoader().sendToPlayer(new MessageCreater(new OpenStyleEditorScreen()), ctx.getSource().getPlayerOrException());
                            return 1;
                        }))
                        .then(Commands.literal("advancement").executes(ctx -> {
                            MessageLoader.getApiLoader().sendToPlayer(new MessageCreater(new OpenAdvancementEditorScreen()), ctx.getSource().getPlayerOrException());
                            return 1;
                        }))
                )
                .then(Commands.literal("quest")
                        // 完成所有进行中任务
                        .then(Commands.literal("completeall").executes(ApiCommand::questCompleteAll))
                        // 完成指定任务及其所有父任务
                                .then(Commands.literal("complete")
                                        .then(Commands.argument("quest", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                                .suggests(QUEST_SUGGESTIONS)
                                                .executes(ApiCommand::questCompleteChain)))
                        // 重置所有任务
                        .then(Commands.literal("reset").executes(ApiCommand::questReset))
                        // 列出所有任务状态
                        .then(Commands.literal("list").executes(ApiCommand::questList))
                        // 打开任务链 GUI（客户端本地渲染）
//                        .then(Commands.literal("tree").executes(ctx -> {
//                            MessageLoader.getApiLoader().sendToPlayer(new MessageCreater(new OpenQuestTreeScreen()),
//                                    ctx.getSource().getPlayer());
//                            return 1;
//                        }))
                )
        );
    }

    private static int armorLockCommend(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        Variables.setVariable(getEntity(commandSourceStackCommandContext), ModVariable.DISABLE_ARMOR_SLOT, BoolArgumentType.getBool(commandSourceStackCommandContext, "open"));
        return 0;
    }

    private static int animTest(CommandContext<CommandSourceStack> context, String modid, String name, boolean showLeftHand, boolean showRightHand) {
        if (!PlayerAnimator.instance().installed()) return 0;
        Entity entity = getEntity(context);
        if (entity instanceof Player player)
            PlayerAnimator.playerAnimation(player, modid, name, showRightHand, showLeftHand, true);
        return 0;
    }

    private static int testCommend(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        return 0;
    }

    private static int timeCommend(CommandContext<CommandSourceStack> context, int state) {
        Level world = context.getSource().getUnsidedLevel();
        GJ.TimerControl.timerStartUp(world, (LivingEntity) getEntity(context), state);
        return 0;
    }

    private static int timeUserCommend(CommandContext<CommandSourceStack> context, int state) {
        try {
            Entity entity = EntityArgument.getEntity(context, "user");
            Variables.setVariable(entity, ModVariable.TIME_STATUS, state);
            return 0;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
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

    private static int questCompleteAll(CommandContext<CommandSourceStack> ctx) {
        var sp = ctx.getSource().getPlayer();
        if (sp == null) return 0;
        var data = QuestAttachments.get(sp);
        int count = 0;
        // 复制一份避免遍历时修改
        for (var prog : new java.util.ArrayList<>(data.active())) {
            forceComplete(sp, prog.getQuestId());
            count++;
        }
        int fc = count;
        ctx.getSource().sendSuccess(() ->
                net.minecraft.network.chat.Component.literal("§a完成了 " + fc + " 个任务"), true);
        return count;
    }

    private static int questCompleteChain(CommandContext<CommandSourceStack> ctx) {
        var sp = ctx.getSource().getPlayer();
        if (sp == null) return 0;
        var qid = net.minecraft.commands.arguments.ResourceLocationArgument.getId(ctx, "quest");

        var defOpt = QuestRegistry.INSTANCE.get(qid);
        if (defOpt.isEmpty()) {
            ctx.getSource().sendFailure(
                    net.minecraft.network.chat.Component.literal("§c不存在的任务: " + qid));
            return 0;
        }

        // 任务标题
        var def = defOpt.get();
        net.minecraft.network.chat.Component title = def.title().startsWith("quest.")
                ? net.minecraft.network.chat.Component.translatable(def.title())
                : net.minecraft.network.chat.Component.literal(def.title());

        int n = completeChain(sp, qid);
        ctx.getSource().sendSuccess(() ->
                        net.minecraft.network.chat.Component.literal("§a完成任务链: ")
                                .append(title)
                                .append(net.minecraft.network.chat.Component.literal(" §7(" + n + " 个)")),
                true);
        return n;
    }

    private static int questReset(CommandContext<CommandSourceStack> ctx) {
        var sp = ctx.getSource().getPlayer();
        if (sp == null) return 0;
        var data = QuestAttachments.get(sp);
        data.clearAll();
        QuestNetwork.syncToPlayer(sp);
        ctx.getSource().sendSuccess(() ->
                net.minecraft.network.chat.Component.literal("§e已重置所有任务进度"), true);
        return 1;
    }

    private static int questList(CommandContext<CommandSourceStack> ctx) {
        var sp = ctx.getSource().getPlayer();
        if (sp == null) return 0;
        var data = QuestAttachments.get(sp);
        StringBuilder sb = new StringBuilder("§6=== 任务状态 ===\n");
        for (var def : QuestRegistry.INSTANCE.all()) {
            var st = data.get(def.id()).map(p -> p.getState().name()).orElse("NONE");
            sb.append("§7").append(def.id()).append(": §f").append(st).append("\n");
        }
        ctx.getSource().sendSuccess(() ->
                net.minecraft.network.chat.Component.literal(sb.toString()), false);
        return 1;
    }

    /** 强制完成单个任务（拉满目标计数并走正常完成流程） */
    public static void forceComplete(ServerPlayer sp, ResourceLocation questId) {
        var defOpt = QuestRegistry.INSTANCE.get(questId);
        if (defOpt.isEmpty()) return;  // 判定：没有就跳过
        var def = defOpt.get();
        var data = QuestAttachments.get(sp);
        var prog = data.getOrCreate(questId);
        prog.setState(QuestState.ACTIVE);
        for (var obj : def.objectives()) prog.setCounter(obj.getId(), obj.getRequired());
        QuestManager.tryCompleteQuest(sp, questId);
    }

    /** 完成任务及其所有父任务（递归），返回完成数量 */
    public static int completeChain(ServerPlayer sp, ResourceLocation questId) {
        var defOpt = QuestRegistry.INSTANCE.get(questId);
        if (defOpt.isEmpty()) return 0;  // 判定：不存在直接返回
        var def = defOpt.get();
        int count = 0;
        for (var parent : def.parentIds()) count += completeChain(sp, parent);
        forceComplete(sp, questId);
        return count + 1;
    }

    /** 任务 ID 补全，带标题 tooltip */
    private static final SuggestionProvider<CommandSourceStack> QUEST_SUGGESTIONS =
            (ctx, builder) -> {
                String input = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
                for (var def : QuestRegistry.INSTANCE.all()) {
                    String id = def.id().toString();
                    if (id.toLowerCase(java.util.Locale.ROOT).contains(input)) {
                        // 标题作为提示悬浮文本（翻译键就翻译，否则原文）
                        Component title = def.title().startsWith("quest.")
                                ? Component.translatable(def.title())
                                : Component.literal(def.title());
                        builder.suggest(id, title);
                    }
                }
                return builder.buildFuture();
            };
}
