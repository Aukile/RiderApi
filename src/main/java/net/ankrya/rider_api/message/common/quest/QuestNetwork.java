package net.ankrya.rider_api.message.common.quest;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.quest.registry.QuestAttachments;
import net.minecraft.server.level.ServerPlayer;

public class QuestNetwork {
    public static void syncToPlayer(ServerPlayer player) {
        var data = QuestAttachments.get(player);
        MessageLoader.getApiLoader().sendToPlayer(new SyncQuestDataPacket(data), player);
    }
}
