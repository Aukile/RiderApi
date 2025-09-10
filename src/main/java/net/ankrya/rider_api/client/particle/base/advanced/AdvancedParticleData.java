package net.ankrya.rider_api.client.particle.base.advanced;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class AdvancedParticleData implements ParticleOptions {
    public static final ParticleOptions.Deserializer<AdvancedParticleData> DESERIALIZER = new ParticleOptions.Deserializer<AdvancedParticleData>() {
        public AdvancedParticleData fromCommand(ParticleType<AdvancedParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
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
            double faceCameraAngle = reader.readDouble();
            reader.expect(' ');
            boolean canCollide = reader.readBoolean();
            Object rotation;
            if (rotationMode.equals("face_camera")) {
                rotation = new ParticleRotation.FaceCamera((float)faceCameraAngle);
            } else if (rotationMode.equals("euler")) {
                rotation = new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            } else {
                rotation = new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));
            }

            return new AdvancedParticleData(particleTypeIn, (ParticleRotation)rotation, scale, red, green, blue, alpha, airDrag, duration, emissive, canCollide);
        }

        public AdvancedParticleData fromNetwork(ParticleType<AdvancedParticleData> particleTypeIn, FriendlyByteBuf buffer) {
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
            double faceCameraAngle = (double)buffer.readFloat();
            boolean canCollide = buffer.readBoolean();
            Object rotation;
            if (rotationMode.equals("face_camera")) {
                rotation = new ParticleRotation.FaceCamera((float)faceCameraAngle);
            } else if (rotationMode.equals("euler")) {
                rotation = new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            } else {
                rotation = new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));
            }

            return new AdvancedParticleData(particleTypeIn, (ParticleRotation)rotation, scale, red, green, blue, alpha, airDrag, duration, emissive, canCollide);
        }
    };
    private final ParticleType<? extends AdvancedParticleData> type;
    private final float airDrag;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final ParticleRotation rotation;
    private final float scale;
    private final boolean emissive;
    private final float duration;
    private final boolean canCollide;
    private final ParticleComponent[] components;

    public AdvancedParticleData(ParticleType<? extends AdvancedParticleData> type, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide) {
        this(type, rotation, scale, r, g, b, a, drag, duration, emissive, canCollide, new ParticleComponent[0]);
    }

    public AdvancedParticleData(ParticleType<? extends AdvancedParticleData> type, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide, ParticleComponent[] components) {
        this.type = type;
        this.rotation = rotation;
        this.scale = (float)scale;
        this.red = (float)r;
        this.green = (float)g;
        this.blue = (float)b;
        this.alpha = (float)a;
        this.emissive = emissive;
        this.airDrag = (float)drag;
        this.duration = (float)duration;
        this.canCollide = canCollide;
        this.components = components;
    }

    public static ParticleType<AdvancedParticleData> createParticleType() {
        return new ParticleType<>(false, AdvancedParticleData.DESERIALIZER) {
            @Override
            public Codec<AdvancedParticleData> codec() {
                return CODEC(this);
            }
        };
    }

    public static Codec<AdvancedParticleData> CODEC(ParticleType<AdvancedParticleData> particleType) {
        return RecordCodecBuilder.create((codecBuilder) -> {
            return codecBuilder.group(Codec.DOUBLE.fieldOf("scale").forGetter(AdvancedParticleData::getScale), Codec.DOUBLE.fieldOf("r").forGetter(AdvancedParticleData::getRed), Codec.DOUBLE.fieldOf("g").forGetter(AdvancedParticleData::getGreen), Codec.DOUBLE.fieldOf("b").forGetter(AdvancedParticleData::getBlue), Codec.DOUBLE.fieldOf("a").forGetter(AdvancedParticleData::getAlpha), Codec.DOUBLE.fieldOf("drag").forGetter(AdvancedParticleData::getAirDrag), Codec.DOUBLE.fieldOf("duration").forGetter(AdvancedParticleData::getDuration), Codec.BOOL.fieldOf("emissive").forGetter(AdvancedParticleData::isEmissive), Codec.BOOL.fieldOf("canCollide").forGetter(AdvancedParticleData::getCanCollide)).apply(codecBuilder, (scale, r, g, b, a, drag, duration, emissive, canCollide) -> {
                return new AdvancedParticleData(particleType, new ParticleRotation.FaceCamera(0.0F), scale, r, g, b, a, drag, duration, emissive, canCollide, new ParticleComponent[0]);
            });
        });
    }

    public String writeToString() {
        float faceCameraAngle = 0.0F;
        float yaw = 0.0F;
        float pitch = 0.0F;
        float roll = 0.0F;
        String rotationMode;
        if (this.rotation instanceof ParticleRotation.FaceCamera) {
            rotationMode = "face_camera";
            faceCameraAngle = ((ParticleRotation.FaceCamera)this.rotation).faceCameraAngle;
        } else if (this.rotation instanceof ParticleRotation.EulerAngles) {
            rotationMode = "euler";
            yaw = ((ParticleRotation.EulerAngles)this.rotation).yaw;
            pitch = ((ParticleRotation.EulerAngles)this.rotation).pitch;
            roll = ((ParticleRotation.EulerAngles)this.rotation).roll;
        } else {
            rotationMode = "orient";
            Vec3 vec = ((ParticleRotation.OrientVector)this.rotation).orientation;
            yaw = (float)vec.x;
            pitch = (float)vec.y;
            roll = (float)vec.z;
        }

        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %s %.2f %.2f %.2f %.2f %b %.2f %.2f %b", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.airDrag, this.red, this.green, this.blue, this.alpha, rotationMode, this.scale, yaw, pitch, roll, this.emissive, this.duration, faceCameraAngle, this.canCollide);
    }

    public @NotNull ParticleType<? extends AdvancedParticleData> getType() {
        return this.type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        float faceCameraAngle = 0.0F;
        float yaw = 0.0F;
        float pitch = 0.0F;
        float roll = 0.0F;
        String rotationMode;
        if (this.rotation instanceof ParticleRotation.FaceCamera) {
            rotationMode = "face_camera";
            faceCameraAngle = ((ParticleRotation.FaceCamera)this.rotation).faceCameraAngle;
        } else if (this.rotation instanceof ParticleRotation.EulerAngles) {
            rotationMode = "euler";
            yaw = ((ParticleRotation.EulerAngles)this.rotation).yaw;
            pitch = ((ParticleRotation.EulerAngles)this.rotation).pitch;
            roll = ((ParticleRotation.EulerAngles)this.rotation).roll;
        } else {
            rotationMode = "orient";
            Vec3 vec = ((ParticleRotation.OrientVector)this.rotation).orientation;
            yaw = (float)vec.x;
            pitch = (float)vec.y;
            roll = (float)vec.z;
        }

        buffer.writeFloat(this.airDrag);
        buffer.writeFloat(this.red);
        buffer.writeFloat(this.green);
        buffer.writeFloat(this.blue);
        buffer.writeFloat(this.alpha);
        buffer.writeUtf(rotationMode);
        buffer.writeFloat(this.scale);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
        buffer.writeBoolean(this.emissive);
        buffer.writeFloat(this.duration);
        buffer.writeFloat(faceCameraAngle);
        buffer.writeBoolean(this.canCollide);
    }

    @OnlyIn(Dist.CLIENT)
    public double getRed() {
        return this.red;
    }

    @OnlyIn(Dist.CLIENT)
    public double getGreen() {
        return this.green;
    }

    @OnlyIn(Dist.CLIENT)
    public double getBlue() {
        return this.blue;
    }

    @OnlyIn(Dist.CLIENT)
    public double getAlpha() {
        return this.alpha;
    }

    @OnlyIn(Dist.CLIENT)
    public double getAirDrag() {
        return this.airDrag;
    }

    @OnlyIn(Dist.CLIENT)
    public ParticleRotation getRotation() {
        return this.rotation;
    }

    @OnlyIn(Dist.CLIENT)
    public double getScale() {
        return this.scale;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isEmissive() {
        return this.emissive;
    }

    @OnlyIn(Dist.CLIENT)
    public double getDuration() {
        return this.duration;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getCanCollide() {
        return this.canCollide;
    }

    @OnlyIn(Dist.CLIENT)
    public ParticleComponent[] getComponents() {
        return this.components;
    }

    public static MapCodec<AdvancedParticleData> codec(ParticleType<AdvancedParticleData> particleType) {
        return RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.DOUBLE.fieldOf("scale").forGetter(AdvancedParticleData::getScale),
                Codec.DOUBLE.fieldOf("r").forGetter(AdvancedParticleData::getRed),
                Codec.DOUBLE.fieldOf("g").forGetter(AdvancedParticleData::getGreen),
                Codec.DOUBLE.fieldOf("b").forGetter(AdvancedParticleData::getBlue),
                Codec.DOUBLE.fieldOf("a").forGetter(AdvancedParticleData::getAlpha),
                Codec.DOUBLE.fieldOf("drag").forGetter(AdvancedParticleData::getAirDrag),
                Codec.DOUBLE.fieldOf("duration").forGetter(AdvancedParticleData::getDuration),
                Codec.BOOL.fieldOf("emissive").forGetter(AdvancedParticleData::isEmissive),
                Codec.BOOL.fieldOf("canCollide").forGetter(AdvancedParticleData::getCanCollide)
        ).apply(instance, (scale, r, g, b, a, drag, duration, emissive, canCollide) -> new AdvancedParticleData(
                particleType,
                new ParticleRotation.FaceCamera(0.0F),
                scale, r, g, b, a,
                drag, duration, emissive, canCollide,
                new ParticleComponent[0]
        )));
    }

    public static AdvancedParticleData readFromNetwork(FriendlyByteBuf buffer, ParticleType<AdvancedParticleData> type) {
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

    @SuppressWarnings("unchecked")
    public static ParticleType<AdvancedParticleData> getParticleType() {
        return (ParticleType<AdvancedParticleData>) ApiRegister.getRegisterObject("advanced_particle", ParticleType.class).get();
    }
}