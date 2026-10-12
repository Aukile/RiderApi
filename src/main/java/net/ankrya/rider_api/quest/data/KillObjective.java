package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class KillObjective extends QuestObjective {

    private final ResourceLocation entityType;
    private final int count;

    public KillObjective(String id, String label, ResourceLocation entityType, int count) {
        super(id, label);
        this.entityType = entityType;
        this.count = count;
    }

    public ResourceLocation getEntityType() { return entityType; }

    @Override public String getType() { return "kill"; }
    @Override public int getRequired() { return count; }
    @Override public int getCurrent(QuestProgress p) { return p.getCounter(id); }
    @Override public boolean isCompleted(QuestProgress p) { return getCurrent(p) >= count; }

    @Override
    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeUtf("kill");
        buf.writeUtf(id);
        buf.writeUtf(label);
        buf.writeResourceLocation(entityType);
        buf.writeInt(count);
    }

    public static KillObjective fromJson(JsonObject o) {
        return new KillObjective(o.get("id").getAsString(),
                o.get("label").getAsString(),
                new ResourceLocation(o.get("entity").getAsString()),
                o.get("count").getAsInt()
        );
    }

    public static KillObjective fromBuf(FriendlyByteBuf buf) {
        return new KillObjective(buf.readUtf(), buf.readUtf(),
                buf.readResourceLocation(), buf.readInt());
    }
}