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
        ctx.enqueueWork(() -> getOnetime(name, GJ.Easy.getPlayer(ctx)).use());
    }

    private static GJ getOnetime(String name, Player player) {
        try {
            // 安全检查：只允许实例化 GJ 的子类
            Class<?> clazz = Class.forName(name, false, OnetimeMessage.class.getClassLoader());
            if (!GJ.class.isAssignableFrom(clazz))
                throw new IllegalArgumentException("Not a GJ: " + name);
            return (GJ) clazz.getDeclaredConstructor(Player.class).newInstance(player);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
