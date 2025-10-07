package net.ankrya.rider_api.message.common;

import net.ankrya.rider_api.client.sound.LoopSound;
import net.ankrya.rider_api.message.ex_message.PlayLoopSound;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static net.ankrya.rider_api.message.ex_message.PlayLoopSound.getSoundSource;

/**
 * 播放声音的网络包<br>
 * (常用的还是做成正常规格的好一点？
 */
public class LoopSoundMessage {

    final ResourceLocation sound;
    final boolean loop;
    final int range;
    final int type;
    final int id;

    public LoopSoundMessage(ResourceLocation sound, boolean loop, int range, int type, int id) {
        this.sound = sound;
        this.loop = loop;
        this.range = range;
        this.type = type;
        this.id = id;
    }

    public static void toBuf(LoopSoundMessage loopSoundMessage, FriendlyByteBuf buf) {
        buf.writeResourceLocation(loopSoundMessage.sound);
        buf.writeBoolean(loopSoundMessage.loop);
        buf.writeInt(loopSoundMessage.range);
        buf.writeInt(loopSoundMessage.type);
        buf.writeInt(loopSoundMessage.id);
    }

    public static LoopSoundMessage fromBuf(FriendlyByteBuf buf) {
        final ResourceLocation sound = buf.readResourceLocation();
        final boolean loop = buf.readBoolean();
        final int range = buf.readInt();
        final int type = buf.readInt();
        final int id = buf.readInt();
        return new LoopSoundMessage(sound, loop, range, type, id);
    }

    public static void handle(LoopSoundMessage message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            playSound(message, sender == null ? null : sender.level());
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void playSound(LoopSoundMessage message, Level level) {
        if (level == null)
            level = Minecraft.getInstance().level;
        PlayLoopSound.playLoopSound(Minecraft.getInstance(), new LoopSound(level.getEntity(message.id), message.sound, getSoundSource(message.type), message.loop, message.range));
    }

    public ResourceLocation getSound() {
        return sound;
    }
}
