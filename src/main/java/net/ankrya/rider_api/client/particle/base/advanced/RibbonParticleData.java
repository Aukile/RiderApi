package net.ankrya.rider_api.client.particle.base.advanced;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class RibbonParticleData extends AdvancedParticleData {

    private final int length;

    public RibbonParticleData(ParticleType<? extends RibbonParticleData> type, AdvancedParticleData data, int length){
        this(type, data.getRotation(), data.getScale(), data.getRed(), data.getGreen(), data.getBlue(), data.getAlpha(), data.getAirDrag(), data.getDuration(), data.isEmissive(), length);
    }

    public RibbonParticleData(ParticleType<? extends RibbonParticleData> type, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, int length) {
        this(type, rotation, scale, r, g, b, a, drag, duration, emissive, length, new ParticleComponent[0]);
    }

    public RibbonParticleData(ParticleType<? extends RibbonParticleData> type, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, int length, ParticleComponent[] components) {
        super(type, rotation, scale, r, g, b, a, drag, duration, emissive, false, components);
        this.length = length;
    }

    private static void writeToNetwork(FriendlyByteBuf buffer, RibbonParticleData data) {
        AdvancedParticleData.writeToNetwork(buffer, data);
        buffer.writeInt(data.getLength());
    }

    public String writeToString() {
        String var10000 = super.writeToString();
        return var10000 + " " + this.length;
    }

    @OnlyIn(Dist.CLIENT)
    public int getLength() {
        return this.length;
    }
}
