package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 数据同步的发包<br>
 * 因为需要使用自己的序列号和反序列化器<br>
 * 所以得写个正常的发包awa
 */
public class SyncVariableMessage implements CustomPacketPayload {
    public static final Type<SyncVariableMessage> TYPE = new Type<>(GJ.Easy.getApiResource("sync_variable_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVariableMessage> CODEC = StreamCodec.of(SyncVariableMessage::toBuf, SyncVariableMessage::fromBuf);

    final int id;
    final Variables variables;

    public SyncVariableMessage(int id, Variables variables) {
        this.id = id;
        this.variables = variables;
    }

    private static SyncVariableMessage fromBuf(RegistryFriendlyByteBuf buf) {
        int target = buf.readInt();
        SyncVariableMessage message = new SyncVariableMessage(target, new Variables());
        message.variables.deserializeNBT(buf.registryAccess(), (ListTag) buf.readNbt(NbtAccounter.create(2097152L)));
        return message;
    }

    private static void toBuf(RegistryFriendlyByteBuf buf, SyncVariableMessage message) {
        buf.writeInt(message.id);
        buf.writeNbt(message.variables.serializeNBT(buf.registryAccess()));
    }

    public static void handle(final SyncVariableMessage message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND && message.variables != null) {
            if (message.id >= 0){
                Player player = context.player();
                context.enqueueWork(() -> player.getData(Variables.VARIABLES)
                        .deserializeNBT(player.registryAccess(), message.variables
                                .serializeNBT(player.registryAccess()))).exceptionally(e -> {
                    context.connection().disconnect(Component.literal(e.getMessage()));
                    return null;
                });
            } else {
                try (Level level = context.player().level()) {
                    context.enqueueWork(() -> level.getData(Variables.VARIABLES)
                            .deserializeNBT(level.registryAccess(), message.variables
                                    .serializeNBT(level.registryAccess()))).exceptionally(e -> {
                        context.connection().disconnect(Component.literal(e.getMessage()));
                        return null;
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
