package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.message.ex_message.PlayerAnimationMessage;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/*
 * 播放动画
 */
public class PlayAnimation implements CustomPacketPayload {
    public static final Type<PlayAnimation> TYPE = new Type<>(GJ.Easy.getApiResource("play_animation_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayAnimation> CODEC = StreamCodec.of(PlayAnimation::toBuf, PlayAnimation::fromBuf);

    final UUID uuid;
    final ResourceLocation layer;
    final String animation;
    final boolean showRightArm;
    final boolean showLeftArm;
    final boolean override;

    public PlayAnimation(UUID uuid, ResourceLocation layer, String animation, boolean showRightArm, boolean showLeftArm, boolean override) {
        this.uuid = uuid;
        this.layer = layer;
        this.animation = animation;
        this.showRightArm = showRightArm;
        this.showLeftArm = showLeftArm;
        this.override = override;
    }

    private static PlayAnimation fromBuf(RegistryFriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ResourceLocation layer = buf.readResourceLocation();
        String animation = buf.readUtf();
        boolean showRightArm = buf.readBoolean();
        boolean showLeftArm = buf.readBoolean();
        boolean override = buf.readBoolean();
        return new PlayAnimation(uuid, layer, animation, showRightArm, showLeftArm, override);
    }

    private static void toBuf(RegistryFriendlyByteBuf buf, PlayAnimation playAnimation) {
        buf.writeUUID(playAnimation.uuid);
        buf.writeResourceLocation(playAnimation.layer);
        buf.writeUtf(playAnimation.animation);
        buf.writeBoolean(playAnimation.showRightArm);
        buf.writeBoolean(playAnimation.showLeftArm);
        buf.writeBoolean(playAnimation.override);
    }

    public static void run(final PlayAnimation message, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = ctx.player().level();
            Player player = level.getPlayerByUUID(message.uuid);
            if (player instanceof AbstractClientPlayer clientPlayer) {
                PlayerAnimationMessage.playerAnimation(clientPlayer, message.layer, message.animation, message.showRightArm, message.showLeftArm, message.override);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
