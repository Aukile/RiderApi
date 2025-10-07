package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.Objects;
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
                ServerLevel serverLevel = Objects.requireNonNull(context.getSender()).serverLevel();
                Entity entity = serverLevel.getEntity(message.uuid);
                if (message.itemStack.getItem() instanceof IGeoItem item)
                    item.triggerAnim(entity, GeoItem.getId(message.itemStack), "controller", message.animation);
            });
        }
    }
}
