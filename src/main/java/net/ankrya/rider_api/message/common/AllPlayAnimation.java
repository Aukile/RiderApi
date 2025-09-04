package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.help.GJ;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/*
 * 是播放动画的CTSTC包
 */
public class AllPlayAnimation implements CustomPacketPayload {
    public static final Type<AllPlayAnimation> TYPE = new Type<>(GJ.Easy.getApiResource("all_play_animation_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AllPlayAnimation> CODEC = StreamCodec.of(AllPlayAnimation::toBuf, AllPlayAnimation::fromBuf);

    final UUID uuid;
    final ResourceLocation layer;
    final String animation;
    final boolean showRightArm;
    final boolean showLeftArm;
    final boolean override;

    public AllPlayAnimation(PlayAnimation playAnimation) {
        this(playAnimation.uuid, playAnimation.layer, playAnimation.animation, playAnimation.showRightArm, playAnimation.showLeftArm, playAnimation.override);
    }

    public AllPlayAnimation(UUID uuid, ResourceLocation layer, String animation, boolean showRightArm, boolean showLeftArm, boolean override) {
        this.uuid = uuid;
        this.layer = layer;
        this.animation = animation;
        this.showRightArm = showRightArm;
        this.showLeftArm = showLeftArm;
        this.override = override;
    }

    private static AllPlayAnimation fromBuf(RegistryFriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ResourceLocation layer = buf.readResourceLocation();
        String animation = buf.readUtf();
        boolean showRightArm = buf.readBoolean();
        boolean showLeftArm = buf.readBoolean();
        boolean override = buf.readBoolean();
        System.out.println("AllPlayAnimation read");
        return new AllPlayAnimation(uuid, layer, animation, showRightArm, showLeftArm, override);
    }

    private static void toBuf(RegistryFriendlyByteBuf buf, AllPlayAnimation playAnimation) {
        buf.writeUUID(playAnimation.uuid);
        buf.writeResourceLocation(playAnimation.layer);
        buf.writeUtf(playAnimation.animation);
        buf.writeBoolean(playAnimation.showRightArm);
        buf.writeBoolean(playAnimation.showLeftArm);
        buf.writeBoolean(playAnimation.override);
        System.out.println("AllPlayAnimation write");
    }

    public static void run(final AllPlayAnimation message, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ctx.reply(new PlayAnimation(message.uuid, message.layer, message.animation, message.showRightArm, message.showLeftArm, message.override));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
