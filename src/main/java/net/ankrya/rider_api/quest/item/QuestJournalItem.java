package net.ankrya.rider_api.quest.item;

import net.ankrya.rider_api.quest.client.QuestJournalScreen;
import net.ankrya.rider_api.quest.data.QuestFilter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class QuestJournalItem extends Item {

    private final QuestFilter filter;
    @Nullable private final ResourceLocation styleId;

    //===== 注册用构造 =====

    /** 最简：显示全部任务，默认样式 */
    public QuestJournalItem(Properties props) {
        this(props, QuestFilter.all(), null);
    }

    /** 只显示指定模组任务 */
    public QuestJournalItem(Properties props, QuestFilter filter) {
        this(props, filter, null);
    }

    /** 完整版：过滤器 + 样式 */
    public QuestJournalItem(Properties props,
                            QuestFilter filter,
                            @Nullable ResourceLocation styleId) {
        super(props.stacksTo(1));
        this.filter= filter;
        this.styleId = styleId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) openScreen();
        return InteractionResultHolder.sidedSuccess(
                player.getItemInHand(hand), level.isClientSide);
    }

    /** Tooltip 里显示过滤模式 */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> lines, TooltipFlag flag) {
        lines.add(filter.describe().copy()
                .withStyle(s -> s.withColor(0xFF888888)));
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        net.minecraft.client.Minecraft.getInstance()
                .setScreen(new QuestJournalScreen(styleId, filter));
    }
}