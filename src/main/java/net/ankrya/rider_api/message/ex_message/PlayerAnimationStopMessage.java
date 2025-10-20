package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.compat.animation.PlayerAnimator;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerAnimationStopMessage implements INMessage {
    final UUID uuid;
    final ResourceLocation layer;
    final int fadeTime;

    public PlayerAnimationStopMessage(UUID uuid, ResourceLocation layer, int fadeTime) {
        this.uuid = uuid;
        this.layer = layer;
        this.fadeTime = fadeTime;
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, uuid, layer, fadeTime);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Level level = GJ.Easy.getLevel(ctx);
            if (level != null){
                Player player = level.getPlayerByUUID(uuid);
                stopAnimation(player, layer, fadeTime);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopAnimation(Player player, ResourceLocation dataId, int fadeTime){
        if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerAnimator.instance().stopAnimation(clientPlayer, dataId, fadeTime);
        }
    }
}
