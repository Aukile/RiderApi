package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuestReward {
    public final int xp;
    public final List<ItemStack> items;

    public QuestReward(int xp, List<ItemStack> items) {
        this.xp= xp;
        this.items = items;
    }

    public static QuestReward fromJson(JsonObject o) {
        int xp = o.has("xp") ? o.get("xp").getAsInt() : 0;
        List<ItemStack> items = new ArrayList<>();
        if (o.has("items")) {
            for (var el : o.getAsJsonArray("items")) {
                var io = el.getAsJsonObject();
                var item = BuiltInRegistries.ITEM
                        .getOptional(new ResourceLocation(io.get("item").getAsString()))
                        .orElse(null);
                if (item != null) {
                    int cnt = io.has("count") ? io.get("count").getAsInt() : 1;
                    items.add(new ItemStack(item, cnt));
                }
            }
        }
        return new QuestReward(xp, items);
    }

    public static final QuestReward EMPTY = new QuestReward(0, List.of());
}