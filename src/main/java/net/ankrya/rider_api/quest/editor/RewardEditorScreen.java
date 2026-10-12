package net.ankrya.rider_api.quest.editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RewardEditorScreen extends Screen {

    private final Screen parent;
    private final QuestEditorData data;
    private RewardListWidget listWidget;

    public RewardEditorScreen(Screen parent, QuestEditorData data) {
        super(Component.translatable("gui.api.editor.rewards.title"));
        this.parent = parent;
        this.data = data;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        // XP
        EditBox xpBox = new EditBox(font, cx - 100, 30, 130, 20, Component.literal("xp"));
        xpBox.setValue(String.valueOf(data.rewardXp));
        xpBox.setResponder(v -> {
            try { data.rewardXp = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(xpBox);

        // 添加奖励物品
        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.rewards.add_item"),
                b -> minecraft.setScreen(new ItemPickerScreen(this, id -> {
                    var ri = new QuestEditorData.RewardItem();
                    ri.item = id;
                    ri.count = 1;
                    data.rewardItems.add(ri);
                    listWidget.refresh();
                }))).bounds(cx + 40, 30, 60, 20).build());

        listWidget = new RewardListWidget();
        addRenderableWidget(listWidget);

        addRenderableWidget(Button.builder(Component.translatable("gui.api.editor.btn.done"),
                b -> minecraft.setScreen(parent)
        ).bounds(cx - 50, height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.api.editor.field.xp").getString(), width / 2 - 145, 36, 0xAAAAAA);
    }

    private class RewardListWidget extends ObjectSelectionList<RewardListWidget.Entry> {
        RewardListWidget() {
            super(RewardEditorScreen.this.minecraft, RewardEditorScreen.this.width, RewardEditorScreen.this.height - 100, 60, (60) + (RewardEditorScreen.this.height - 100), 22);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            refresh();
        }

        void refresh() {
            clearEntries();
            data.rewardItems.forEach(r -> addEntry(new Entry(r)));
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            final QuestEditorData.RewardItem reward;
            final ItemStack stack;
            Entry(QuestEditorData.RewardItem r) {
                reward = r;
                var item = BuiltInRegistries.ITEM.getOptional(
                        new ResourceLocation(r.item)).orElse(Items.BARRIER);
                stack = new ItemStack(item);
            }

            @Override
            public Component getNarration() { return Component.literal(reward.item); }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int w, int h,
                               int mx, int my, boolean hover, float pt) {
                g.renderItem(stack, left + 2, top + 2);
                g.drawString(font, stack.getHoverName().getString() + " x" + reward.count,
                        left + 24, top + 7, 0xFFFFFF);
                // 数量 +/-
                if (hover) {
                    g.drawString(font, "[-]", left + w - 90, top + 7, 0xFFAAAAAA);
                    g.drawString(font, "[+]", left + w - 70, top + 7, 0xFFAAAAAA);
                }
                if (hover && mx > left + w - 50) {
                    g.fill(left + w - 50, top, left + w, top + h, 0x55FF4444);
                    g.drawCenteredString(font, "Remove", left + w - 25, top + 7, 0xFFFFFF);
                }
            }

            @Override
            public boolean mouseClicked(double mx, double my, int btn) {
                int right = getRowRight();
                if (mx > right - 50) {
                    data.rewardItems.remove(reward);
                    refresh();
                    return true;
                }
                if (mx > right - 90 && mx < right - 70) {
                    reward.count = Math.max(1, reward.count - 1);
                    return true;
                }
                if (mx > right - 70 && mx < right - 50) {
                    reward.count++;
                    return true;
                }
                return false;
            }
        }
    }
}