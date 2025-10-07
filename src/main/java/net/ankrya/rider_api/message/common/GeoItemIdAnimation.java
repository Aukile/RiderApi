package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class GeoItemIdAnimation implements INMessage {
    final ItemStack itemStack;
    final String animation;
    final UUID uuid;

    public GeoItemIdAnimation(ItemStack itemStack, String animation, UUID uuid) {
        this.itemStack = itemStack;
        this.animation = animation;
        this.uuid = uuid;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, itemStack, animation, uuid);
    }

    @Override
    public void run(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level() instanceof ServerLevel serverLevel){
                Entity entity = serverLevel.getEntity(uuid);
                if (itemStack.getItem() instanceof IGeoItem item)
                    item.triggerAnim(entity, GeoItem.getId(itemStack), "controller", animation);
            }
        });
    }
}
