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

    public static ParticleType<RibbonParticleData> createRibbonParticleType() {
        return new ParticleType<>(false) {
            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, RibbonParticleData> streamCodec() {
                return RibbonParticleData.deserializerRibbon(this);
            }

            @Override
            public @NotNull MapCodec<RibbonParticleData> codec() {
                return codecRibbon(this);
            }
        };
    }

    private static StreamCodec<RegistryFriendlyByteBuf, RibbonParticleData> deserializerRibbon(ParticleType<RibbonParticleData> type){
        return StreamCodec.of(RibbonParticleData::writeToNetwork, buf -> RibbonParticleData.readRibbonFromNetwork(buf, type));
    }

    private static RibbonParticleData readRibbonFromNetwork(RegistryFriendlyByteBuf buffer, ParticleType<RibbonParticleData> type) {
        AdvancedParticleData advancedParticleData = AdvancedParticleData.readFromNetwork(buffer,null);

        int length = buffer.readInt();
        return new RibbonParticleData(type, advancedParticleData, length);
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

    private static MapCodec<RibbonParticleData> codecRibbon(ParticleType<RibbonParticleData> particleType) {
        return RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.DOUBLE.fieldOf("scale").forGetter(AdvancedParticleData::getScale),
                Codec.DOUBLE.fieldOf("r").forGetter(AdvancedParticleData::getRed),
                Codec.DOUBLE.fieldOf("g").forGetter(AdvancedParticleData::getGreen),
                Codec.DOUBLE.fieldOf("b").forGetter(AdvancedParticleData::getBlue),
                Codec.DOUBLE.fieldOf("a").forGetter(AdvancedParticleData::getAlpha),
                Codec.DOUBLE.fieldOf("drag").forGetter(AdvancedParticleData::getAirDrag),
                Codec.DOUBLE.fieldOf("duration").forGetter(AdvancedParticleData::getDuration),
                Codec.BOOL.fieldOf("emissive").forGetter(AdvancedParticleData::isEmissive),
                Codec.INT.fieldOf("length").forGetter(RibbonParticleData::getLength)
        ).apply(instance, (scale, r, g, b, a, drag, duration, emissive, length) -> new RibbonParticleData(
                particleType,
                new ParticleRotation.FaceCamera(0.0F),
                scale, r, g, b, a,
                drag, duration, emissive, length,
                new ParticleComponent[0]
        )));
    }

    @SuppressWarnings("unchecked")
    public static ParticleType<RibbonParticleData> getRibbonParticleType() {
        return (ParticleType<RibbonParticleData>) ApiRegister.getRegisterObject("ribbon_particle", ParticleType.class).get();
    }
}
