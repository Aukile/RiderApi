package net.ankrya.rider_api.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleComponent;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleRotation;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ParticleTypeFactory {
    private static StreamCodec<RegistryFriendlyByteBuf, AdvancedParticleData> deserializer(ParticleType<AdvancedParticleData> type){
        return StreamCodec.of(AdvancedParticleData::writeToNetwork, buf -> readFromNetwork(buf, type));
    }

    private static StreamCodec<RegistryFriendlyByteBuf, RibbonParticleData> deserializerRibbon(ParticleType<RibbonParticleData> type){
        return StreamCodec.of(RibbonParticleData::writeToNetwork, buf -> readRibbonFromNetwork(buf, type));
    }

    public static AdvancedParticleData readFromNetwork(RegistryFriendlyByteBuf buffer, ParticleType<AdvancedParticleData> type) {
        float airDrag = buffer.readFloat();
        float red = buffer.readFloat();
        float green = buffer.readFloat();
        float blue = buffer.readFloat();
        float alpha = buffer.readFloat();
        String rotationMode = buffer.readUtf();
        float scale = buffer.readFloat();
        float yaw = buffer.readFloat();
        float pitch = buffer.readFloat();
        float roll = buffer.readFloat();
        boolean emissive = buffer.readBoolean();
        float duration = buffer.readFloat();
        float faceCameraAngle = buffer.readFloat();
        boolean canCollide = buffer.readBoolean();

        ParticleRotation rotation = switch (rotationMode) {
            case "face_camera" -> new ParticleRotation.FaceCamera(faceCameraAngle);
            case "euler" -> new ParticleRotation.EulerAngles(yaw, pitch, roll);
            default -> new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));
        };

        return new AdvancedParticleData(
                type, rotation, scale, red, green, blue, alpha,
                airDrag, duration, emissive, canCollide
        );
    }

    private static RibbonParticleData readRibbonFromNetwork(RegistryFriendlyByteBuf buffer, ParticleType<RibbonParticleData> type) {
        AdvancedParticleData advancedParticleData = readFromNetwork(buffer,null);

        int length = buffer.readInt();
        return new RibbonParticleData(type, advancedParticleData, length);
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

    public static ParticleType<AdvancedParticleData> createParticleType() {
        return new ParticleType<>(false) {
            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, AdvancedParticleData> streamCodec() {
                return deserializer(this);
            }

            @Override
            public @NotNull MapCodec<AdvancedParticleData> codec() {
                return AdvancedParticleData.codec(this);
            }
        };
    }

    public static ParticleType<RibbonParticleData> createRibbonParticleType() {
        return new ParticleType<>(false) {
            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, RibbonParticleData> streamCodec() {
                return deserializerRibbon(this);
            }

            @Override
            public @NotNull MapCodec<RibbonParticleData> codec() {
                return codecRibbon(this);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static ParticleType<AdvancedParticleData> getParticleType() {
        return (ParticleType<AdvancedParticleData>) ApiRegister.get().getRegisterObject("advanced_particle", ParticleType.class).get();
    }

    @SuppressWarnings("unchecked")
    public static ParticleType<RibbonParticleData> getRibbonParticleType() {
        return (ParticleType<RibbonParticleData>) ApiRegister.get().getRegisterObject("ribbon_particle", ParticleType.class).get();
    }
}
