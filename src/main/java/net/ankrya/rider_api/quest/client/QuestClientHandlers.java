package net.ankrya.rider_api.quest.client;

import net.ankrya.rider_api.message.common.quest.OpenDialoguePacket;
import net.ankrya.rider_api.message.common.quest.SyncQuestDataPacket;
import net.minecraft.client.Minecraft;

public final class QuestClientHandlers {
    public static void openDialogue(OpenDialoguePacket pkt) {
        Minecraft.getInstance().setScreen(new DialogueScreen(pkt));
    }

    public static void syncQuestData(SyncQuestDataPacket pkt) {
        ClientQuestCache.INSTANCE.update(pkt.data());
    }
}