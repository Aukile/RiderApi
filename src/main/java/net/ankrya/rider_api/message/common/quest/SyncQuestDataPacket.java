package net.ankrya.rider_api.message.common.quest;

import net.ankrya.rider_api.quest.client.QuestClientHandlers;
import net.ankrya.rider_api.quest.data.PlayerQuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncQuestDataPacket(PlayerQuestData data) {

    public static void encode(SyncQuestDataPacket pkt, FriendlyByteBuf buf) {
        pkt.data().writeBuf(buf);
    }

    public static SyncQuestDataPacket decode(FriendlyByteBuf buf) {
        return new SyncQuestDataPacket(PlayerQuestData.fromBuf(buf));
    }

    public static void handle(SyncQuestDataPacket pkt, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        if (ctx.getDirection().getReceptionSide().isClient())
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> QuestClientHandlers.syncQuestData(pkt)));
        ctx.setPacketHandled(true);
    }
}
