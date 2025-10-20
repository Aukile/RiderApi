package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
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
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()){
                Level level = GJ.Easy.getLevel(context);
                update(message, level);
            }
        });
        context.setPacketHandled(true);
    }

    private static void update(SyncVariableMessage message, Level level) {
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
    }
}