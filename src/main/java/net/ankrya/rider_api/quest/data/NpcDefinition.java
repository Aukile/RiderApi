package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NpcDefinition {
    public final List<String> greetings;
    @Nullable public final String guiStyleId;
    
    public NpcDefinition(List<String> greetings, @Nullable String guiStyleId) { 
        this.greetings = greetings; 
        this.guiStyleId = guiStyleId;
    }

    public static NpcDefinition fromJson(JsonObject o) {
        List<String> g = new ArrayList<>();
        if (o.has("greetings"))
            for (var e : o.getAsJsonArray("greetings")) 
                g.add(e.getAsString());
        else
            g.add("npc.api.default_greeting");
        String guiStyleId = o.has("gui_style") ? o.get("gui_style").getAsString() : null;
        return new NpcDefinition(g, guiStyleId);
    }
}
