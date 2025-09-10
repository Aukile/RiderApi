package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.client.sound.LoopSound;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.ankrya.rider_api.interfaces.inside_use.ISoundMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

// TODO 测试中发现播放多次后播放会瞬间停掉
public class PlayLoopSound implements INMessage {
    public static final int MASTER = 0;
    public static final int MUSIC = 1;
    public static final int RECORDS = 2;
    public static final int WEATHER = 3;
    public static final int BLOCKS = 4;
    public static final int HOSTILE = 5;
    public static final int NEUTRAL = 6;
    public static final int PLAYERS = 7;
    public static final int AMBIENT = 8;
    public static final int VOICE = 9;


    final ResourceLocation sound;
    final boolean loop;
    final int range;
    final int type;
    final int id;

    public PlayLoopSound(ResourceLocation sound, boolean loop, int range, int type, int id) {
        this.sound = sound;
        this.loop = loop;
        this.range = range;
        this.type = type;
        this.id = id;
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, sound, loop, range, type, id);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Level level = ctx.getSender().serverLevel();
            Entity entity = level.getEntity(id);
            LoopSound loopSound = new LoopSound(entity, sound, getSoundSource(type), loop, range);
            if (loop && entity instanceof ISoundMap soundMap)
                soundMap.rider$addLoopSound(sound, loopSound);
            playLoopSound(Minecraft.getInstance(), loopSound);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void playLoopSound(Minecraft minecraft, LoopSound sound) {
        minecraft.getSoundManager().play(sound);
    }

    public static SoundSource getSoundSource(int type) {
        return switch (type) {
            case MASTER -> SoundSource.MASTER;
            case MUSIC -> SoundSource.MUSIC;
            case RECORDS -> SoundSource.RECORDS;
            case WEATHER -> SoundSource.WEATHER;
            case BLOCKS -> SoundSource.BLOCKS;
            case HOSTILE -> SoundSource.HOSTILE;
            case NEUTRAL -> SoundSource.NEUTRAL;
            case AMBIENT -> SoundSource.AMBIENT;
            case VOICE -> SoundSource.VOICE;
            default -> SoundSource.PLAYERS;
        };
    }
}
