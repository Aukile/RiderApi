package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.help.advancement.AdvancementEditorScreen;
import net.ankrya.rider_api.interfaces.message.IFMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenAdvancementEditorScreen implements IFMessage {
    @Override
    public void run(NetworkEvent.Context ctx) {
        if (ctx.getDirection().getReceptionSide().isClient())
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OpenAdvancementEditorScreen::open));
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void open() {
        Minecraft.getInstance().setScreen(new AdvancementEditorScreen());
    }
}
