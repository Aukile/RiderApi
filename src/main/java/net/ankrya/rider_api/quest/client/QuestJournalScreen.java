package net.ankrya.rider_api.quest.client;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.quest.AcceptQuestPacket;
import net.ankrya.rider_api.quest.client.style.GuiStyle;
import net.ankrya.rider_api.quest.client.style.GuiStyleRegistry;
import net.ankrya.rider_api.quest.data.DialogueObjective;
import net.ankrya.rider_api.quest.data.QuestFilter;
import net.ankrya.rider_api.quest.data.QuestProgress;
import net.ankrya.rider_api.quest.data.QuestState;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class QuestJournalScreen extends Screen {

    private static final int PHONE_W = 140;
    private static final int PHONE_H = 230;
    private static final int ITEM_H = 22;

    private int scrollOffset = 0;
    private ResourceLocation expandedQuestId = null;
    private final GuiStyle style;
    @Nullable private final ResourceLocation styleId;
    private final QuestFilter filter;

    /** 模式：false=进行中+可接取，true=可重复已完成 */
    private final boolean repeatableMode;

    public QuestJournalScreen() {
        this(null, QuestFilter.all());
    }

    public QuestJournalScreen(@Nullable ResourceLocation styleId) {
        this(styleId, QuestFilter.all());
    }

    public QuestJournalScreen(@Nullable ResourceLocation styleId, QuestFilter filter) {
        this(styleId, filter, false);
    }

    public QuestJournalScreen(@Nullable ResourceLocation styleId, QuestFilter filter, boolean repeatableMode) {
        super(Component.translatable("gui.api.journal.title"));
        this.style = GuiStyleRegistry.INSTANCE.getOrDefault(styleId);
        this.styleId = styleId;
        this.filter = filter;
        this.repeatableMode = repeatableMode;
    }

    /** 当前模式下要显示的任务列表 */
    private List<QuestProgress> currentList() {
        List<QuestProgress> list = new ArrayList<>();
        if (repeatableMode) {
            list.addAll(ClientQuestCache.INSTANCE.repeatableCompleted());
        } else {
            list.addAll(ClientQuestCache.INSTANCE.activeQuests());
            list.addAll(ClientQuestCache.INSTANCE.availableQuests());
        }
        return filter.apply(list);
    }

    @Override
    protected void init() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int px = width / 2 - PHONE_W / 2;
        int py = height / 2 - PHONE_H / 2;

        // 手机外壳
        if (style.journalBgTexture != null) {
            g.blit(style.journalBgTexture,
                    px - 4, py - 6, 0, 0,
                    PHONE_W + 8, PHONE_H + 12, PHONE_W + 8, PHONE_H + 12);
        } else {
            g.fill(px - 4, py - 6, px + PHONE_W + 4, py + PHONE_H + 6, style.journalFrameColor);
            g.fill(px, py, px + PHONE_W, py + PHONE_H, style.journalScreenColor);
        }

        // 顶部状态栏
        if (style.journalHeaderTexture != null) {
            g.blit(style.journalHeaderTexture, px, py, 0, 0, PHONE_W, 16, PHONE_W, 16);
        } else {
            g.fill(px, py, px + PHONE_W, py + 16, style.journalHeaderBgColor);
        }
        Component header = repeatableMode
                ? Component.translatable("gui.api.journal.repeatable_header")
                : Component.translatable("gui.api.journal.header");
        g.drawCenteredString(font, header, px + PHONE_W / 2, py + 4, style.journalHeaderTextColor);

        // 当前列表
        List<QuestProgress> list = currentList();

        if (list.isEmpty()) {
            g.drawCenteredString(font,
                    Component.translatable("gui.api.journal.no_quests"),
                    px + PHONE_W / 2, py + PHONE_H / 2,
                    style.journalDescColor);
        } else {
            renderListItems(g, px, py, list, mx, my);
        }

        // 切换按钮：无论列表空不空都画
        renderSwitchButton(g, px, py, mx, my);

        super.render(g, mx, my, pt);
    }

    /** 渲染列表条目（不含切换按钮） */
    private void renderListItems(GuiGraphics g, int px, int py, List<QuestProgress> list, int mx, int my) {
        int listTop = py + 20;
        int listBot = py + PHONE_H - 22;
        g.enableScissor(px, listTop, px + PHONE_W, listBot);

        int renderY = listTop - scrollOffset;
        for (QuestProgress prog : list) {
            ResourceLocation qid = prog.getQuestId();
            boolean expanded = qid.equals(expandedQuestId);
            var defOpt = QuestRegistry.INSTANCE.get(qid);
            String title = defOpt.map(d -> d.title()).orElse(qid.toString());

            // 条目背景（合并按普通/展开，展开时叠加高亮）
            int itemW = PHONE_W - 4;
            if (style.journalItemTexture != null) {
                g.blit(style.journalItemTexture, px + 2, renderY, 0, 0, itemW, ITEM_H, itemW, ITEM_H);
            } else {
                g.fill(px + 2, renderY, px + PHONE_W - 2, renderY + ITEM_H, style.journalItemBgColor);
            }
            if (expanded) {
                g.fill(px + 2, renderY, px + PHONE_W - 2, renderY + ITEM_H, 0x22FFFFFF);
            }

            // 状态点
            boolean isAvailable = prog.getState() == QuestState.AVAILABLE;
            boolean completable = defOpt.map(def ->
                    def.objectives().stream().allMatch(o -> o.isCompleted(prog))).orElse(false);
            int dotColor;
            if (isAvailable) {
                dotColor = 0xFF44AAFF; // 蓝色 = 可接取
            } else if (completable) {
                dotColor = style.journalDotCompletableColor; // 绿色 = 可交付
            } else {
                dotColor = style.journalDotActiveColor; // 橙色 = 进行中
            }
            g.fill(px + 5, renderY + 8, px + 10, renderY + 13, dotColor);

            // 任务标题
            Component titleComp = title.startsWith("quest.")
                    ? Component.translatable(title) : Component.literal(title);
            int titleMaxW = (isAvailable || repeatableMode) ? PHONE_W - 46 : PHONE_W - 20;
            if (filter.showModLabel() && !isAvailable && !repeatableMode) {
                titleMaxW = PHONE_W - 46;
            }
            String titleStr = font.plainSubstrByWidth(titleComp.getString(), titleMaxW);
            g.drawString(font, titleStr, px + 14, renderY + 7, style.journalTextColor);

            // 来源模组标注
            if (filter.showModLabel() && !isAvailable && !repeatableMode) {
                String ns = qid.getNamespace();
                String modName = font.plainSubstrByWidth(
                        QuestFilter.modDisplayName(ns).getString(), 34);
                g.drawString(font, modName,
                        px + PHONE_W - 2 - font.width(modName),
                        renderY + 7, style.journalDescColor);
            }

            // 接取按钮（AVAILABLE 或 可重复已完成）
            if (isAvailable || repeatableMode) {
                int bx = px + PHONE_W - 22;
                boolean hover = mx >= bx && mx < bx + 18
                        && my >= renderY + 3 && my < renderY + ITEM_H - 3;
                g.fill(bx, renderY + 3, bx + 18, renderY + ITEM_H - 3,
                        hover ? style.journalButtonHoverColor : style.journalButtonColor);
                g.drawCenteredString(font, "+", bx + 9, renderY + 7, style.journalButtonTextColor);
            } else {
                // 展开箭头
                g.drawString(font, expanded ? "▲" : "▼",
                        px + PHONE_W - 12, renderY + 7, style.journalDescColor);
            }

            renderY += ITEM_H;

            // 展开显示详情（ACTIVE 和 AVAILABLE 都可展开）
            if (expanded && defOpt.isPresent()) {
                var def = defOpt.get();

                g.fill(px + 4, renderY, px + PHONE_W - 4, renderY + 1, style.journalDescColor);
                renderY += 4;

                if (filter.mode != QuestFilter.Mode.ONLY_MOD) {
                    Component sourceComp = Component.translatable(
                            "gui.api.journal.source",
                            QuestFilter.modDisplayName(qid.getNamespace()));
                    g.drawString(font, sourceComp, px + 8, renderY, style.journalDescColor);
                    renderY += font.lineHeight + 2;
                }

                // 描述
                Component descComp = def.description().startsWith("quest.")
                        ? Component.translatable(def.description())
                        : Component.literal(def.description());
                for (var line : font.split(descComp, PHONE_W - 12)) {
                    g.drawString(font, line, px + 8, renderY, style.journalDescColor);
                    renderY += font.lineHeight + 1;
                }
                renderY += 3;

                // 目标（AVAILABLE 预览时排除对话目标）
                for (var obj : def.objectives()) {
                    if (isAvailable && obj instanceof DialogueObjective) continue;
                    boolean done = obj.isCompleted(prog);
                    int c = done ? style.journalDotCompletableColor : style.journalTextColor;
                    String check = done ? "§a✔ " : "○ ";
                    Component objLine = Component.literal(check)
                            .append(obj.getProgressComponent(prog));
                    g.drawString(font, objLine, px + 10, renderY, c);
                    renderY += font.lineHeight + 3;
                }
                renderY += 4;
            }
        }

        g.disableScissor();

        // 滚动条
        int listH = listBot - listTop;
        int totalH = computeTotalHeight(list);
        if (totalH > listH) {
            int thumbH = Math.max(12, listH * listH / totalH);
            int thumbY = listTop + (int) ((float) scrollOffset
                    / (totalH - listH) * (listH - thumbH));
            g.fill(px + PHONE_W - 3, listTop, px + PHONE_W - 1, listBot,
                    style.journalScrollTrackColor);
            g.fill(px + PHONE_W - 3, thumbY, px + PHONE_W - 1, thumbY + thumbH,
                    style.journalScrollThumbColor);
        }
    }

    /** 渲染底部切换按钮（独立方法，始终绘制） */
    private void renderSwitchButton(GuiGraphics g, int px, int py, int mx, int my) {
        int switchY = py + PHONE_H - 18;
        int btnX = px + 2;
        int btnW = PHONE_W - 4;
        int btnH = 16;

        boolean hover = mx >= btnX && mx < btnX + btnW
                && my >= switchY && my < switchY + btnH;

        ResourceLocation tex = hover
                ? style.journalButtonHoverTexture
                : style.journalButtonTexture;
        // 悬停态没单独贴图就退回普通贴图
        if (tex == null) tex = style.journalButtonTexture;

        if (tex != null) {
            g.blit(tex, btnX, switchY, 0, 0, btnW, btnH, btnW, btnH);
        } else {
            g.fill(btnX, switchY, btnX + btnW, switchY + btnH,
                    hover ? style.journalButtonHoverColor : style.journalButtonColor);
        }

        Component text = repeatableMode
                ? Component.translatable("gui.api.journal.back_to_active")
                : Component.translatable("gui.api.journal.view_repeatable");
        g.drawCenteredString(font, text, px + PHONE_W / 2, switchY + 4,
                style.journalButtonTextColor);
    }

    /** 计算展开后的总高度（含 AVAILABLE 排除 DialogueObjective 的逻辑） */
    private int computeTotalHeight(List<QuestProgress> list) {
        int h = 0;
        for (var prog : list) {
            h += ITEM_H;
            if (prog.getQuestId().equals(expandedQuestId)) {
                var defOpt = QuestRegistry.INSTANCE.get(prog.getQuestId());
                if (defOpt.isPresent()) {
                    var def = defOpt.get();
                    boolean isAvailable = prog.getState() == QuestState.AVAILABLE;
                    h += 5;
                    if (filter.mode != QuestFilter.Mode.ONLY_MOD)
                        h += font.lineHeight + 2;
                    h += font.split(Component.literal(def.description()), PHONE_W - 12)
                            .size() * (font.lineHeight + 1) + 3;
                    // AVAILABLE 时排除 DialogueObjective
                    int objCount = isAvailable
                            ? (int) def.objectives().stream().filter(o -> !(o instanceof DialogueObjective)).count()
                            : def.objectives().size();
                    h += objCount * (font.lineHeight + 3) + 4;
                }
            }
        }
        return h;
    }

    @Override
    public boolean mouseClicked(double mxD, double myD, int btn) {
        int px = width / 2 - PHONE_W / 2;
        int py = height / 2 - PHONE_H / 2;
        int mx = (int) mxD, my = (int) myD;

        // 底部切换按钮（始终可点击）
        int switchY = py + PHONE_H - 18;
        if (mx >= px + 2 && mx < px + PHONE_W - 2 && my >= switchY && my < switchY + 16) {
            minecraft.setScreen(new QuestJournalScreen(styleId, filter, !repeatableMode));
            return true;
        }

        List<QuestProgress> list = currentList();
        int listTop = py + 20;
        int renderY = listTop - scrollOffset;

        for (var prog : list) {
            ResourceLocation qid = prog.getQuestId();
            boolean isAvailable = prog.getState() == QuestState.AVAILABLE;

            // 接取按钮
            if (isAvailable || repeatableMode) {
                int bx = px + PHONE_W - 22;
                if (mx >= bx && mx < bx + 18 && my >= renderY + 3 && my < renderY + ITEM_H - 3) {
                    MessageLoader.getApiLoader().sendToServer(new AcceptQuestPacket(qid));
                    return true;
                }
            }

            // 点条目展开/折叠（ACTIVE 和 AVAILABLE 都可展开，可重复模式也可展开）
            if (mx >= px + 2 && mx < px + PHONE_W - 2
                    && my >= renderY && my < renderY + ITEM_H) {
                expandedQuestId = qid.equals(expandedQuestId) ? null : qid;
                return true;
            }

            renderY += ITEM_H;

            // 展开高度累加
            if (qid.equals(expandedQuestId)) {
                var defOpt = QuestRegistry.INSTANCE.get(qid);
                if (defOpt.isPresent()) {
                    var def = defOpt.get();
                    renderY += 5;
                    if (filter.mode != QuestFilter.Mode.ONLY_MOD)
                        renderY += font.lineHeight + 2;
                    renderY += font.split(Component.literal(def.description()), PHONE_W - 12)
                            .size() * (font.lineHeight + 1) + 3;
                    int objCount = isAvailable
                            ? (int) def.objectives().stream().filter(o -> !(o instanceof DialogueObjective)).count()
                            : def.objectives().size();
                    renderY += objCount * (font.lineHeight + 3) + 4;
                }
            }
        }
        return super.mouseClicked(mxD, myD, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dy) {
        int px = width / 2 - PHONE_W / 2;
        int py = height / 2 - PHONE_H / 2;
        if (mx >= px && mx <= px + PHONE_W && my >= py && my <= py + PHONE_H) {
            List<QuestProgress> list = currentList();
            int totalH = computeTotalHeight(list);
            int listH = PHONE_H - 24;
            int maxScroll = Math.max(0, totalH - listH);
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - dy * 10));
            return true;
        }
        return super.mouseScrolled(mx, my, dy);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(int key, int scan, int mod) {
        if (key == 69) { onClose(); return true; }
        return super.keyPressed(key, scan, mod);
    }
}
