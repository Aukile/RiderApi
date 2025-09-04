package net.ankrya.rider_api.client.particle.base;

import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleComponent;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleRotation;
import net.ankrya.rider_api.client.particle.rendertype.CustomParticleRenderType;
import net.ankrya.rider_api.help.GJ;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 高级粒子~<br>
 * 可加组件<br>
 * @author Aistray
 * @see ParticleComponent
 */
public class AdvancedParticleBase extends TextureSheetParticle {
    public boolean doRender;
    public float airDrag;
    public float red;
    public float green;
    public float blue;
    public float alpha;
    public float prevRed;
    public float prevGreen;
    public float prevBlue;
    public float prevAlpha;
    public float scale;
    public float prevScale;
    public float particleScale;
    public ParticleRotation rotation;
    public boolean emissive;
    public double prevMotionX;
    public double prevMotionY;
    public double prevMotionZ;
    public ParticleComponent[] components;
    public ParticleRibbon ribbon;

    protected AdvancedParticleBase(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motionX, double motionY, double motionZ, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide, ParticleComponent[] components) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0, 0.0, 0.0);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.red = (float)r;
        this.green = (float)g;
        this.blue = (float)b;
        this.alpha = (float)a;
        this.scale = (float)scale;
        this.lifetime = (int)duration;
        this.airDrag = (float)drag;
        this.rotation = rotation;
        this.components = components;
        this.emissive = emissive;
        this.ribbon = null;
        this.doRender = true;
        ParticleComponent[] var32 = components;
        int var33 = components.length;

        for(int var34 = 0; var34 < var33; ++var34) {
            ParticleComponent component = var32[var34];
            component.init(this);
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;
        this.prevAlpha = this.alpha;
        this.rotation.setPrevValues();
        this.prevScale = this.scale;
        this.hasPhysics = canCollide;
    }

    public @NotNull ParticleRenderType getRenderType() {
        return CustomParticleRenderType.PARTICLE_SHEET_LIT_TRANSLUCENT;
    }

    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        if (this.emissive) {
            //int k = i >> 16 & 255;
            return 15728880;
        } else {
            return i;
        }
    }

    public void tick() {
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;
        this.prevAlpha = this.alpha;
        this.prevScale = this.scale;
        this.rotation.setPrevValues();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevMotionX = this.xd;
        this.prevMotionY = this.yd;
        this.prevMotionZ = this.zd;
        ParticleComponent[] var1 = this.components;
        int var2 = var1.length;

        int var3;
        ParticleComponent component;
        for(var3 = 0; var3 < var2; ++var3) {
            component = var1[var3];
            component.preUpdate(this);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.updatePosition();
        var1 = this.components;
        var2 = var1.length;

        for(var3 = 0; var3 < var2; ++var3) {
            component = var1[var3];
            component.postUpdate(this);
        }

        if (this.ribbon != null) {
            this.ribbon.setPos(this.x, this.y, this.z);
            this.ribbon.positions[0] = new Vec3(this.x, this.y, this.z);
            this.ribbon.prevPositions[0] = this.getPrevPos();
        }

    }

    protected void updatePosition() {
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround && this.hasPhysics) {
            this.xd *= 0.699999988079071;
            this.zd *= 0.699999988079071;
        }

        this.xd *= this.airDrag;
        this.yd *= this.airDrag;
        this.zd *= this.airDrag;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        System.out.println("++rendering particle");
        this.alpha = this.prevAlpha + (this.alpha - this.prevAlpha) * partialTicks;
        if ((double)this.alpha < 0.01) {
            this.alpha = 0.01F;
        }

        this.rCol = this.prevRed + (this.red - this.prevRed) * partialTicks;
        this.gCol = this.prevGreen + (this.green - this.prevGreen) * partialTicks;
        this.bCol = this.prevBlue + (this.blue - this.prevBlue) * partialTicks;
        this.particleScale = this.prevScale + (this.scale - this.prevScale) * partialTicks;
        ParticleComponent[] var4 = this.components;
        int var5 = var4.length;

        for (ParticleComponent component : var4) {
            component.preRender(this, partialTicks);
        }

        if (this.doRender) {
            Vec3 Vector3d = renderInfo.getPosition();
            float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - Vector3d.x());
            float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - Vector3d.y());
            float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - Vector3d.z());
            Quaternionf quaternionf = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
            float f4;
            float f7;
            float f8;
            if (this.rotation instanceof ParticleRotation.FaceCamera) {
                ParticleRotation.FaceCamera faceCameraRot = (ParticleRotation.FaceCamera)this.rotation;
                if (faceCameraRot.faceCameraAngle == 0.0F && faceCameraRot.prevFaceCameraAngle == 0.0F) {
                    quaternionf = renderInfo.rotation();
                } else {
                    quaternionf = new Quaternionf(renderInfo.rotation());
                    f4 = Mth.lerp(partialTicks, faceCameraRot.prevFaceCameraAngle, faceCameraRot.faceCameraAngle);
                    quaternionf.mul(Axis.ZP.rotation(f4));
                }
            } else if (this.rotation instanceof ParticleRotation.EulerAngles) {
                ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles)this.rotation;
                f4 = eulerRot.prevPitch + (eulerRot.pitch - eulerRot.prevPitch) * partialTicks;
                f7 = eulerRot.prevYaw + (eulerRot.yaw - eulerRot.prevYaw) * partialTicks;
                f8 = eulerRot.prevRoll + (eulerRot.roll - eulerRot.prevRoll) * partialTicks;
                Quaternionf quatX = createQuaternionf(f4, 0.0F, 0.0F);
                Quaternionf quatY = createQuaternionf(0.0F, f7, 0.0F);
                Quaternionf quatZ = createQuaternionf(0.0F, 0.0F, f8);
                quaternionf.mul(quatZ);
                quaternionf.mul(quatY);
                quaternionf.mul(quatX);
            }

            if (this.rotation instanceof ParticleRotation.OrientVector) {
                ParticleRotation.OrientVector orientRot = (ParticleRotation.OrientVector)this.rotation;
                double x = orientRot.prevOrientation.x + (orientRot.orientation.x - orientRot.prevOrientation.x) * (double)partialTicks;
                double y = orientRot.prevOrientation.y + (orientRot.orientation.y - orientRot.prevOrientation.y) * (double)partialTicks;
                double z = orientRot.prevOrientation.z + (orientRot.orientation.z - orientRot.prevOrientation.z) * (double)partialTicks;
                float pitch = (float)Math.asin(-y);
                float yaw = (float)Mth.atan2(x, z);
                Quaternionf quatX = createQuaternionf(pitch, 0.0F, 0.0F);
                Quaternionf quatY = createQuaternionf(0.0F, yaw, 0.0F);
                quaternionf.mul(quatY);
                quaternionf.mul(quatX);
            }

            Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            f4 = this.particleScale * 0.1F;

            for(int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f[i];
                GJ.ToMath.transform(vector3f, quaternionf);
                vector3f.mul(f4);
                vector3f.add(f, f1, f2);
            }

            f7 = this.getU0();
            f8 = this.getU1();
            float f5 = this.getV0();
            float f6 = this.getV1();
            int j = this.getLightColor(partialTicks);
            buffer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(f8, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(15728880 & '\uffff', 15728880 >> 16 & '\uffff');
            buffer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(f8, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(15728880 & '\uffff', 15728880 >> 16 & '\uffff');
            buffer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(f7, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(15728880 & '\uffff', 15728880 >> 16 & '\uffff');
            buffer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(f7, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(15728880 & '\uffff', 15728880 >> 16 & '\uffff');
            ParticleComponent[] var35 = this.components;
            for (ParticleComponent component : var35) {
                component.postRender(this, buffer, renderInfo, partialTicks, j);
            }

        }
        System.out.println("--render particle over");
    }

    public float getAge() {
        return (float)this.age;
    }

    public double getPosX() {
        return this.x;
    }

    public void setPosX(double posX) {
        this.setPos(posX, this.y, this.z);
    }

    public double getPosY() {
        return this.y;
    }

    public void setPosY(double posY) {
        this.setPos(this.x, posY, this.z);
    }

    public double getPosZ() {
        return this.z;
    }

    public void setPosZ(double posZ) {
        this.setPos(this.x, this.y, posZ);
    }

    public double getMotionX() {
        return this.xd;
    }

    public void setMotionX(double motionX) {
        this.xd = motionX;
    }

    public double getMotionY() {
        return this.yd;
    }

    public void setMotionY(double motionY) {
        this.yd = motionY;
    }

    public double getMotionZ() {
        return this.zd;
    }

    public void setMotionZ(double motionZ) {
        this.zd = motionZ;
    }

    public Vec3 getPrevPos() {
        return new Vec3(this.xo, this.yo, this.zo);
    }

    public double getPrevPosX() {
        return this.xo;
    }

    public double getPrevPosY() {
        return this.yo;
    }

    public double getPrevPosZ() {
        return this.zo;
    }

    public Level getWorld() {
        return this.level;
    }

    /**无组件款*/
    public static void spawnParticle(Level world, ParticleType<AdvancedParticleData> particle, double x, double y, double z, double motionX, double motionY, double motionZ, boolean faceCamera, double yaw, double pitch, double roll, double faceCameraAngle, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide) {
        spawnParticle(world, particle, x, y, z, motionX, motionY, motionZ, faceCamera, yaw, pitch, roll, faceCameraAngle, scale, r, g, b, a, drag, duration, emissive, canCollide, new ParticleComponent[0]);
    }

    /**
     * 会控制面向摄像机角度的
     * @param world 世界
     * @param particle 粒子
     * @param x 坐标X
     * @param y 坐标Y
     * @param z 坐标Z
     * @param motionX 粒子X轴运动
     * @param motionY 粒子Y轴运动
     * @param motionZ 粒子Z轴运动
     * @param faceCamera 是否面向摄像机
     * @param yaw 粒子Y轴旋转
     * @param pitch 粒子X轴旋转
     * @param roll 粒子Z轴旋转
     * @param faceCameraAngle 粒子面向摄像机角度
     * @param scale 粒子大小
     * @param r 粒子颜色R
     * @param g 粒子颜色G
     * @param b 粒子颜色B
     * @param a 粒子透明度
     * @param drag 粒子运动阻力
     * @param duration 粒子生命时间
     * @param emissive 粒子是否发光
     * @param canCollide 粒子是否可碰撞
     * @param components 粒子组件
     */
    public static void spawnParticle(Level world, ParticleType<AdvancedParticleData> particle, double x, double y, double z, double motionX, double motionY, double motionZ, boolean faceCamera, double yaw, double pitch, double roll, double faceCameraAngle, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide, ParticleComponent[] components) {
        ParticleRotation rotation = faceCamera ? new ParticleRotation.FaceCamera((float)faceCameraAngle) : new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
        world.addParticle(new AdvancedParticleData(particle, rotation, scale, r, g, b, a, drag, duration, emissive, canCollide, components), x, y, z, motionX, motionY, motionZ);
    }

    /**
     * 基础款
     * @param motionX 粒子X轴运动
     * @param motionY 粒子Y轴运动
     * @param motionZ 粒子Z轴运动
     * @param rotation 粒子旋转
     * @param scale 粒子大小
     * @param r 粒子颜色R
     * @param g 粒子颜色G
     * @param b 粒子颜色B
     * @param a 粒子透明度
     * @param drag 粒子运动阻力
     * @param duration 粒子生命时间
     * @param emissive 粒子是否发光
     * @param canCollide 粒子是否可碰撞
     * @param components 粒子组件
     */
    public static void spawnParticle(Level world, ParticleType<AdvancedParticleData> particle, double x, double y, double z, double motionX, double motionY, double motionZ, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide, ParticleComponent[] components) {
        world.addParticle(new AdvancedParticleData(particle, rotation, scale, r, g, b, a, drag, duration, emissive, canCollide, components), x, y, z, motionX, motionY, motionZ);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<AdvancedParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        public Particle createParticle(AdvancedParticleData typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            AdvancedParticleBase particle = new AdvancedParticleBase(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getRotation(), typeIn.getScale(), typeIn.getRed(), typeIn.getGreen(), typeIn.getBlue(), typeIn.getAlpha(), typeIn.getAirDrag(), typeIn.getDuration(), typeIn.isEmissive(), typeIn.getCanCollide(), typeIn.getComponents());
            particle.setColor((float)typeIn.getRed(), (float)typeIn.getGreen(), (float)typeIn.getBlue());
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

    private static Quaternionf createQuaternionf(float p_80130_, float p_80131_, float p_80132_) {
        float $$4 = (float) Math.sin(0.5F * p_80130_);
        float $$5 = (float) Math.cos(0.5F * p_80130_);
        float $$6 = (float) Math.sin(0.5F * p_80131_);
        float $$7 = (float) Math.cos(0.5F * p_80131_);
        float $$8 = (float) Math.sin(0.5F * p_80132_);
        float $$9 = (float) Math.cos(0.5F * p_80132_);
        float x = $$4 * $$7 * $$9 + $$5 * $$6 * $$8;
        float y = $$5 * $$6 * $$9 - $$4 * $$7 * $$8;
        float z = $$4 * $$6 * $$9 + $$5 * $$7 * $$8;
        float w = $$5 * $$7 * $$9 - $$4 * $$6 * $$8;
        return new Quaternionf(x, y, z, w);
    }
}
