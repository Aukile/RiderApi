package net.ankrya.rider_api.quest.client;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.quest.DialogueActionPacket;
import net.ankrya.rider_api.message.common.quest.OpenDialoguePacket;
import net.ankrya.rider_api.quest.client.style.GuiStyle;
import net.ankrya.rider_api.quest.client.style.GuiStyleRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class DialogueScreen extends Screen {

    private final OpenDialoguePacket data;
    private final List<String> lines;
    private final GuiStyle style;
    private int lineIndex = 0;

    // NPC 预览实体（客户端临时创建，仅用于渲染）
    private LivingEntity previewEntity;

    // 对话框布局（下半部分）
    private int boxX, boxY, boxW, boxH;

    public DialogueScreen(OpenDialoguePacket data) {
        super(Component.translatable("gui.api.dialogue.title"));
        this.data = data;
        this.lines = data.lines();
        this.style = GuiStyleRegistry.INSTANCE.getOrDefault(data.guiStyleId());
    }

    @Override
    protected void init() {
        boxW = Math.min(style.dialogueBoxWidth, width - 40);
        boxH = style.dialogueBoxHeight;
        boxX = (width - boxW) / 2;
        // 整体下移：放在屏幕下方，留出上方给 NPC 模型
        boxY = height - boxH - 40;

        // 创建 NPC 预览实体
        createPreviewEntity();

        rebuildButtons();
    }

    private void createPreviewEntity() {
        try {
            var type = BuiltInRegistries.ENTITY_TYPE.get(data.npcId());
            if (type != null && minecraft.level != null) {
                Entity e = type.create(minecraft.level);
                if (e instanceof LivingEntity le) previewEntity = le;
            }
        } catch (Exception ignored) {}
    }

    private void rebuildButtons() {
        clearWidgets();

        // 按钮放在对话框右下角内部，使用样式尺寸
        int btnW = style.dialogueButtonWidth;
        int btnH = style.dialogueButtonHeight;
        int btnY = boxY + boxH - style.dialoguePaddingBottom - btnH;
        int btnX = boxX + boxW - style.dialoguePaddingRight - btnW;

        boolean hasNext = lineIndex < lines.size() - 1;

        if (hasNext) {
            // 下一句
            addRenderableWidget(new StyledButton(btnX, btnY, btnW, btnH,
                    Component.translatable("gui.api.dialogue.next"),
                    b -> { lineIndex++; rebuildButtons(); }, style));
        } else {
            // 最后一句
            if (data.isGreeting()) {
                addRenderableWidget(new StyledButton(btnX, btnY, btnW, btnH,
                        Component.translatable("gui.api.dialogue.close"),
                        b -> onClose(), style));
            } else if (data.questId() != null) {
                boolean alreadyActive = ClientQuestCache.INSTANCE.activeQuests().stream()
                        .anyMatch(p -> p.getQuestId().equals(data.questId()));
                if (!alreadyActive) {
                    // 接受 + 拒绝
                    addRenderableWidget(new StyledButton(btnX - btnW - 8, btnY, btnW, btnH,
                            Component.translatable("gui.api.dialogue.accept_quest"),
                            b -> {
                                MessageLoader.getApiLoader().sendToServer(new DialogueActionPacket(
                                        data.questId(), DialogueActionPacket.Action.ACCEPT_QUEST));
                                onClose();
                            }, style));
                    addRenderableWidget(new StyledButton(btnX, btnY, btnW, btnH,
                            Component.translatable("gui.api.dialogue.decline"),
                            b -> onClose(), style));
                } else {
                    // 已接受：通知对话完成 + 关闭
                    MessageLoader.getApiLoader().sendToServer(new DialogueActionPacket(
                            data.questId(), DialogueActionPacket.Action.DIALOGUE_COMPLETE));
                    addRenderableWidget(new StyledButton(btnX, btnY, btnW, btnH,
                            Component.translatable("gui.api.dialogue.close"),
                            b -> onClose(), style));
                }
            } else {
                // 无任务，纯对话
                addRenderableWidget(new StyledButton(btnX, btnY, btnW, btnH,
                        Component.translatable("gui.api.dialogue.close"),
                        b -> onClose(), style));
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, width, height, 0x80000000);

        // === NPC 模型渲染在对话框上方 ===
        if (previewEntity != null) {
            int npcCenterX = boxX + 60;
            int npcBottomY = boxY - 5;   // 模型底部在对话框上方
            renderEntity(g, npcCenterX, npcBottomY, mx, my);
        }

        // === 对话框背景 ===
        if (style.dialogueBgTexture != null) {
            g.blit(style.dialogueBgTexture, boxX, boxY, 0, 0, boxW, boxH, boxW, boxH);
        } else {
            g.fill(boxX, boxY, boxX + boxW, boxY + boxH, style.dialogueBgColor);
        }
        g.renderOutline(boxX - 1, boxY - 1, boxW + 2, boxH + 2, style.dialogueBorderColor);

        // NPC 名字
        Component npcName = Component.translatable("npc." + data.npcId().getNamespace()
                + "." + data.npcId().getPath());
        g.drawString(font, npcName, boxX + style.dialoguePaddingLeft, boxY + style.dialogueNameOffsetY, style.dialogueTitleColor);
        g.fill(boxX + style.dialoguePaddingLeft, boxY + style.dialogueDividerOffsetY,
                boxX + boxW - style.dialoguePaddingRight, boxY + style.dialogueDividerOffsetY + 1, style.dialogueDividerColor);

        // === 台词 / 任务目标 ===
        renderContent(g);

        super.render(g, mx, my, pt);
    }


    private void renderContent(GuiGraphics g) {
        boolean isLastLine = lineIndex >= lines.size() - 1;
        boolean showObjectives = isLastLine && !data.isGreeting() && data.questId() != null;

        // 使用样式边距定位文字
        int textLeft = boxX + style.dialoguePaddingLeft;
        int textTop = boxY + style.dialoguePaddingTop;
        int textRight = boxX + boxW - style.dialoguePaddingRight;
        int textMaxWidth = textRight - textLeft;

        int ty = textTop;

        // 当前台词（对话内容）
        if (lineIndex < lines.size()) {
            String line = lines.get(lineIndex);
            Component text = (line.startsWith("quest.") || line.startsWith("npc."))
                    ? Component.translatable(line) : Component.literal(line);
            for (var wrapped : font.split(text, textMaxWidth)) {
                g.drawString(font, wrapped, textLeft, ty, style.dialogueBodyColor);
                ty += font.lineHeight + 2;
            }
        }

        // 只在最后一句显示未完成目标（排除对话目标）
        if (showObjectives) {
            ty += 4;
            List<Component> objs = collectUncompletedObjectives();
            if (!objs.isEmpty()) {
                g.drawString(font, Component.translatable("gui.api.dialogue.objectives"),
                        textLeft, ty, style.dialogueTitleColor);
                ty += font.lineHeight + 2;
                for (Component o : objs) {
                    g.drawString(font, Component.literal("• ").append(o),
                            textLeft + 4, ty, style.dialogueBodyColor);
                    ty += font.lineHeight + 1;
                }
            }
        }

        // 进度提示 x/y
        if (!data.isGreeting() && lines.size() > 1) {
            String prog = (lineIndex + 1) + "/" + lines.size();
            g.drawString(font, prog,
                    boxX + boxW - font.width(prog) - style.dialogueProgressOffsetX,
                    boxY + style.dialogueProgressOffsetY,
                    style.dialogueProgressColor);
        }
    }

    /** 收集当前任务里除"对话目标"外所有未完成的目标 */
    private List<Component> collectUncompletedObjectives() {
        var prog = ClientQuestCache.INSTANCE.activeQuests().stream()
                .filter(p -> p.getQuestId().equals(data.questId()))
                .findFirst().orElse(null);
        if (prog == null) return List.of();

        var defOpt = net.ankrya.rider_api.quest.registry.QuestRegistry.INSTANCE.get(data.questId());
        return defOpt.map(questDefinition -> questDefinition.objectives().stream()
                // 排除对话类目标
                .filter(obj -> !(obj instanceof net.ankrya.rider_api.quest.data.DialogueObjective))
                // 只显示未完成的
                .filter(obj -> !obj.isCompleted(prog))
                .map(obj -> obj.getProgressComponent(prog))
                .toList()).orElseGet(List::of);

    }

    /** 渲染实体到 GUI */
    private void renderEntity(GuiGraphics g, int centerX, int bottomY, int mx, int my) {
        if (previewEntity == null) return;

        // 根据实体高度自动算 scale，让它显示为固定像素高度
        float bbHeight = previewEntity.getBbHeight();       // 实体世界高度（方块）
        float bbWidth = previewEntity.getBbWidth();
        float maxDim = Math.max(bbHeight, bbWidth);

        // 想让模型显示成约 targetPixels 像素高
        int targetPixels = 60;
        int scale = (int) (targetPixels / Math.max(1.0f, maxDim));
        scale = Math.max(6, Math.min(scale, 60)); // 限制范围

        // 边界框给足够大，覆盖模型显示区域
        int halfW = 60;
        int topY = bottomY - targetPixels - 20;

        try {
            // 1.20：传入脚底坐标，鼠标参数是相对实体的偏移
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g, centerX, bottomY, scale,
                    (float) (centerX - mx), (float) (bottomY - targetPixels * 0.8f - my),
                    previewEntity);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        if (key == 69) { onClose(); return true; } // E 关闭
        // 空格/回车推进对话
        if ((key == 32 || key == 257) && lineIndex < lines.size() - 1) {
            lineIndex++;
            rebuildButtons();
            return true;
        }
        return super.keyPressed(key, scan, mod);
    }
}