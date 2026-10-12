package net.ankrya.rider_api.quest.client.style;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从所有命名空间的data/<ns>/quest_gui_styles/*.json 加载样式。
 * 别的模组只需在自己的 data/<modid>/quest_gui_styles/ 下放JSON 即可。
 */
public class GuiStyleRegistry extends SimpleJsonResourceReloadListener {

    private static final Logger LOG = LogUtils.getLogger();
    public static final GuiStyleRegistry INSTANCE = new GuiStyleRegistry();

    /** 代码注册（优先级低于 JSON） */
    private final Map<ResourceLocation, GuiStyle> runtime = new ConcurrentHashMap<>();
    /** JSON 加载结果 */
    private final Map<ResourceLocation, GuiStyle> loaded= new ConcurrentHashMap<>();

    private GuiStyleRegistry() {
        super(new Gson(), "quest_gui_styles");
    }

    /** 别的模组可在 FMLCommonSetup 里调用，注册代码风格 */
    public void registerRuntime(ResourceLocation id, GuiStyle style) {
        runtime.put(id, style);
    }

    public Optional<GuiStyle> get(ResourceLocation id) {
        if (id == null) return Optional.empty();
        GuiStyle s = loaded.get(id);
        if (s != null) return Optional.of(s);
        return Optional.ofNullable(runtime.get(id));
    }

    /** 获取样式，未找到则返回默认 */
    public GuiStyle getOrDefault(ResourceLocation id) {
        return get(id).orElse(GuiStyle.DEFAULT);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map,
                         ResourceManager rm, ProfilerFiller pf) {
        loaded.clear();
        for (var e : map.entrySet()) {
            try {
                GuiStyle style = parseStyle(e.getValue().getAsJsonObject());
                loaded.put(e.getKey(), style);
                LOG.debug("[quest] loaded gui style: {}", e.getKey());
            } catch (Exception ex) {
                LOG.error("[quest] failed to load gui style {}: {}", e.getKey(), ex.getMessage());
            }
        }
        LOG.info("[quest] loaded {} gui styles", loaded.size());
    }

    private static GuiStyle parseStyle(JsonObject o) {
        GuiStyle.Builder b = GuiStyle.builder();

        if (o.has("dialogue")) {
            JsonObject d = o.getAsJsonObject("dialogue");
            ifStr(d, "background_texture",v -> b.dialogueBgTexture(rl(v)));
            ifColor(d, "bg_color",               b::dialogueBgColor);
            ifColor(d, "border_color",           b::dialogueBorderColor);
            ifColor(d, "title_color",            b::dialogueTitleColor);
            ifColor(d, "body_color",             b::dialogueBodyColor);
            ifColor(d, "progress_color",         b::dialogueProgressColor);
            ifColor(d, "divider_color",          b::dialogueDividerColor);
            ifInt(d,"box_width",              b::dialogueBoxWidth);
            ifInt(d,   "box_height",             b::dialogueBoxHeight);
            if (d.has("padding")) {
                JsonObject p = d.getAsJsonObject("padding");
                ifInt(p, "left",   b::dialoguePaddingLeft);
                ifInt(p, "top",    b::dialoguePaddingTop);
                ifInt(p, "right",  b::dialoguePaddingRight);
                ifInt(p, "bottom", b::dialoguePaddingBottom);
            }
            ifInt(d, "button_width",  b::dialogueButtonWidth);
            ifInt(d, "button_height", b::dialogueButtonHeight);
            ifInt(d, "name_offset_y",    b::dialogueNameOffsetY);
            ifInt(d, "divider_offset_y", b::dialogueDividerOffsetY);
            ifInt(d, "progress_offset_x", b::dialogueProgressOffsetX);
            ifInt(d, "progress_offset_y", b::dialogueProgressOffsetY);
            ifStr(d, "button_texture",       v -> b.dialogueButtonTexture(rl(v)));
            ifStr(d, "button_hover_texture", v -> b.dialogueButtonHoverTexture(rl(v)));
        }

        if (o.has("journal")) {
            JsonObject j = o.getAsJsonObject("journal");
            ifStr(j, "background_texture",       v -> b.journalBgTexture(rl(v)));
            ifColor(j, "frame_color",            b::journalFrameColor);
            ifColor(j, "screen_color",           b::journalScreenColor);
            ifStr(j, "header_texture",           v -> b.journalHeaderTexture(rl(v)));
            ifColor(j, "header_bg_color",        b::journalHeaderBgColor);
            ifColor(j, "header_text_color",      b::journalHeaderTextColor);
            ifColor(j, "item_bg_color",          b::journalItemBgColor);
            ifStr(j, "item_texture",          v -> b.journalItemTexture(rl(v)));
            ifStr(j, "button_texture",       v -> b.journalButtonTexture(rl(v)));
            ifStr(j, "button_hover_texture", v -> b.journalButtonHoverTexture(rl(v)));
            ifColor(j, "text_color",b::journalTextColor);
            ifColor(j, "desc_color",             b::journalDescColor);
            ifColor(j, "dot_active_color",       b::journalDotActiveColor);
            ifColor(j, "dot_completable_color",  b::journalDotCompletableColor);
            ifColor(j, "scroll_track_color",     b::journalScrollTrackColor);
            ifColor(j, "scroll_thumb_color",     b::journalScrollThumbColor);
            ifColor(j, "button_color",           b::journalButtonColor);
            ifColor(j, "button_hover_color",     b::journalButtonHoverColor);
            ifColor(j, "button_text_color",      b::journalButtonTextColor);
        }

        return b.build();
    }

    // ===== 解析工具 =====

    @FunctionalInterface
    interface StrConsumer { void accept(String v); }
    @FunctionalInterface
    interface IntConsumer { void accept(int v); }

    private static void ifStr(JsonObject o, String key, StrConsumer fn) {
        if (o.has(key)) fn.accept(o.get(key).getAsString());
    }

    private static void ifInt(JsonObject o, String key, IntConsumer fn) {
        if (o.has(key)) fn.accept(o.get(key).getAsInt());
    }

    /**
     * 支持三种写法：
     *   "0xFFD700"(带0x前缀的hex字符串)
     *   "#FFD700"   (带#前缀的hex字符串)
     *   16763648    (纯数字)
     */
    private static void ifColor(JsonObject o, String key, IntConsumer fn) {
        if (!o.has(key)) return;
        var el = o.get(key);
        if (el.isJsonPrimitive()) {
            var prim = el.getAsJsonPrimitive();
            if (prim.isString()) {
                String s = prim.getAsString().trim();
                if (s.startsWith("0x") || s.startsWith("0X")) {
                    fn.accept((int) Long.parseLong(s.substring(2), 16));
                } else if (s.startsWith("#")) {
                    fn.accept((int) Long.parseLong(s.substring(1), 16));
                } else {
                    fn.accept((int) Long.parseLong(s, 16));
                }
            } else {
                fn.accept(prim.getAsInt());
            }
        }
    }

    private static ResourceLocation rl(String s) {
        return new ResourceLocation(s);
    }
}