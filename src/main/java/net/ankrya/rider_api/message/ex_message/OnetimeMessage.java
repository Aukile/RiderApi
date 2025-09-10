package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class OnetimeMessage implements INMessage {
    final String name;
    GJ onetime;

    public OnetimeMessage(GJ onetime) {
        this.name = onetime.getClass().getName();
    }

    public OnetimeMessage(String name) {
        this.name = name;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, name);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            GJ onetime = getOnetime(name, ctx.getSender());
            onetime.use();
        });
    }

    private static GJ getOnetime(String name, Player player) {
        try {
            return (GJ) Class.forName(name).getDeclaredConstructor(Player.class).newInstance(player);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
