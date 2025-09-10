package net.ankrya.rider_api.message.ex_message;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.ankrya.rider_api.compat.animation.PlayerAnimator;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PlayerAnimationMessage implements INMessage {
    final UUID uuid;
    final ResourceLocation layer;
    final String animation;
    final boolean showRightArm;
    final boolean showLeftArm;
    final boolean override;

    public PlayerAnimationMessage(UUID uuid, ResourceLocation layer, String animation, boolean showRightArm, boolean showLeftArm, boolean override) {
        this.uuid = uuid;
        this.layer = layer;
        this.animation = animation;
        this.showRightArm = showRightArm;
        this.showLeftArm = showLeftArm;
        this.override = override;
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, uuid, layer, animation, showRightArm, showLeftArm, override);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() ->{
            Player player = ctx.getSender().serverLevel().getPlayerByUUID(uuid);
            if (ctx.getDirection().getReceptionSide().isClient() && player instanceof AbstractClientPlayer clientPlayer) {
                playerAnimation(clientPlayer, layer, animation, showRightArm, showLeftArm, override);
            }
        });
    }

    public static void playerAnimation(AbstractClientPlayer player, ResourceLocation dataId, String animation, boolean showRightArm, boolean showLeftArm, boolean override){
        KeyframeAnimationPlayer keyframeAnimationPlayer = new KeyframeAnimationPlayer(Objects.requireNonNull(PlayerAnimationRegistry.getAnimation(ResourceLocation.parse(animation))))
                .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL).setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(showRightArm).setShowLeftItem(showLeftArm));
        PlayerAnimator.instance().playAnimation(player, dataId, keyframeAnimationPlayer, override);
    }
}
