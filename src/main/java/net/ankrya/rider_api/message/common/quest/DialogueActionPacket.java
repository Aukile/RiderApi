package net.ankrya.rider_api.message.common.quest;

import net.ankrya.rider_api.quest.data.QuestDefinition;
import net.ankrya.rider_api.quest.manager.QuestManager;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 客户端 → 服务端：对话完成/接受任务 */
public record DialogueActionPacket(ResourceLocation questId, Action action) {
    public enum Action { ACCEPT_QUEST, DIALOGUE_COMPLETE }

    public static void encode(DialogueActionPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.questId());
        buf.writeEnum(pkt.action());
    }

    public static DialogueActionPacket decode(FriendlyByteBuf buf) {
        return new DialogueActionPacket(buf.readResourceLocation(), buf.readEnum(Action.class));
    }

    public static void handle(DialogueActionPacket pkt, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            switch (pkt.action()) {
                case ACCEPT_QUEST     -> QuestManager.acceptQuest(sp, pkt.questId());
                // 原来把 questId 当成 npcId 传进去，对话目标永远匹配不上，对话任务无法完成
                case DIALOGUE_COMPLETE -> QuestManager.onDialogueComplete(
                        sp, pkt.questId(), QuestRegistry.INSTANCE.get(pkt.questId())
                                .map(QuestDefinition::npcId).orElse(null));
            }
        });
        ctx.setPacketHandled(true);
    }
}
