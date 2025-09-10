package net.ankrya.rider_api.client.particle.base.advanced;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class RibbonParticleData extends AdvancedParticleData {
    public static final ParticleOptions.Deserializer<RibbonParticleData> DESERIALIZER = new ParticleOptions.Deserializer<RibbonParticleData>() {
        public RibbonParticleData fromCommand(ParticleType<RibbonParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double airDrag = reader.readDouble();
            reader.expect(' ');
            double red = reader.readDouble();
            reader.expect(' ');
            double green = reader.readDouble();
            reader.expect(' ');
            double blue = reader.readDouble();
            reader.expect(' ');
            double alpha = reader.readDouble();
            reader.expect(' ');
            String rotationMode = reader.readString();
            reader.expect(' ');
            double scale = reader.readDouble();
            reader.expect(' ');
            double yaw = reader.readDouble();
            reader.expect(' ');
            double pitch = reader.readDouble();
            reader.expect(' ');
            double roll = reader.readDouble();
            reader.expect(' ');
            boolean emissive = reader.readBoolean();
            reader.expect(' ');
            double duration = reader.readDouble();
            reader.expect(' ');
            reader.readDouble();
            reader.expect(' ');
            int length = reader.readInt();
            Object rotation;
            if (rotationMode.equals("face_camera")) {
                rotation = new ParticleRotation.FaceCamera(0.0F);
            } else if (rotationMode.equals("euler")) {
                rotation = new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            } else {
                rotation = new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));
            }

            return new RibbonParticleData(particleTypeIn, (ParticleRotation)rotation, scale, red, green, blue, alpha, airDrag, duration, emissive, length);
        }

        public RibbonParticleData fromNetwork(ParticleType<RibbonParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            double airDrag = (double)buffer.readFloat();
            double red = (double)buffer.readFloat();
            double green = (double)buffer.readFloat();
            double blue = (double)buffer.readFloat();
            double alpha = (double)buffer.readFloat();
            String rotationMode = buffer.readUtf();
            double scale = (double)buffer.readFloat();
            double yaw = (double)buffer.readFloat();
            double pitch = (double)buffer.readFloat();
            double roll = (double)buffer.readFloat();
            boolean emissive = buffer.readBoolean();
            double duration = (double)buffer.readFloat();
            buffer.readFloat();
            int length = buffer.readInt();
            Object rotation;
            if (rotationMode.equals("face_camera")) {
                rotation = new ParticleRotation.FaceCamera(0.0F);
            } else if (rotationMode.equals("euler")) {
                rotation = new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            } else {
                rotation = new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));
            }

            return new RibbonParticleData(particleTypeIn, (ParticleRotation)rotation, scale, red, green, blue, alpha, airDrag, duration, emissive, length);
        }
    };
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

    public String writeToString() {
        String var10000 = super.writeToString();
        return var10000 + " " + this.length;
    }

    @OnlyIn(Dist.CLIENT)
    public int getLength() {
        return this.length;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        super.writeToNetwork(buffer);
        buffer.writeInt(this.length);
    }

    public static Codec<RibbonParticleData> CODEC_RIBBON(ParticleType<RibbonParticleData> particleType) {
        return RecordCodecBuilder.create((codecBuilder) -> codecBuilder.group(Codec.DOUBLE.fieldOf("scale").forGetter(AdvancedParticleData::getScale), Codec.DOUBLE.fieldOf("r").forGetter(AdvancedParticleData::getRed), Codec.DOUBLE.fieldOf("g").forGetter(AdvancedParticleData::getGreen), Codec.DOUBLE.fieldOf("b").forGetter(AdvancedParticleData::getBlue), Codec.DOUBLE.fieldOf("a").forGetter(AdvancedParticleData::getAlpha), Codec.DOUBLE.fieldOf("drag").forGetter(AdvancedParticleData::getAirDrag), Codec.DOUBLE.fieldOf("duration").forGetter(AdvancedParticleData::getDuration), Codec.BOOL.fieldOf("emissive").forGetter(AdvancedParticleData::isEmissive), Codec.INT.fieldOf("length").forGetter(RibbonParticleData::getLength)).apply(codecBuilder, (scale, r, g, b, a, drag, duration, emissive, length) -> new RibbonParticleData(particleType, new ParticleRotation.FaceCamera(0.0F), scale, r, g, b, a, drag, duration, emissive, length, new ParticleComponent[0])));
    }

    @SuppressWarnings("unchecked")
    public static ParticleType<RibbonParticleData> getRibbonParticleType() {
        return (ParticleType<RibbonParticleData>) ApiRegister.getRegisterObject("ribbon_particle", ParticleType.class).get();
    }

    public static ParticleType<RibbonParticleData> createRibbonParticleType() {
        return new ParticleType<>(false, RibbonParticleData.DESERIALIZER) {
            @Override
            public @NotNull Codec<RibbonParticleData> codec() {
                return CODEC_RIBBON(this);
            }
        };
    }
}
