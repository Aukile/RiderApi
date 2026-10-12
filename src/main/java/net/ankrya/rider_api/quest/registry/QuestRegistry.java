package net.ankrya.rider_api.quest.registry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.ankrya.rider_api.quest.data.QuestDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.*;

/**
 * 扫描所有命名空间下 data/<ns>/quests/*.json
 * 别的模组只要把 json 放到 data/<modid>/quests/ 就会被自动发现
 */
public class QuestRegistry extends SimpleJsonResourceReloadListener {

    private static final Logger LOG = LogUtils.getLogger();
    public static final QuestRegistry INSTANCE = new QuestRegistry();

    private final Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    private final Map<ResourceLocation, com.google.gson.JsonObject> rawJson = new LinkedHashMap<>();

    private QuestRegistry() {
        super(new Gson(), "quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, com.google.gson.JsonElement> map,
                         ResourceManager rm, ProfilerFiller pf) {
        quests.clear();
        rawJson.clear();
        for (var entry : map.entrySet()) {
            try {
                JsonObject o = entry.getValue().getAsJsonObject();
                // ResourceLocation key由 SimpleJsonResourceReloadListener 自动生成
                // 格式: <namespace>:<path_without_extension>
                QuestDefinition def = QuestDefinition.fromJson(entry.getKey(), o);
                quests.put(def.id(), def);
                rawJson.put(def.id(), o);
                LOG.debug("[quest] loaded: {}", def.id());
            } catch (Exception ex) {
                LOG.error("[quest] failed to load {}: {}", entry.getKey(), ex.getMessage());
            }
        }
        LOG.info("[quest] loaded {} quests total", quests.size());
    }

    public Optional<QuestDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(quests.get(id));
    }

    public Collection<QuestDefinition> all() {
        return Collections.unmodifiableCollection(quests.values());
    }

    public boolean has(ResourceLocation id) { return quests.containsKey(id); }

    public Optional<com.google.gson.JsonObject> getRaw(ResourceLocation id) {
        return Optional.ofNullable(rawJson.get(id));
    }
}