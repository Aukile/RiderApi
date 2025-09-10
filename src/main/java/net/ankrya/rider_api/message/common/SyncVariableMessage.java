package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.data.Variables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 数据同步的发包<br>
 * 因为需要使用自己的序列号和反序列化器<br>
 * 所以得写个正常的发包awa
 */
public class SyncVariableMessage{
    final int id;
    final Variables variables;

    public SyncVariableMessage(int id, Variables variables) {
        this.id = id;
        this.variables = variables;
    }

    private static SyncVariableMessage fromBuf(FriendlyByteBuf buf) {
        int target = buf.readInt();
        SyncVariableMessage message = new SyncVariableMessage(target, new Variables());
        message.variables.deserializeNBT((ListTag) Objects.requireNonNull(buf.readNbt(NbtAccounter.UNLIMITED)).get("variables"));
        return message;
    }

    private static void toBuf(FriendlyByteBuf buf, SyncVariableMessage message) {
        buf.writeInt(message.id);
        ListTag listTag = message.variables.serializeNBT();
        CompoundTag tag = new CompoundTag();
        tag.put("variables", listTag);
        buf.writeNbt(tag);
    }

    public static void handle(final SyncVariableMessage message, final Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerLevel serverLevel = context.getSender().serverLevel();
        Player player = context.getSender().getId() != message.id ? (Player) serverLevel.getEntity(message.id) : context.getSender();
        if (context.getDirection().getReceptionSide().isClient() && message.variables != null) {
            if (message.id >= 0){
                context.enqueueWork(() -> Variables.getCapability(player)
                        .deserializeNBT(message.variables.serializeNBT())).exceptionally(e -> {
                            context.getNetworkManager().disconnect(Component.literal(e.getMessage()));
                    return null;
                });
            } else {
                Level level = context.getSender().serverLevel();
                context.enqueueWork(() ->  Variables.getCapability(level)
                        .deserializeNBT(message.variables.serializeNBT())).exceptionally(e -> {
                            context.getNetworkManager().disconnect(Component.literal(e.getMessage()));
                    return null;
                });
            }
        }
        context.setPacketHandled(true);
    }
}
