package net.ankrya.rider_api.quest.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CollectObjective extends QuestObjective {

    private final ResourceLocation itemId;
    private final int count;

    public CollectObjective(String id, String label, ResourceLocation itemId, int count) {
        super(id, label);
        this.itemId = itemId;
        this.count = count;
    }

    public ResourceLocation getItemId() { return itemId; }

    @Override public String getType() { return "collect"; }
    @Override public int getRequired() { return count; }
    @Override public int getCurrent(QuestProgress p) { return p.getCounter(id); }
    @Override public boolean isCompleted(QuestProgress p) { return getCurrent(p) >= count; }

    /** 扫描玩家背包，返回该物品当前总数 */
    public int countInInventory(Player player) {
        var itemOpt = BuiltInRegistries.ITEM.getOptional(itemId);
        if (itemOpt.isEmpty()) return 0;
        var item = itemOpt.get();
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        // 也算副手和盔甲槽（可选）
        for (ItemStack stack : player.getInventory().armor)
            if (stack.is(item)) total += stack.getCount();
        total += player.getInventory().offhand.stream()
                .filter(s -> s.is(item)).mapToInt(ItemStack::getCount).sum();
        return total;
    }

    @Override
    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeUtf("collect");
        buf.writeUtf(id);
        buf.writeUtf(label);
        buf.writeResourceLocation(itemId);
        buf.writeInt(count);
    }

    public static CollectObjective fromJson(JsonObject o) {
        return new CollectObjective(
                o.get("id").getAsString(),
                o.get("label").getAsString(),
                new ResourceLocation(o.get("item").getAsString()),
                o.get("count").getAsInt()
        );
    }

    public static CollectObjective fromBuf(FriendlyByteBuf buf) {
        return new CollectObjective(buf.readUtf(), buf.readUtf(),
                buf.readResourceLocation(), buf.readInt());
    }
}