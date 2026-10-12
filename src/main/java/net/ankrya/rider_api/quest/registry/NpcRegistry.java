package net.ankrya.rider_api.quest.registry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.ankrya.rider_api.quest.data.NpcDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NpcRegistry extends SimpleJsonResourceReloadListener {
    public static final NpcRegistry INSTANCE = new NpcRegistry();
    private final Map<ResourceLocation, NpcDefinition> npcs = new HashMap<>();

    private NpcRegistry() { 
        super(new Gson(), "quest_npcs"); 
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager rm, ProfilerFiller pf) {
        npcs.clear();
        for (var e : map.entrySet()) {
            try { 
                npcs.put(e.getKey(), NpcDefinition.fromJson(e.getValue().getAsJsonObject())); 
            }
            catch (Exception ignored) {}
        }
    }

    public Optional<NpcDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(npcs.get(id));
    }
}
