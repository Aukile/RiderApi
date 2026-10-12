package net.ankrya.rider_api.quest.client.style;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * 对话框+ 任务日志手机UI的全部视觉属性。
 * 所有字段都有默认值，JSON 中只需写要覆盖的字段。
 */
public class GuiStyle {

    //===== 对话框=====
    @Nullable public final ResourceLocation dialogueBgTexture; // null = 纯色
    public final int dialogueBgColor;
    public final int dialogueBorderColor;
    public final int dialogueTitleColor;
    public final int dialogueBodyColor;
    public final int dialogueProgressColor;
    public final int dialogueDividerColor;
    public final int dialogueBoxWidth;
    public final int dialogueBoxHeight;

    // 对话框边距
    public final int dialoguePaddingLeft;
    public final int dialoguePaddingTop;
    public final int dialoguePaddingRight;
    public final int dialoguePaddingBottom;

    // 对话框按钮尺寸
    public final int dialogueButtonWidth;
    public final int dialogueButtonHeight;

    // NPC 名字 / 分割线 Y 偏移（相对对话框顶部）
    public final int dialogueNameOffsetY;
    public final int dialogueDividerOffsetY;

    // 页数偏移（距右边界 X / 距顶部 Y）
    public final int dialogueProgressOffsetX;
    public final int dialogueProgressOffsetY;

    // 对话框按钮贴图
    @Nullable public final ResourceLocation dialogueButtonTexture;
    @Nullable public final ResourceLocation dialogueButtonHoverTexture;

    // ===== 任务日志(手机) =====
    @Nullable public final ResourceLocation journalBgTexture;// null = 纯色
    public final int journalFrameColor;
    public final int journalScreenColor;
    @Nullable public final ResourceLocation journalHeaderTexture; // 顶栏背景图
    public final int journalHeaderBgColor;
    public final int journalHeaderTextColor;
    public final int journalItemBgColor;
    @Nullable public final ResourceLocation journalItemTexture;         // 条目背景图
    @Nullable public final ResourceLocation journalButtonTexture;      // 普通态贴图
    @Nullable public final ResourceLocation journalButtonHoverTexture; // 悬停态贴图
    public final int journalTextColor;
    public final int journalDescColor;
    public final int journalDotActiveColor;
    public final int journalDotCompletableColor;
    public final int journalScrollTrackColor;
    public final int journalScrollThumbColor;

    // ===== 按钮样式 =====
    public final int journalButtonColor;
    public final int journalButtonHoverColor;
    public final int journalButtonTextColor;

    private GuiStyle(Builder b) {
        this.dialogueBgTexture         = b.dialogueBgTexture;
        this.dialogueBgColor           = b.dialogueBgColor;
        this.dialogueBorderColor       = b.dialogueBorderColor;
        this.dialogueTitleColor        = b.dialogueTitleColor;
        this.dialogueBodyColor         = b.dialogueBodyColor;
        this.dialogueProgressColor     = b.dialogueProgressColor;
        this.dialogueDividerColor      = b.dialogueDividerColor;
        this.dialogueBoxWidth          = b.dialogueBoxWidth;
        this.dialogueBoxHeight         = b.dialogueBoxHeight;
        this.dialoguePaddingLeft       = b.dialoguePaddingLeft;
        this.dialoguePaddingTop        = b.dialoguePaddingTop;
        this.dialoguePaddingRight      = b.dialoguePaddingRight;
        this.dialoguePaddingBottom     = b.dialoguePaddingBottom;
        this.dialogueButtonWidth       = b.dialogueButtonWidth;
        this.dialogueButtonHeight      = b.dialogueButtonHeight;
        this.dialogueNameOffsetY       = b.dialogueNameOffsetY;
        this.dialogueDividerOffsetY    = b.dialogueDividerOffsetY;
        this.dialogueProgressOffsetX   = b.dialogueProgressOffsetX;
        this.dialogueProgressOffsetY   = b.dialogueProgressOffsetY;
        this.dialogueButtonTexture     = b.dialogueButtonTexture;
        this.dialogueButtonHoverTexture= b.dialogueButtonHoverTexture;

        this.journalBgTexture          = b.journalBgTexture;
        this.journalFrameColor         = b.journalFrameColor;
        this.journalScreenColor        = b.journalScreenColor;
        this.journalHeaderTexture      = b.journalHeaderTexture;
        this.journalHeaderBgColor      = b.journalHeaderBgColor;
        this.journalHeaderTextColor    = b.journalHeaderTextColor;
        this.journalItemBgColor        = b.journalItemBgColor;
        this.journalItemTexture        = b.journalItemTexture;
        this.journalButtonTexture      = b.journalButtonTexture;
        this.journalButtonHoverTexture = b.journalButtonHoverTexture;
        this.journalTextColor          = b.journalTextColor;
        this.journalDescColor          = b.journalDescColor;
        this.journalDotActiveColor     = b.journalDotActiveColor;
        this.journalDotCompletableColor= b.journalDotCompletableColor;
        this.journalScrollTrackColor   = b.journalScrollTrackColor;
        this.journalScrollThumbColor   = b.journalScrollThumbColor;

        this.journalButtonColor        = b.journalButtonColor;
        this.journalButtonHoverColor   = b.journalButtonHoverColor;
        this.journalButtonTextColor    = b.journalButtonTextColor;
    }

    // ===== 默认样式（与原来代码里硬编码的颜色一致） =====
    public static final GuiStyle DEFAULT = new Builder().build();

    public static Builder builder() { return new Builder(); }

    // ===== Builder =====
    public static class Builder {
        @Nullable ResourceLocation dialogueBgTexture = null;
        int dialogueBgColor           = 0xCC000000;
        int dialogueBorderColor       = 0xFFAAAAAA;
        int dialogueTitleColor        = 0xFFD700;
        int dialogueBodyColor         = 0xFFFFFFFF;
        int dialogueProgressColor     = 0xFFAAAAAA;
        int dialogueDividerColor      = 0x66FFFFFF;
        int dialogueBoxWidth          = 320;
        int dialogueBoxHeight         = 100;
        int dialoguePaddingLeft       = 8;
        int dialoguePaddingTop        = 24;
        int dialoguePaddingRight      = 8;
        int dialoguePaddingBottom     = 8;
        int dialogueButtonWidth       = 80;
        int dialogueButtonHeight      = 20;
        int dialogueNameOffsetY       = 6;
        int dialogueDividerOffsetY    = 18;
        int dialogueProgressOffsetX   = 8;
        int dialogueProgressOffsetY   = 6;
        @Nullable ResourceLocation dialogueButtonTexture = null;
        @Nullable ResourceLocation dialogueButtonHoverTexture = null;

        @Nullable ResourceLocation journalBgTexture = null;
        int journalFrameColor         = 0xFF1A1A2E;
        int journalScreenColor        = 0xFF0F0F1A;
        @Nullable ResourceLocation journalHeaderTexture = null;
        int journalHeaderBgColor      = 0xFF16213E;
        int journalHeaderTextColor    = 0xFF6EC6FF;
        int journalItemBgColor        = 0xFF162032;
        @Nullable ResourceLocation journalItemTexture = null;
        @Nullable ResourceLocation journalButtonTexture = null;
        @Nullable ResourceLocation journalButtonHoverTexture = null;
        int journalTextColor          = 0xFFDDDDDD;
        int journalDescColor          = 0xFF888888;
        int journalDotActiveColor     = 0xFFFFAA22;
        int journalDotCompletableColor= 0xFF44FF88;
        int journalScrollTrackColor   = 0x33FFFFFF;
        int journalScrollThumbColor   = 0xFFAAAAAA;

        int journalButtonColor        = 0xFF16213E;
        int journalButtonHoverColor   = 0xFF1E3A5F;
        int journalButtonTextColor    = 0xFF6EC6FF;

        public Builder dialogueBgTexture(ResourceLocation v){ dialogueBgTexture  = v; return this; }
        public Builder dialogueBgColor(int v)                 { dialogueBgColor    = v; return this; }
        public Builder dialogueBorderColor(int v)             { dialogueBorderColor= v; return this; }
        public Builder dialogueTitleColor(int v)              { dialogueTitleColor = v; return this; }
        public Builder dialogueBodyColor(int v)               { dialogueBodyColor  = v; return this; }
        public Builder dialogueProgressColor(int v)           { dialogueProgressColor = v; return this; }
        public Builder dialogueDividerColor(int v)            { dialogueDividerColor  = v; return this; }
        public Builder dialogueBoxWidth(int v)                { dialogueBoxWidth   = v; return this; }
        public Builder dialogueBoxHeight(int v)               { dialogueBoxHeight  = v; return this; }
        public Builder dialoguePaddingLeft(int v)             { dialoguePaddingLeft = v; return this; }
        public Builder dialoguePaddingTop(int v)              { dialoguePaddingTop  = v; return this; }
        public Builder dialoguePaddingRight(int v)            { dialoguePaddingRight = v; return this; }
        public Builder dialoguePaddingBottom(int v)           { dialoguePaddingBottom = v; return this; }
        public Builder dialogueButtonWidth(int v)             { dialogueButtonWidth  = v; return this; }
        public Builder dialogueButtonHeight(int v)            { dialogueButtonHeight = v; return this; }
        public Builder dialogueNameOffsetY(int v)             { dialogueNameOffsetY  = v; return this; }
        public Builder dialogueDividerOffsetY(int v)          { dialogueDividerOffsetY = v; return this; }
        public Builder dialogueProgressOffsetX(int v)         { dialogueProgressOffsetX = v; return this; }
        public Builder dialogueProgressOffsetY(int v)         { dialogueProgressOffsetY = v; return this; }
        public Builder dialogueButtonTexture(ResourceLocation v)      { dialogueButtonTexture = v; return this; }
        public Builder dialogueButtonHoverTexture(ResourceLocation v) { dialogueButtonHoverTexture = v; return this; }

        public Builder journalBgTexture(ResourceLocation v){ journalBgTexture   = v; return this; }
        public Builder journalFrameColor(int v)               { journalFrameColor  = v; return this; }
        public Builder journalScreenColor(int v)              { journalScreenColor = v; return this; }
        public Builder journalHeaderTexture(ResourceLocation v){ journalHeaderTexture = v; return this; }
        public Builder journalHeaderBgColor(int v)            { journalHeaderBgColor  = v; return this; }
        public Builder journalHeaderTextColor(int v)          { journalHeaderTextColor= v; return this; }
        public Builder journalItemBgColor(int v)              { journalItemBgColor = v; return this; }
        public Builder journalItemTexture(ResourceLocation v)         { journalItemTexture = v; return this; }
        public Builder journalButtonTexture(ResourceLocation v)           { journalButtonTexture = v; return this; }
        public Builder journalButtonHoverTexture(ResourceLocation v)      { journalButtonHoverTexture = v; return this; }
        public Builder journalTextColor(int v)                { journalTextColor= v; return this; }
        public Builder journalDescColor(int v)                { journalDescColor   = v; return this; }
        public Builder journalDotActiveColor(int v)           { journalDotActiveColor     = v; return this; }
        public Builder journalDotCompletableColor(int v)      { journalDotCompletableColor= v; return this; }
        public Builder journalScrollTrackColor(int v)         { journalScrollTrackColor   = v; return this; }
        public Builder journalScrollThumbColor(int v)         { journalScrollThumbColor   = v; return this; }

        public Builder journalButtonColor(int v)              { journalButtonColor = v; return this; }
        public Builder journalButtonHoverColor(int v)         { journalButtonHoverColor = v; return this; }
        public Builder journalButtonTextColor(int v)          { journalButtonTextColor = v; return this; }

        public GuiStyle build() { return new GuiStyle(this); }
    }
}