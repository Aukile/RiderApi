package net.ankrya.rider_api.client.particle.base;

import net.ankrya.rider_api.client.particle.base.advanced.ParticleComponent;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleRotation;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonComponent;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.help.GJ;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ParticleRibbon extends AdvancedParticleBase {
    public Vec3[] positions;
    public Vec3[] prevPositions;
    public float texPanOffset;

    protected ParticleRibbon(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motionX, double motionY, double motionZ, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, int length, ParticleComponent[] components) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motionX, motionY, motionZ, rotation, scale, r, g, b, a, drag, duration, emissive, false, components);
        this.positions = new Vec3[length];
        this.prevPositions = new Vec3[length];
        if (this.positions.length >= 1) {
            this.positions[0] = new Vec3(this.getPosX(), this.getPosY(), this.getPosZ());
        }

        if (this.prevPositions.length >= 1) {
            this.prevPositions[0] = this.getPrevPos();
        }

    }

    protected void updatePosition() {
        super.updatePosition();
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        this.alpha = this.prevAlpha + (this.alpha - this.prevAlpha) * partialTicks;
        if ((double)this.alpha < 0.01) {
            this.alpha = 0.01F;
        }

        this.rCol = this.prevRed + (this.red - this.prevRed) * partialTicks;
        this.gCol = this.prevGreen + (this.green - this.prevGreen) * partialTicks;
        this.bCol = this.prevBlue + (this.blue - this.prevBlue) * partialTicks;
        this.particleScale = this.prevScale + (this.scale - this.prevScale) * partialTicks;
        ParticleComponent[] var4 = this.components;

        for (ParticleComponent component : var4) {
            component.preRender(this, partialTicks);
        }

        int j = this.getLightColor(partialTicks);
        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        float a = this.alpha;
        float scale = this.particleScale;
        float prevR = r;
        float prevG = g;
        float prevB = b;
        float prevA = a;
        float prevScale = scale;
        ParticleComponent[] var15 = this.components;
        int index = var15.length;

        int var17;
        for(var17 = 0; var17 < index; ++var17) {
            ParticleComponent component = var15[var17];
            if (component instanceof RibbonComponent.PropertyOverLength pOverLength) {
                float value = pOverLength.evaluate(0.0F);
                if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.SCALE) {
                    prevScale *= value;
                } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.RED) {
                    prevR *= value;
                } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.GREEN) {
                    prevG *= value;
                } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.BLUE) {
                    prevB *= value;
                } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.ALPHA) {
                    prevA *= value;
                }
            }
        }

        Vec3 offsetDir = new Vec3(0.0, 0.0, 0.0);

        for(index = 0; index < this.positions.length - 1; ++index) {
            if (this.positions[index] != null && this.positions[index + 1] != null) {
                r = this.rCol;
                g = this.gCol;
                b = this.bCol;
                scale = this.particleScale;
                float t = ((float)index + 1.0F) / ((float)this.positions.length - 1.0F);
                float tPrev = (float)index / ((float)this.positions.length - 1.0F);
                ParticleComponent[] var40 = this.components;

                for (ParticleComponent component : var40) {
                    if (component instanceof RibbonComponent.PropertyOverLength pOverLength) {
                        float value = pOverLength.evaluate(t);
                        if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.SCALE) {
                            scale *= value;
                        } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.RED) {
                            r *= value;
                        } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.GREEN) {
                            g *= value;
                        } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.BLUE) {
                            b *= value;
                        } else if (pOverLength.getProperty() == RibbonComponent.PropertyOverLength.EnumRibbonProperty.ALPHA) {
                            a *= value;
                        }
                    }
                }

                Vec3 Vector3d = renderInfo.getPosition();
                Vec3 p1 = this.prevPositions[index].add(this.positions[index].subtract(this.prevPositions[index]).scale((double)partialTicks)).subtract(Vector3d);
                Vec3 p2 = this.prevPositions[index + 1].add(this.positions[index + 1].subtract(this.prevPositions[index + 1]).scale((double)partialTicks)).subtract(Vector3d);
                Vec3 moveDir;
                if (index == 0) {
                    Vec3 moveDir2 = p2.subtract(p1).normalize();
                    if (this.rotation instanceof ParticleRotation.FaceCamera) {
                        moveDir2 = new Vec3(renderInfo.getLookVector());
                        offsetDir = moveDir2.cross(moveDir2).normalize();
                    } else {
                        offsetDir = moveDir2.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
                    }

                    offsetDir = offsetDir.scale(prevScale);
                }

                Vec3[] aVector3d2 = new Vec3[]{offsetDir.scale(-1.0), offsetDir, null, null};
                moveDir = p2.subtract(p1).normalize();
                if (this.rotation instanceof ParticleRotation.FaceCamera) {
                    Vec3 viewVec = new Vec3(renderInfo.getLookVector());
                    offsetDir = moveDir.cross(viewVec).normalize();
                } else {
                    offsetDir = moveDir.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
                }

                offsetDir = offsetDir.scale(scale);
                aVector3d2[2] = offsetDir;
                aVector3d2[3] = offsetDir.scale(-1.0);
                Vector4f[] vertices2 = new Vector4f[]{new Vector4f((float)aVector3d2[0].x, (float)aVector3d2[0].y, (float)aVector3d2[0].z, 1.0F), new Vector4f((float)aVector3d2[1].x, (float)aVector3d2[1].y, (float)aVector3d2[1].z, 1.0F), new Vector4f((float)aVector3d2[2].x, (float)aVector3d2[2].y, (float)aVector3d2[2].z, 1.0F), new Vector4f((float)aVector3d2[3].x, (float)aVector3d2[3].y, (float)aVector3d2[3].z, 1.0F)};
                Matrix4f boxTranslate = createTranslateMatrix((float)p1.x, (float)p1.y, (float)p1.z);
                GJ.ToMath.transform(vertices2[0], boxTranslate);
                GJ.ToMath.transform(vertices2[1], boxTranslate);
                boxTranslate = createTranslateMatrix((float)p2.x, (float)p2.y, (float)p2.z);
                GJ.ToMath.transform(vertices2[2], boxTranslate);
                GJ.ToMath.transform(vertices2[3], boxTranslate);
                float halfU = (this.getU1() - this.getU0()) / 2.0F + this.getU0();
                float f = this.getU0() + this.texPanOffset;
                float f1 = halfU + this.texPanOffset;
                float f2 = this.getV0();
                float f3 = this.getV1();
                buffer.vertex(vertices2[0].x(), vertices2[0].y(), vertices2[0].z()).uv(f1, f3).color(prevR, prevG, prevB, prevA).uv2(j).endVertex();
                buffer.vertex(vertices2[1].x(), vertices2[1].y(), vertices2[1].z()).uv(f1, f2).color(prevR, prevG, prevB, prevA).uv2(j).endVertex();
                buffer.vertex(vertices2[2].x(), vertices2[2].y(), vertices2[2].z()).uv(f, f2).color(r, g, b, a).uv2(j).endVertex();
                buffer.vertex(vertices2[3].x(), vertices2[3].y(), vertices2[3].z()).uv(f, f3).color(r, g, b, a).uv2(j).endVertex();
                prevR = r;
                prevG = g;
                prevB = b;
                prevA = a;
            }
        }

        ParticleComponent[] var36 = this.components;
        var17 = var36.length;

        for(int var39 = 0; var39 < var17; ++var39) {
            ParticleComponent component = var36[var39];
            component.postRender(this, buffer, renderInfo, partialTicks, j);
        }

    }

    public AABB getBoundingBox() {
        if (this.positions != null && this.positions.length > 0 && this.positions[0] != null) {
            double minX = this.positions[0].x() - 0.1;
            double minY = this.positions[0].y() - 0.1;
            double minZ = this.positions[0].z() - 0.1;
            double maxX = this.positions[0].x() + 0.1;
            double maxY = this.positions[0].y() + 0.1;
            double maxZ = this.positions[0].z() + 0.1;
            Vec3[] var13 = this.positions;

            for (Vec3 pos : var13) {
                if (pos != null) {
                    minX = Math.min(minX, pos.x());
                    minY = Math.min(minY, pos.y());
                    minZ = Math.min(minZ, pos.z());
                    maxX = Math.max(maxX, pos.x());
                    maxY = Math.max(maxY, pos.y());
                    maxZ = Math.max(maxZ, pos.z());
                }
            }

            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        } else {
            return super.getBoundingBox();
        }
    }

    public float getMinUPublic() {
        return this.getU0();
    }

    public float getMaxUPublic() {
        return this.getU1();
    }

    public float getMinVPublic() {
        return this.getV0();
    }

    public float getMaxVPublic() {
        return this.getV1();
    }

    public static void spawnRibbon(Level world, ParticleType<? extends RibbonParticleData> particle, int length, double x, double y, double z, double motionX, double motionY, double motionZ, boolean faceCamera, double yaw, double pitch, double roll, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive) {
        spawnRibbon(world, particle, length, x, y, z, motionX, motionY, motionZ, faceCamera, yaw, pitch, roll, scale, r, g, b, a, drag, duration, emissive, new ParticleComponent[0]);
    }

    public static void spawnRibbon(Level world, ParticleType<? extends RibbonParticleData> particle, int length, double x, double y, double z, double motionX, double motionY, double motionZ, boolean faceCamera, double yaw, double pitch, double roll, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, ParticleComponent[] components) {
        ParticleRotation rotation = faceCamera ? new ParticleRotation.FaceCamera(0.0F) : new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
        world.addParticle(new RibbonParticleData(particle, rotation, scale, r, g, b, a, drag, duration, emissive, length, components), x, y, z, motionX, motionY, motionZ);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Factory implements ParticleProvider<RibbonParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(RibbonParticleData typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleRibbon particle = new ParticleRibbon(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getRotation(), typeIn.getScale(), typeIn.getRed(), typeIn.getGreen(), typeIn.getBlue(), typeIn.getAlpha(), typeIn.getAirDrag(), typeIn.getDuration(), typeIn.isEmissive(), typeIn.getLength(), typeIn.getComponents());
            particle.setSpriteFromAge(this.spriteSet);
            return particle;
        }
    }

    public static Matrix4f createTranslateMatrix(float p_27654_, float p_27655_, float p_27656_) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00(1.0F);
        matrix4f.m11(1.0F);
        matrix4f.m22(1.0F);
        matrix4f.m33(1.0F);
        matrix4f.m03(p_27654_);
        matrix4f.m13(p_27655_);
        matrix4f.m23(p_27656_);
        return matrix4f;
    }
}
