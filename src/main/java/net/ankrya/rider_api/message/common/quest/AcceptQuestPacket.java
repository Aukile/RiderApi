package net.ankrya.rider_api.message.common.quest;

import net.ankrya.rider_api.quest.manager.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 客户端 → 服务端：接取任务（从日志界面点击接取按钮） */
public record AcceptQuestPacket(ResourceLocation questId) {

    public static void encode(AcceptQuestPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.questId());
    }

    public static AcceptQuestPacket decode(FriendlyByteBuf buf) {
        return new AcceptQuestPacket(buf.readResourceLocation());
    }

    public static void handle(AcceptQuestPacket pkt, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp != null)
                QuestManager.acceptQuest(sp, pkt.questId());
        });
        ctx.setPacketHandled(true);
    }
}
