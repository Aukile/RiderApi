package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class DialogueObjective extends QuestObjective {

    private final ResourceLocation npcId;

    public DialogueObjective(String id, String label, ResourceLocation npcId) {
        super(id, label);
        this.npcId = npcId;
    }

    public ResourceLocation getNpcId() { return npcId; }

    @Override public String getType() { return "dialogue"; }
    @Override public int getRequired() { return 1; }
    @Override public int getCurrent(QuestProgress p) { return p.getCounter(id); }
    @Override public boolean isCompleted(QuestProgress p) { return getCurrent(p) >= 1; }

    @Override
    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeUtf("dialogue");
        buf.writeUtf(id);
        buf.writeUtf(label);
        buf.writeResourceLocation(npcId);
    }

    public static DialogueObjective fromJson(JsonObject o) {
        return new DialogueObjective(
                o.get("id").getAsString(),
                o.get("label").getAsString(),
                new ResourceLocation(o.get("npc_id").getAsString())
        );
    }

    public static DialogueObjective fromBuf(FriendlyByteBuf buf) {
        return new DialogueObjective(buf.readUtf(), buf.readUtf(), buf.readResourceLocation());
    }
}