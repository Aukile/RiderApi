package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.data.Variables;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 数据同步的发包<br>
 * 因为需要使用自己的序列号和反序列化器<br>
 * 所以得写个正常的发包awa
 */
public class SyncVariableMessage {
    final int id;
    final Variables variables;

    public SyncVariableMessage(int id, Variables variables) {
        this.id = id;
        this.variables = variables;
    }

    public static void encode(SyncVariableMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.id);
        ListTag listTag = message.variables.serializeNBT();
        CompoundTag tag = new CompoundTag();
        tag.put("variables", listTag);
        buf.writeNbt(tag);
    }

    public static SyncVariableMessage decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        CompoundTag tag = buf.readNbt();
        Variables variables = new Variables();
        if (tag != null && tag.contains("variables")) {
            variables.deserializeNBT(tag.getList("variables", 10));
        }
        return new SyncVariableMessage(id, variables);
    }

    public static void handle(final SyncVariableMessage message, final Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                Level level = sender == null ? Minecraft.getInstance().level : sender.level();
                if (level == null) return;
                if (message.id >= 0) {
                    Entity entity = level.getEntity(message.id);
                    if (entity != null) {
                        entity.getCapability(Variables.VARIABLES).ifPresent(cap ->
                                cap.deserializeNBT(message.variables.serializeNBT()));
                    }
                } else {
                    level.getCapability(Variables.VARIABLES).ifPresent(cap ->
                            cap.deserializeNBT(message.variables.serializeNBT()));
                }
            });
        }

        context.setPacketHandled(true);
    }
}