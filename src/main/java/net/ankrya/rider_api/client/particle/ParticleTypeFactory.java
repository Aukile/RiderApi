package net.ankrya.rider_api.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleComponent;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleRotation;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class ParticleTypeFactory {
    public static Codec<AdvancedParticleData> CODEC(ParticleType<AdvancedParticleData> particleType) {
        return RecordCodecBuilder.create((codecBuilder) -> codecBuilder.group(Codec.DOUBLE.fieldOf("scale").forGetter(AdvancedParticleData::getScale), Codec.DOUBLE.fieldOf("r").forGetter(AdvancedParticleData::getRed), Codec.DOUBLE.fieldOf("g").forGetter(AdvancedParticleData::getGreen), Codec.DOUBLE.fieldOf("b").forGetter(AdvancedParticleData::getBlue), Codec.DOUBLE.fieldOf("a").forGetter(AdvancedParticleData::getAlpha), Codec.DOUBLE.fieldOf("drag").forGetter(AdvancedParticleData::getAirDrag), Codec.DOUBLE.fieldOf("duration").forGetter(AdvancedParticleData::getDuration), Codec.BOOL.fieldOf("emissive").forGetter(AdvancedParticleData::isEmissive), Codec.BOOL.fieldOf("canCollide").forGetter(AdvancedParticleData::getCanCollide)).apply(codecBuilder, (scale, r, g, b, a, drag, duration, emissive, canCollide) -> new AdvancedParticleData(particleType, new ParticleRotation.FaceCamera(0.0F), scale, r, g, b, a, drag, duration, emissive, canCollide, new ParticleComponent[0])));
    }

    public static Codec<RibbonParticleData> CODEC_RIBBON(ParticleType<RibbonParticleData> particleType) {
        return RecordCodecBuilder.create((codecBuilder) -> codecBuilder.group(Codec.DOUBLE.fieldOf("scale").forGetter(AdvancedParticleData::getScale), Codec.DOUBLE.fieldOf("r").forGetter(AdvancedParticleData::getRed), Codec.DOUBLE.fieldOf("g").forGetter(AdvancedParticleData::getGreen), Codec.DOUBLE.fieldOf("b").forGetter(AdvancedParticleData::getBlue), Codec.DOUBLE.fieldOf("a").forGetter(AdvancedParticleData::getAlpha), Codec.DOUBLE.fieldOf("drag").forGetter(AdvancedParticleData::getAirDrag), Codec.DOUBLE.fieldOf("duration").forGetter(AdvancedParticleData::getDuration), Codec.BOOL.fieldOf("emissive").forGetter(AdvancedParticleData::isEmissive), Codec.INT.fieldOf("length").forGetter(RibbonParticleData::getLength)).apply(codecBuilder, (scale, r, g, b, a, drag, duration, emissive, length) -> new RibbonParticleData(particleType, new ParticleRotation.FaceCamera(0.0F), scale, r, g, b, a, drag, duration, emissive, length, new ParticleComponent[0])));
    }

    public static ParticleType<AdvancedParticleData> createParticleType() {
        return new ParticleType<>(false, AdvancedParticleData.DESERIALIZER) {
            @Override
            public Codec<AdvancedParticleData> codec() {
                return CODEC(this);
            }
        };
    }

    public static ParticleType<RibbonParticleData> createRibbonParticleType() {
        return new ParticleType<>(false, RibbonParticleData.DESERIALIZER) {
            @Override
            public @NotNull Codec<RibbonParticleData> codec() {
                return CODEC_RIBBON(this);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static ParticleType<RibbonParticleData> getRibbonParticleType() {
        return (ParticleType<RibbonParticleData>) ApiRegister.get().getRegisterObject("ribbon_particle", ParticleType.class).get();
    }

    @SuppressWarnings("unchecked")
    public static ParticleType<AdvancedParticleData> getParticleType() {
        return (ParticleType<AdvancedParticleData>) ApiRegister.get().getRegisterObject("advanced_particle", ParticleType.class).get();
    }
}
