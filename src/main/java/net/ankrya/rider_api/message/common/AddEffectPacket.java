package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.effect.PlayerEffectConfig;
import net.ankrya.rider_api.effect.PlayerEffectManager;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/** 服务端 → 客户端：添加特效（ownerUUID 为 null 表示世界坐标特效） */
public record AddEffectPacket(@Nullable UUID ownerUUID, PlayerEffectConfig config) {

    public static AddEffectPacket decode(FriendlyByteBuf buf) {
        UUID owner = buf.readBoolean() ? buf.readUUID() : null;
        return new AddEffectPacket(owner, PlayerEffectConfig.decode(buf));
    }

    public static void encode(AddEffectPacket packet, FriendlyByteBuf buf) {
        // 原来直接 writeUUID，世界坐标特效（owner 为 null）会在发送时空指针
        buf.writeBoolean(packet.ownerUUID() != null);
        if (packet.ownerUUID() != null) buf.writeUUID(packet.ownerUUID());
        packet.config().encode(buf);
    }

    public static void handle(AddEffectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PlayerEffectManager.get().addEffect(packet.ownerUUID(), packet.config())));
        ctx.setPacketHandled(true);
    }
}
