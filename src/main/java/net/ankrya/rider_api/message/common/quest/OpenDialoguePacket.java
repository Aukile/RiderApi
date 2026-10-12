package net.ankrya.rider_api.message.common.quest;

import net.ankrya.rider_api.quest.client.QuestClientHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/** 服务端→ 客户端：打开对话界面 */
public record OpenDialoguePacket(
        ResourceLocation npcId,
        ResourceLocation questId,   // 当前关联任务，null 表示无任务（打招呼）
        List<String> lines,
        boolean isGreeting,          // true = 打招呼, false = 任务对话
        @Nullable ResourceLocation guiStyleId
) {
    public static void encode(OpenDialoguePacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.npcId());
        buf.writeBoolean(pkt.questId() != null);
        if (pkt.questId() != null) buf.writeResourceLocation(pkt.questId());
        buf.writeCollection(pkt.lines(), FriendlyByteBuf::writeUtf);
        buf.writeBoolean(pkt.isGreeting());
        buf.writeBoolean(pkt.guiStyleId() != null);
        if (pkt.guiStyleId() != null) buf.writeResourceLocation(pkt.guiStyleId());
    }

    public static OpenDialoguePacket decode(FriendlyByteBuf buf) {
        ResourceLocation npcId = buf.readResourceLocation();
        ResourceLocation questId = buf.readBoolean() ? buf.readResourceLocation() : null;
        List<String> lines = buf.readList(FriendlyByteBuf::readUtf);
        boolean greeting = buf.readBoolean();
        ResourceLocation styleId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new OpenDialoguePacket(npcId, questId, lines, greeting, styleId);
    }

    public static void handle(OpenDialoguePacket pkt, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        if (ctx.getDirection().getReceptionSide().isClient())
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> QuestClientHandlers.openDialogue(pkt)));
        ctx.setPacketHandled(true);
    }
}
