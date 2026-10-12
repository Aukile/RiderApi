package net.ankrya.rider_api.quest.editor.style;

import com.google.gson.JsonObject;
import net.ankrya.rider_api.quest.client.style.GuiStyle;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/** 编辑器内存中的样式，字段与 GuiStyle 一一对应，可变 */
public class StyleEditorData {

    public String styleId = "yourmod:my_theme";

    // dialogue
    @Nullable public String dialogueBgTexture = null;
    public int dialogueBgColor       = 0xCC000000;
    public int dialogueBorderColor   = 0xFFAAAAAA;
    public int dialogueTitleColor    = 0xFFFFD700;
    public int dialogueBodyColor     = 0xFFFFFFFF;
    public int dialogueProgressColor = 0xFFAAAAAA;
    public int dialogueDividerColor  = 0x66FFFFFF;
    public int dialogueBoxWidth      = 320;
    public int dialogueBoxHeight     = 100;
    public int dialoguePaddingLeft   = 8;
    public int dialoguePaddingTop    = 24;
    public int dialoguePaddingRight  = 8;
    public int dialoguePaddingBottom = 8;
    public int dialogueButtonWidth   = 80;
    public int dialogueButtonHeight  = 20;
    public int dialogueNameOffsetY   = 6;
    public int dialogueDividerOffsetY = 18;
    public int dialogueProgressOffsetX = 8;
    public int dialogueProgressOffsetY = 6;
    @Nullable public String dialogueButtonTexture = null;
    @Nullable public String dialogueButtonHoverTexture = null;

    // journal
    @Nullable public String journalBgTexture = null;
    public int journalFrameColor          = 0xFF1A1A2E;
    public int journalScreenColor         = 0xFF0F0F1A;
    @Nullable public String journalHeaderTexture = null;
    public int journalHeaderBgColor       = 0xFF16213E;
    public int journalHeaderTextColor     = 0xFF6EC6FF;
    public int journalItemBgColor         = 0xFF162032;
    @Nullable public String journalItemTexture = null;
    @Nullable public String journalButtonTexture = null;
    @Nullable public String journalButtonHoverTexture = null;
    public int journalTextColor           = 0xFFDDDDDD;
    public int journalDescColor           = 0xFF888888;
    public int journalDotActiveColor      = 0xFFFFAA22;
    public int journalDotCompletableColor = 0xFF44FF88;
    public int journalScrollTrackColor    = 0x33FFFFFF;
    public int journalScrollThumbColor    = 0xFFAAAAAA;

    // 按钮样式
    public int journalButtonColor         = 0xFF16213E;
    public int journalButtonHoverColor    = 0xFF1E3A5F;
    public int journalButtonTextColor     = 0xFF6EC6FF;

    /** 转成运行时 GuiStyle 供预览 */
    public GuiStyle toGuiStyle() {
        var b = GuiStyle.builder()
                .dialogueBgColor(dialogueBgColor)
                .dialogueBorderColor(dialogueBorderColor)
                .dialogueTitleColor(dialogueTitleColor)
                .dialogueBodyColor(dialogueBodyColor)
                .dialogueProgressColor(dialogueProgressColor)
                .dialogueDividerColor(dialogueDividerColor)
                .dialogueBoxWidth(dialogueBoxWidth)
                .dialogueBoxHeight(dialogueBoxHeight)
                .dialoguePaddingLeft(dialoguePaddingLeft)
                .dialoguePaddingTop(dialoguePaddingTop)
                .dialoguePaddingRight(dialoguePaddingRight)
                .dialoguePaddingBottom(dialoguePaddingBottom)
                .dialogueButtonWidth(dialogueButtonWidth)
                .dialogueButtonHeight(dialogueButtonHeight)
                .dialogueNameOffsetY(dialogueNameOffsetY)
                .dialogueDividerOffsetY(dialogueDividerOffsetY)
                .dialogueProgressOffsetX(dialogueProgressOffsetX)
                .dialogueProgressOffsetY(dialogueProgressOffsetY)
                .journalFrameColor(journalFrameColor)
                .journalScreenColor(journalScreenColor)
                .journalHeaderBgColor(journalHeaderBgColor)
                .journalHeaderTextColor(journalHeaderTextColor)
                .journalItemBgColor(journalItemBgColor)
                .journalTextColor(journalTextColor)
                .journalDescColor(journalDescColor)
                .journalDotActiveColor(journalDotActiveColor)
                .journalDotCompletableColor(journalDotCompletableColor)
                .journalScrollTrackColor(journalScrollTrackColor)
                .journalScrollThumbColor(journalScrollThumbColor)
                .journalButtonColor(journalButtonColor)
                .journalButtonHoverColor(journalButtonHoverColor)
                .journalButtonTextColor(journalButtonTextColor);
        if (dialogueBgTexture != null && !dialogueBgTexture.isEmpty())
            b.dialogueBgTexture(new ResourceLocation(dialogueBgTexture));
        if (journalBgTexture != null && !journalBgTexture.isEmpty())
            b.journalBgTexture(new ResourceLocation(journalBgTexture));
        if (journalHeaderTexture != null && !journalHeaderTexture.isEmpty())
            b.journalHeaderTexture(new ResourceLocation(journalHeaderTexture));
        if (journalItemTexture != null && !journalItemTexture.isEmpty())
            b.journalItemTexture(new ResourceLocation(journalItemTexture));
        if (journalButtonTexture != null && !journalButtonTexture.isEmpty())
            b.journalButtonTexture(new ResourceLocation(journalButtonTexture));
        if (journalButtonHoverTexture != null && !journalButtonHoverTexture.isEmpty())
            b.journalButtonHoverTexture(new ResourceLocation(journalButtonHoverTexture));
        if (dialogueButtonTexture != null && !dialogueButtonTexture.isEmpty())
            b.dialogueButtonTexture(new ResourceLocation(dialogueButtonTexture));
        if (dialogueButtonHoverTexture != null && !dialogueButtonHoverTexture.isEmpty())
            b.dialogueButtonHoverTexture(new ResourceLocation(dialogueButtonHoverTexture));
        return b.build();
    }

    /** 导出为样式 JSON，只写与默认值不同的字段 */
    public JsonObject toJson() {
        var def = GuiStyle.DEFAULT;
        JsonObject root = new JsonObject();

        JsonObject d = new JsonObject();
        if (dialogueBgTexture != null && !dialogueBgTexture.isEmpty())
            d.addProperty("background_texture", dialogueBgTexture);
        putColor(d, "bg_color",       dialogueBgColor,       def.dialogueBgColor);
        putColor(d, "border_color",   dialogueBorderColor,   def.dialogueBorderColor);
        putColor(d, "title_color",    dialogueTitleColor,    def.dialogueTitleColor);
        putColor(d, "body_color",     dialogueBodyColor,     def.dialogueBodyColor);
        putColor(d, "progress_color", dialogueProgressColor, def.dialogueProgressColor);
        putColor(d, "divider_color",  dialogueDividerColor,  def.dialogueDividerColor);
        if (dialogueBoxWidth  != def.dialogueBoxWidth)  d.addProperty("box_width",  dialogueBoxWidth);
        if (dialogueBoxHeight != def.dialogueBoxHeight) d.addProperty("box_height", dialogueBoxHeight);
        if (dialoguePaddingLeft != def.dialoguePaddingLeft || dialoguePaddingTop != def.dialoguePaddingTop
                || dialoguePaddingRight != def.dialoguePaddingRight || dialoguePaddingBottom != def.dialoguePaddingBottom) {
            JsonObject padding = new JsonObject();
            if (dialoguePaddingLeft != def.dialoguePaddingLeft) padding.addProperty("left", dialoguePaddingLeft);
            if (dialoguePaddingTop != def.dialoguePaddingTop) padding.addProperty("top", dialoguePaddingTop);
            if (dialoguePaddingRight != def.dialoguePaddingRight) padding.addProperty("right", dialoguePaddingRight);
            if (dialoguePaddingBottom != def.dialoguePaddingBottom) padding.addProperty("bottom", dialoguePaddingBottom);
            d.add("padding", padding);
        }
        if (dialogueButtonWidth != def.dialogueButtonWidth) d.addProperty("button_width", dialogueButtonWidth);
        if (dialogueButtonHeight != def.dialogueButtonHeight) d.addProperty("button_height", dialogueButtonHeight);
        if (dialogueNameOffsetY != def.dialogueNameOffsetY) d.addProperty("name_offset_y", dialogueNameOffsetY);
        if (dialogueDividerOffsetY != def.dialogueDividerOffsetY) d.addProperty("divider_offset_y", dialogueDividerOffsetY);
        if (dialogueProgressOffsetX != def.dialogueProgressOffsetX) d.addProperty("progress_offset_x", dialogueProgressOffsetX);
        if (dialogueProgressOffsetY != def.dialogueProgressOffsetY) d.addProperty("progress_offset_y", dialogueProgressOffsetY);
        if (dialogueButtonTexture != null && !dialogueButtonTexture.isEmpty())
            d.addProperty("button_texture", dialogueButtonTexture);
        if (dialogueButtonHoverTexture != null && !dialogueButtonHoverTexture.isEmpty())
            d.addProperty("button_hover_texture", dialogueButtonHoverTexture);
        if (!d.entrySet().isEmpty()) root.add("dialogue", d);

        JsonObject j = new JsonObject();
        if (journalBgTexture != null && !journalBgTexture.isEmpty())
            j.addProperty("background_texture", journalBgTexture);
        putColor(j, "frame_color",              journalFrameColor,          def.journalFrameColor);
        putColor(j, "screen_color",             journalScreenColor,         def.journalScreenColor);
        if (journalHeaderTexture != null && !journalHeaderTexture.isEmpty())
            j.addProperty("header_texture", journalHeaderTexture);
        putColor(j, "header_bg_color",          journalHeaderBgColor,       def.journalHeaderBgColor);
        putColor(j, "header_text_color",        journalHeaderTextColor,     def.journalHeaderTextColor);
        putColor(j, "item_bg_color",            journalItemBgColor,         def.journalItemBgColor);
        if (journalItemTexture != null && !journalItemTexture.isEmpty())
            j.addProperty("item_texture", journalItemTexture);
        if (journalButtonTexture != null && !journalButtonTexture.isEmpty())
            j.addProperty("button_texture", journalButtonTexture);
        if (journalButtonHoverTexture != null && !journalButtonHoverTexture.isEmpty())
            j.addProperty("button_hover_texture", journalButtonHoverTexture);
        putColor(j, "text_color",               journalTextColor,           def.journalTextColor);
        putColor(j, "desc_color",               journalDescColor,           def.journalDescColor);
        putColor(j, "dot_active_color",         journalDotActiveColor,      def.journalDotActiveColor);
        putColor(j, "dot_completable_color",    journalDotCompletableColor, def.journalDotCompletableColor);
        putColor(j, "scroll_track_color",       journalScrollTrackColor,    def.journalScrollTrackColor);
        putColor(j, "scroll_thumb_color",       journalScrollThumbColor,    def.journalScrollThumbColor);
        putColor(j, "button_color",             journalButtonColor,         def.journalButtonColor);
        putColor(j, "button_hover_color",       journalButtonHoverColor,    def.journalButtonHoverColor);
        putColor(j, "button_text_color",        journalButtonTextColor,     def.journalButtonTextColor);
        if (!j.entrySet().isEmpty()) root.add("journal", j);

        return root;
    }

    private static void putColor(JsonObject o, String key, int value, int defVal) {
        if (value != defVal) o.addProperty(key, String.format("0x%08X", value));
    }
}