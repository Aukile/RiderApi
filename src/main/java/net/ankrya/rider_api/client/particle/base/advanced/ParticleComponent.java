package net.ankrya.rider_api.client.particle.base.advanced;

import net.ankrya.rider_api.client.particle.base.AdvancedParticleBase;
import net.ankrya.rider_api.help.GJ;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * 粒子组件<br>
 * 控件列表：<br>
 * - {@link FaceMotion}面向运动方向组件<br>
 * - {@link Orbit}曲线运动组件<br>
 * - {@link Attractor}粒子追踪组件<br>
 * - {@link PinLocation}固定位置组件<br>
 * - {@link PropertyControl}属性控制组件<br>
 * - {@link Oscillator}振荡器 (动画组件)<br>
 * - {@link KeyTrack}关键帧轨道 (动画组件)
 */
public abstract class ParticleComponent {
    public ParticleComponent() {
    }

    public void init(AdvancedParticleBase particle) {
    }

    public void preUpdate(AdvancedParticleBase particle) {
    }

    public void postUpdate(AdvancedParticleBase particle) {
    }

    public void preRender(AdvancedParticleBase particle, float partialTicks) {
    }

    public void postRender(AdvancedParticleBase particle, VertexConsumer buffer, Camera renderInfo, float partialTicks, int lightmap) {
    }

    public static Constant constant(float value) {
        return new Constant(value);
    }

    public static class Constant extends AnimData {
        float value;

        public Constant(float value) {
            this.value = value;
        }

        public float evaluate(float t) {
            return this.value;
        }
    }

    /**
     * <strong>面向运动方向组件</strong><br>
     * 使粒子始终面向其运动方向。<br>
     * 根据粒子的运动向量计算旋转角度，让粒子的朝向与其运动方向保持一致。<br>
     * 支持两种旋转方式：<br>
     * - EulerAngles（欧拉角）<br>
     * - OrientVector（向量定向）
     */
    public static class FaceMotion extends ParticleComponent {
        public FaceMotion() {
        }

        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            super.preRender(particle, partialTicks);
            double dx = particle.getPosX() - particle.getPrevPosX();
            double dy = particle.getPosY() - particle.getPrevPosY();
            double dz = particle.getPosZ() - particle.getPrevPosZ();
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d != 0.0) {
                if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                    ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles)particle.rotation;
                    double a = dy / d;
                    a = Math.max(-1.0, Math.min(1.0, a));
                    float pitch = -((float)Math.asin(a));
                    float yaw = -((float)(Math.atan2(dz, dx) + Math.PI));
                    eulerRot.roll = pitch;
                    eulerRot.yaw = yaw;
                } else if (particle.rotation instanceof ParticleRotation.OrientVector) {
                    ParticleRotation.OrientVector orientRot = (ParticleRotation.OrientVector)particle.rotation;
                    orientRot.orientation = (new Vec3(dx, dy, dz)).normalize();
                }
            }

        }
    }

    /**
     * <strong>曲线运动组件</strong><br>
     * 通过四元数计算实现平滑的3D旋转效果。<br>
     * 让粒子围绕指定点做轨道运动。<br>
     * 支持：<br>
     * - 自定义轨道中心点<br>
     * - 可变半径<br>
     * - 可调节的旋转轴<br>
     * - 面向摄像机选项<br>
     * - 初始旋转角度设置<br>
     * - 垂直偏移控制
     */
    public static class Orbit extends ParticleComponent {
        private final Vec3[] location;
        private final AnimData phase;
        private final AnimData radius;
        private final AnimData axisX;
        private final AnimData axisY;
        private final AnimData axisZ;
        private final boolean faceCamera;
        private final float firstRot;
        private final double yAdd;

        /**
         *
         * @param location 基础位置
         * @param phase 旋转
         * @param radius 半径
         * @param axisX 轴X
         * @param axisY 轴Y
         * @param axisZ 轴Z
         * @param firstRot 初始旋转
         * @param faceCamera 是否朝向摄像机
         * @param yAdd 竖直偏移
         */
        public Orbit(Vec3[] location, AnimData phase, AnimData radius, AnimData axisX, AnimData axisY, AnimData axisZ,float firstRot, boolean faceCamera,double yAdd) {
            this.location = location;
            this.phase = phase;
            this.radius = radius;
            this.axisX = axisX;
            this.axisY = axisY;
            this.axisZ = axisZ;
            this.faceCamera = faceCamera;
            this.firstRot = firstRot;
            this.yAdd = yAdd;
        }

        public void init(AdvancedParticleBase particle) {
            this.apply(particle, 0.0F);
        }

        public void preUpdate(AdvancedParticleBase particle) {
            float ageFrac = particle.getAge() / (float)particle.getLifetime();
            this.apply(particle, ageFrac);
        }

        private void apply(AdvancedParticleBase particle, float t) {
            float p = this.phase.evaluate(t);
            float r = this.radius.evaluate(t);
            Vector3f axis;
            if (this.faceCamera && Minecraft.getInstance().player != null) {
                axis = new Vector3f(Minecraft.getInstance().player.getLookAngle().toVector3f());
                axis.normalize();
            } else {
                axis = new Vector3f(this.axisX.evaluate(t), this.axisY.evaluate(t), this.axisZ.evaluate(t));
                axis.normalize();
            }

            float v = p * 3.14159265357f * 2.0F + firstRot;
            float multiple = (float) Math.sin(v / 2.0F);
            float vtr = (float) Math.cos(v / 2.0F);

            Quaternionf quat = new Quaternionf(axis.x() * multiple, axis.y() * multiple, axis.z() * multiple, vtr);
            Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
            Vector3f start = axis;
            if ((double)Math.abs(axis.dot(up)) > 0.99) {
                start = new Vector3f(1.0F, 0.0F, 0.0F);
            }

            start.cross(up);
            start.normalize();
            GJ.ToMath.transform(start, quat);
            start.mul(r);
            if (this.location.length > 0 && this.location[0] != null) {
                start.add((float)this.location[0].x, (float)this.location[0].y, (float)this.location[0].z);
            }

            particle.setPos(start.x(), start.y()+t*yAdd, start.z());
        }
    }

    /**
     * <strong>粒子追踪组件</strong><br>
     * 创建一个吸引点，将粒子吸引到指定位置。<br>
     * 还可以设置吸引距离阈值，当粒子接近目标点到一定程度时自动销毁。<br>
     * 有三种行为模式：<br>
     * {@link EnumAttractorBehavior#LINEAR}: 线性移动到目标点<br>
     * {@link EnumAttractorBehavior#EXPONENTIAL}: 指数方式移动到目标点<br>
     * {@link EnumAttractorBehavior#SIMULATED}: 物理模拟方式，通过施加力来移动粒子
     *
     */
    public static class Attractor extends ParticleComponent {
        private final Vec3[] location;
        private final float strength;
        private final float killDist;
        private final EnumAttractorBehavior behavior;
        private Vec3 startLocation;

        /**
         * @param location 吸引点
         * @param strength 吸引力
         * @param killDist 吸引距离阈值
         * @param behavior 吸引行为{@link EnumAttractorBehavior}
         */
        public Attractor(Vec3[] location, float strength, float killDist, EnumAttractorBehavior behavior) {
            this.location = location;
            this.strength = strength;
            this.killDist = killDist;
            this.behavior = behavior;
        }

        public void init(AdvancedParticleBase particle) {
            this.startLocation = new Vec3(particle.getPosX(), particle.getPosY(), particle.getPosZ());
        }

        public void preUpdate(AdvancedParticleBase particle) {
            float ageFrac = particle.getAge() / (float)(particle.getLifetime() - 1);
            if (this.location.length > 0) {
                Vec3 destinationVec = this.location[0];
                Vec3 currPos = new Vec3(particle.getPosX(), particle.getPosY(), particle.getPosZ());
                Vec3 diff = destinationVec.subtract(currPos);
                if (diff.length() < (double)this.killDist) {
                    particle.remove();
                }

                Vec3 path = destinationVec.subtract(this.startLocation).scale(Math.pow(ageFrac, this.strength)).add(this.startLocation).subtract(currPos);
                if (this.behavior == EnumAttractorBehavior.EXPONENTIAL) {

                    particle.move(path.x, path.y, path.z);
                } else if (this.behavior == EnumAttractorBehavior.LINEAR) {
                    path = destinationVec.subtract(this.startLocation).scale(ageFrac).add(this.startLocation).subtract(currPos);
                    particle.move(path.x, path.y, path.z);
                } else {
                    double dist = Math.max(diff.length(), 0.001);
                    diff = diff.normalize().scale((double)this.strength / (dist * dist));
                    particle.setMotionX(Math.min(particle.getMotionX() + diff.x, 5.0));
                    particle.setMotionY(Math.min(particle.getMotionY() + diff.y, 5.0));
                    particle.setMotionZ(Math.min(particle.getMotionZ() + diff.z, 5.0));
                }
            }

        }

        public static enum EnumAttractorBehavior {
            /**线性移动到目标点*/
            LINEAR,
            /**指数方式移动到目标点*/
            EXPONENTIAL,
            /**物理模拟方式，通过施加力来移动粒子*/
            SIMULATED;

            private EnumAttractorBehavior() {
            }
        }
    }

    /**
     * <strong>固定位置组件</strong><br>
     * 将粒子固定在指定位置，无论粒子如何更新，都会强制保持在设置的位置上。
     */
    public static class PinLocation extends ParticleComponent {
        private final Vec3[] location;

        public PinLocation(Vec3[] location) {
            this.location = location;
        }

        public void init(AdvancedParticleBase particle) {
            if (this.location != null && this.location.length > 0 && this.location[0] != null) {
                particle.setPos(this.location[0].x, this.location[0].y, this.location[0].z);
            }

        }

        public void preUpdate(AdvancedParticleBase particle) {
            if (this.location != null && this.location.length > 0 && this.location[0] != null) {
                particle.setPos(this.location[0].x, this.location[0].y, this.location[0].z);
            }

        }

        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            super.preRender(particle, partialTicks);
            particle.doRender = this.location != null && this.location.length > 0 && this.location[0] != null;
        }
    }

    /**
     * <strong>属性控制组件</strong><br>
     * 动态控制粒子的各种属性。<br>
     * 支持两种控制模式：绝对值和增量值.<br>
     * 支持的属性包括：<br>
     * 位置属性：pos_x, pos_y, pos_z<br>
     * 运动属性：motion_x, motion_y, motion_z<br>
     * 颜色属性：red, green, blue, alpha<br>
     * 缩放属性：scale<br>
     * 旋转属性：yaw, pitch, roll, particle_angle<br>
     * 空气阻力：air_drag
     * @see EnumParticleProperty
     */
    public static class PropertyControl extends ParticleComponent {
        private final AnimData animData;
        private final EnumParticleProperty property;
        private final boolean additive;

        public PropertyControl(EnumParticleProperty property, AnimData animData, boolean additive) {
            this.property = property;
            this.animData = animData;
            this.additive = additive;
        }

        public void init(AdvancedParticleBase particle) {
            float value = this.animData.evaluate(0.0F);
            this.applyUpdate(particle, value);
            this.applyRender(particle, value);
        }

        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            float ageFrac = (particle.getAge() + partialTicks) / (float)particle.getLifetime();
            float value = this.animData.evaluate(ageFrac);
            this.applyRender(particle, value);
        }

        public void preUpdate(AdvancedParticleBase particle) {
            float ageFrac = particle.getAge() / (float)particle.getLifetime();
            float value = this.animData.evaluate(ageFrac);
            this.applyUpdate(particle, value);
        }

        private void applyUpdate(AdvancedParticleBase particle, float value) {
            if (this.property == EnumParticleProperty.POS_X) {
                if (this.additive) {
                    particle.setPosX(particle.getPosX() + (double)value);
                } else {
                    particle.setPosX((double)value);
                }
            } else if (this.property == EnumParticleProperty.POS_Y) {
                if (this.additive) {
                    particle.setPosY(particle.getPosY() + (double)value);
                } else {
                    particle.setPosY((double)value);
                }
            } else if (this.property == EnumParticleProperty.POS_Z) {
                if (this.additive) {
                    particle.setPosZ(particle.getPosZ() + (double)value);
                } else {
                    particle.setPosZ((double)value);
                }
            } else if (this.property == EnumParticleProperty.MOTION_X) {
                if (this.additive) {
                    particle.setMotionX(particle.getMotionX() + (double)value);
                } else {
                    particle.setMotionX((double)value);
                }
            } else if (this.property == EnumParticleProperty.MOTION_Y) {
                if (this.additive) {
                    particle.setMotionY(particle.getMotionY() + (double)value);
                } else {
                    particle.setMotionY((double)value);
                }
            } else if (this.property == EnumParticleProperty.MOTION_Z) {
                if (this.additive) {
                    particle.setMotionZ(particle.getMotionZ() + (double)value);
                } else {
                    particle.setMotionZ((double)value);
                }
            } else if (this.property == EnumParticleProperty.AIR_DRAG) {
                if (this.additive) {
                    particle.airDrag += value;
                } else {
                    particle.airDrag = value;
                }
            }

        }

        private void applyRender(AdvancedParticleBase particle, float value) {
            if (this.property == EnumParticleProperty.RED) {
                if (this.additive) {
                    particle.red += value;
                } else {
                    particle.red = value;
                }
            } else if (this.property == EnumParticleProperty.GREEN) {
                if (this.additive) {
                    particle.green += value;
                } else {
                    particle.green = value;
                }
            } else if (this.property == EnumParticleProperty.BLUE) {
                if (this.additive) {
                    particle.blue += value;
                } else {
                    particle.blue = value;
                }
            } else if (this.property == EnumParticleProperty.ALPHA) {
                if (this.additive) {
                    particle.alpha += value;
                } else {
                    particle.alpha = value;
                }
            } else if (this.property == EnumParticleProperty.SCALE) {
                if (this.additive) {
                    particle.scale += value;
                } else {
                    particle.scale = value;
                }
            } else {
                ParticleRotation.EulerAngles eulerRot;
                if (this.property == EnumParticleProperty.YAW) {
                    if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                        eulerRot = (ParticleRotation.EulerAngles)particle.rotation;
                        if (this.additive) {
                            eulerRot.yaw += value;
                        } else {
                            eulerRot.yaw = value;
                        }
                    }
                } else if (this.property == EnumParticleProperty.PITCH) {
                    if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                        eulerRot = (ParticleRotation.EulerAngles)particle.rotation;
                        if (this.additive) {
                            eulerRot.pitch += value;
                        } else {
                            eulerRot.pitch = value;
                        }
                    }
                } else if (this.property == EnumParticleProperty.ROLL) {
                    if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                        eulerRot = (ParticleRotation.EulerAngles)particle.rotation;
                        if (this.additive) {
                            eulerRot.roll += value;
                        } else {
                            eulerRot.roll = value;
                        }
                    }
                } else if (this.property == EnumParticleProperty.PARTICLE_ANGLE && particle.rotation instanceof ParticleRotation.FaceCamera) {
                    ParticleRotation.FaceCamera faceCameraRot = (ParticleRotation.FaceCamera)particle.rotation;
                    if (this.additive) {
                        faceCameraRot.faceCameraAngle += value;
                    } else {
                        faceCameraRot.faceCameraAngle = value;
                    }
                }
            }

        }

        public static enum EnumParticleProperty {
            /**位置属性x*/
            POS_X,
            /**位置属性y*/
            POS_Y,
            /**位置属性z*/
            POS_Z,
            /**速度属性x*/
            MOTION_X,
            /**速度属性y*/
            MOTION_Y,
            /**速度属性z*/
            MOTION_Z,
            /**颜色属性r*/
            RED,
            /**颜色属性g*/
            GREEN,
            /**颜色属性b*/
            BLUE,
            /**透明度属性a*/
            ALPHA,
            /**缩放属性*/
            SCALE,
            /**旋转属性yaw*/
            YAW,
            /**旋转属性pitch*/
            PITCH,
            /**旋转属性roll*/
            ROLL,
            /**粒子旋转属性*/
            PARTICLE_ANGLE,
            /**空气阻力属性*/
            AIR_DRAG;

            private EnumParticleProperty() {
            }
        }
    }

    /**
     * · 动画数据项目<br>
     * <strong>振荡器</strong><br>
     * 创建振荡效果，按余弦函数在两个值之间振荡变化。<br>
     * 可控制：<br>
     * - 起始值和结束值<br>
     * - 振荡频率<br>
     * - 相位偏移<br>
     */
    public static class Oscillator extends AnimData {
        float value1;
        float value2;
        float frequency;
        float phaseShift;

        public Oscillator(float value1, float value2, float frequency, float phaseShift) {
            this.value1 = value1;
            this.value2 = value2;
            this.frequency = frequency;
            this.phaseShift = phaseShift;
        }

        public float evaluate(float t) {
            float a = (this.value2 - this.value1) / 2.0F;
            return (float)((double)(this.value1 + a) + (double)a * Math.cos((double)(t * this.frequency + this.phaseShift)));
        }
    }

    /**
     * · 动画数据项目<br>
     * <strong>关键帧轨道</strong><br>
     * 基于关键帧的插值动画系统。<br>
     * 支持：<br>
     * - 线性插值<br>
     * - 关键帧时间控制<br>
     * - 预设方法：<br>
     * >{@link KeyTrack#startAndEnd}: 起始和结束值<br>
     * >{@link KeyTrack#oscillate}: 振荡效果<br>
     * >{@link KeyTrack#uniform}: 均匀变化
     */
    public static class KeyTrack extends AnimData {
        float[] values;
        float[] times;

        public KeyTrack(float[] values, float[] times) {
            this.values = values;
            this.times = times;
            if (values.length != times.length) {
                System.out.println("Malformed key track. Must have same number of keys and values or key track will evaluate to 0.");
            }

        }

        public float evaluate(float t) {
            if (this.values.length != this.times.length) {
                return 0.0F;
            } else {
                for(int i = 0; i < this.times.length; ++i) {
                    float time = this.times[i];
                    if (t == time) {
                        return this.values[i];
                    }

                    if (t < time) {
                        if (i == 0) {
                            return this.values[0];
                        }

                        float a = (t - this.times[i - 1]) / (time - this.times[i - 1]);
                        return this.values[i - 1] * (1.0F - a) + this.values[i] * a;
                    }

                    if (i == this.values.length - 1) {
                        return this.values[i];
                    }
                }

                return 0.0F;
            }
        }

        public static KeyTrack startAndEnd(float startValue, float endValue) {
            return new KeyTrack(new float[]{startValue, endValue}, new float[]{0.0F, 1.0F});
        }

        public static KeyTrack oscillate(float value1, float value2, int frequency) {
            if (frequency <= 1) {
                new KeyTrack(new float[]{value1, value2}, new float[]{0.0F, 1.0F});
            }

            float step = 1.0F / (float)frequency;
            float[] times = new float[frequency + 1];
            float[] values = new float[frequency + 1];

            for(int i = 0; i < frequency + 1; ++i) {
                float value = i % 2 == 0 ? value1 : value2;
                times[i] = step * (float)i;
                values[i] = value;
            }

            return new KeyTrack(values, times);
        }
        public static KeyTrack uniform(float end, float start, float timeMax) {
            HashMap<Float, Float> rXt = new HashMap<>();
            float cR = 0;
            for(float time=0; time <= timeMax; time+=0.1f) {
                cR = start+(time * ((end-start)/timeMax));
                rXt.put(cR, time);
            }
            float[] r = new float[rXt.size()];
            float[] t = new float[rXt.size()];
            int i = 0;
            for(Map.Entry<Float, Float> entry : rXt.entrySet()) {
                r[i] = entry.getKey();
                t[i] = entry.getValue();
                i++;
            }

            return new KeyTrack(r, t);
        }
    }

    public abstract static class AnimData {
        public AnimData() {
        }

        public float evaluate(float t) {
            return 0.0F;
        }
    }
}
