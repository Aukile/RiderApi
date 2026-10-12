package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.interfaces.geo.GeoItemAnimData;
import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.ankrya.rider_api.message.MessageLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;
import java.util.function.Supplier;

public class GeoItemIdAnimation {
    final ItemStack itemStack;
    final String animation;
    final UUID uuid;

    public GeoItemIdAnimation(ItemStack itemStack, String animation, UUID uuid) {
        this.itemStack = itemStack;
        this.animation = animation;
        this.uuid = uuid;
    }

    public static void toBuf(GeoItemIdAnimation message, FriendlyByteBuf buf){
        buf.writeItem(message.itemStack);
        buf.writeUtf(message.animation);
        buf.writeUUID(message.uuid);
    }

    public static GeoItemIdAnimation fromBuf(FriendlyByteBuf buf){
        ItemStack itemStack = buf.readItem();
        String animation = buf.readUtf();
        UUID uuid = buf.readUUID();
        return new GeoItemIdAnimation(itemStack, animation, uuid);
    }

    public static void handle(GeoItemIdAnimation message, Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection().getReceptionSide().isServer()){
            context.enqueueWork(() -> {
                if (context.getSender() == null) return;
                ServerLevel serverLevel = context.getSender().serverLevel();
                Entity entity = serverLevel.getEntity(message.uuid);
                if (entity == null || !(message.itemStack.getItem() instanceof IGeoItem item)) return;
                long id = GeoItem.getId(message.itemStack);
                if (id == Long.MAX_VALUE) return; // 物品还没有分配动画 id
                // 只允许触发实体身上真实存在的物品，并把动画保存到那个物品上
                ItemStack stack = findStack(entity, message.itemStack.getItem(), id);
                if (stack.isEmpty()) return;
                GeoItemAnimData.set(stack, message.animation);
                item.triggerAnim(entity, id, "controller", message.animation);
            });
        } else {
            context.enqueueWork(() -> MessageLoader.getApiLoader().sendToServer(new GeoItemIdAnimation(message.itemStack, message.animation, message.uuid)));
        }
    }

    private static ItemStack findStack(Entity entity, Item item, long id) {
        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(item) && GeoItem.getId(stack) == id) return stack;
            }
        } else if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = living.getItemBySlot(slot);
                if (stack.is(item) && GeoItem.getId(stack) == id) return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
