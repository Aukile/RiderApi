package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.effect.PlayerEffectManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/** 服务端 → 客户端：移除特效（effectId=null 则移除全部；ownerUUID 为 null 表示世界坐标特效） */
public record RemoveEffectPacket(@Nullable UUID ownerUUID, @Nullable String effectId) {

    public static RemoveEffectPacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readBoolean() ? buf.readUUID() : null;
        String id = buf.readBoolean() ? buf.readUtf() : null;
        return new RemoveEffectPacket(uuid, id);
    }

    public static void encode(RemoveEffectPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.ownerUUID() != null);
        if (packet.ownerUUID() != null) buf.writeUUID(packet.ownerUUID());
        buf.writeBoolean(packet.effectId() != null);
        if (packet.effectId() != null) buf.writeUtf(packet.effectId());
    }

    public static void handle(RemoveEffectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PlayerEffectManager.get().removeEffect(packet.ownerUUID(), packet.effectId())));
        ctx.setPacketHandled(true);
    }
}
