package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.interfaces.message.INMessage;
import net.ankrya.rider_api.interfaces.inside_use.ISoundMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class StopLoopSound implements INMessage {
    private final int id;
    private final int type;
    private final ResourceLocation location;

    public StopLoopSound(int id, int type, ResourceLocation location) {
        this.id = id;
        this.type = type;
        this.location = location;
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, id, type, location);
    }

    @Override
    public void run(IPayloadContext ctx) {
        ctx.enqueueWork(() ->{
            Level level = ctx.player().level();
            Entity entity = level.getEntity(id);
            if (entity instanceof ISoundMap soundMap && soundMap.rider$containsLoopSound(location))
                soundMap.rider$removeLoopSound(location);
            stopSound(Minecraft.getInstance(), this);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopSound(Minecraft instance, StopLoopSound message) {
        instance.getSoundManager().stop(message.location, SoundSource.PLAYERS);
    }
}
