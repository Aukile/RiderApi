package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.client.particle.animation.AnimParticlePlayer;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public abstract class AnimParticleNet implements INMessage {
    final UUID player;
    final String animation;
    final String bone;

    public AnimParticleNet(UUID player, String animation, String bone) {
        this.player = player; this.animation = animation; this.bone = bone;
    }

    @Override public void toBytes(FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, player, animation, bone);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        if (ctx.getDirection().getReceptionSide().isClient())
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::startClient));
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void startClient() {
        new AnimParticlePlayer(
                player,
                new ResourceLocation(animation),
                bone,
                this::summonParticle
        );
    }

    protected abstract void summonParticle(Level level, Vec3 vec3) ;
}
