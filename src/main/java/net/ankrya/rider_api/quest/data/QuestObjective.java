package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public abstract class QuestObjective {

    protected final String id;
    protected final String label;

    protected QuestObjective(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }

    /**当前是否完成 */
    public abstract boolean isCompleted(QuestProgress progress);
    /** 总需求量（用于 UI 进度条） */
    public abstract int getRequired();
    /** 当前数量（用于 UI 进度文字） */
    public abstract int getCurrent(QuestProgress progress);

    public Component getProgressComponent(QuestProgress progress) {
        return Component.literal(label + " (" + getCurrent(progress) + "/" + getRequired() + ")");
    }

    public abstract String getType();

    public abstract void writeBuf(FriendlyByteBuf buf);

    /** 工厂：根据 JSON type 字段分发*/
    public static QuestObjective fromJson(JsonObject o) {
        String type = o.get("type").getAsString();
        return switch (type) {
            case "kill"-> KillObjective.fromJson(o);
            case "collect"  -> CollectObjective.fromJson(o);
            case "dialogue" -> DialogueObjective.fromJson(o);
            default -> throw new IllegalArgumentException("Unknown objective type: " + type);
        };
    }

    public static QuestObjective fromBuf(FriendlyByteBuf buf) {
        String type = buf.readUtf();
        return switch (type) {
            case "kill"     -> KillObjective.fromBuf(buf);
            case "collect"  -> CollectObjective.fromBuf(buf);
            case "dialogue" -> DialogueObjective.fromBuf(buf);
            default -> throw new IllegalArgumentException("Unknown objective type: " + type);
        };
    }
}