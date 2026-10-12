package net.ankrya.rider_api.quest.editor.translation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.locale.Language;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 编辑器内的翻译键管理，只处理 en_us */
public class TranslationManager {

    public static final TranslationManager INSTANCE = new TranslationManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** key -> en_us 值 */
    private final Map<String, String> translations = new ConcurrentHashMap<>();

    private TranslationManager() {}

    /** 获取翻译值：优先编辑器缓存，其次游戏已加载，最后返回 key 本身 */
    public String get(String key) {
        if (translations.containsKey(key)) return translations.get(key);
        String fromGame = Language.getInstance().getOrDefault(key);
        return fromGame; // getOrDefault 找不到会返回 key 本身
    }

    /** 新建或更新翻译键 */
    public void put(String key, String value) {
        translations.put(key, value);
    }

    /** 导出指定 key 集合为 en_us JSON */
    public void exportFiltered(Set<String> keys, File enFile) throws Exception {
        JsonObject en = new JsonObject();
        for (String key : keys) {
            if (translations.containsKey(key)) {
                en.addProperty(key, translations.get(key));
            }
        }
        if (!en.entrySet().isEmpty()) {
            try (FileWriter w = new FileWriter(enFile)) {
                GSON.toJson(en, w);
            }
        }
    }
}