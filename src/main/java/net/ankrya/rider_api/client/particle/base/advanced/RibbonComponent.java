package net.ankrya.rider_api.client.particle.base.advanced;

import net.ankrya.rider_api.client.particle.base.AdvancedParticleBase;
import net.ankrya.rider_api.client.particle.base.ParticleRibbon;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Range;

/**
 *
 * 拖尾粒子<br>
 * 控件列表：<br>
 * - {@link AttachToParticle}附加到粒子组件<br>
 * - {@link Trail}轨迹追踪组件<br>
 * - {@link PanTexture}纹理平移组件<br>
 * - {@link BeamPinning}光束固定组件<br>
 * - {@link PropertyOverLength}长度属性控制组件
 */
public class RibbonComponent extends ParticleComponent {
    int length;
    ParticleType<? extends RibbonParticleData> ribbon;
    double yaw;
    double pitch;
    double roll;
    double scale;
    double r;
    double g;
    double b;
    double a;
    boolean faceCamera;
    boolean emissive;
    ParticleComponent[] components;

    /**
     * @param length 长度
     * @param yaw y轴旋转
     * @param pitch x轴旋转
     * @param roll z轴旋转
     * @param scale 缩放
     * @param faceCamera 是否面向摄像机
     * @param emissive 是否发光
     * @param components 组件
     */
    public RibbonComponent(ParticleType<? extends RibbonParticleData> particle, @Range(from = 1, to = Integer.MIN_VALUE) int length, double yaw, double pitch, double roll, double scale, double r, double g, double b, double a, boolean faceCamera, boolean emissive, ParticleComponent[] components) {
        this.length = length;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.emissive = emissive;
        this.faceCamera = faceCamera;
        this.components = components;
        this.ribbon = particle;
    }

    public void init(AdvancedParticleBase particle) {
        super.init(particle);
        if (particle != null) {
            ParticleComponent[] newComponents = new ParticleComponent[this.components.length + 2];
            System.arraycopy(this.components, 0, newComponents, 0, this.components.length);
            newComponents[this.components.length] = new AttachToParticle(particle);
            newComponents[this.components.length + 1] = new Trail();
            ParticleRibbon.spawnRibbon(particle.getWorld(), this.ribbon, this.length, particle.getPosX(), particle.getPosY(), particle.getPosZ(), 0.0, 0.0, 0.0, this.faceCamera, this.yaw, this.pitch, this.roll, this.scale, this.r, this.g, this.b, this.a, 0.0, (double)(particle.getLifetime() + this.length), this.emissive, newComponents);
        }

    }

    /**
     * <strong>附加到粒子组件</strong><br>
     * 将此粒子与{@link AdvancedParticleBase}关联起来。<br>
     * 在初始化时将原粒子的ribbon属性设置为新创建的带状粒子，实现粒子间的链接关系。
     */
    private static class AttachToParticle extends ParticleComponent {
        AdvancedParticleBase attachedParticle;

        public AttachToParticle(AdvancedParticleBase attachedParticle) {
            this.attachedParticle = attachedParticle;
        }

        public void init(AdvancedParticleBase particle) {
            super.init(particle);
            this.attachedParticle.ribbon = (ParticleRibbon) particle;
        }
    }

    /**
     * <strong>轨迹追踪组件</strong><br>
     * 实现带状粒子的轨迹追踪效果。<br>
     * ps：原理：<br>
     * 在每次更新后，将带状粒子的所有位置点向前移动一位，最新的位置点设置为粒子当前的位置。<br>
     * 这样就形成了一个连续的轨迹带效果。<br>
     */
    public static class Trail extends ParticleComponent {
        public Trail() {
        }

        public void postUpdate(AdvancedParticleBase particle) {
            if (particle instanceof ParticleRibbon ribbon) {
                for(int i = ribbon.positions.length - 1; i > 0; --i) {
                    ribbon.positions[i] = ribbon.positions[i - 1];
                    ribbon.prevPositions[i] = ribbon.prevPositions[i - 1];
                }

                ribbon.positions[0] = new Vec3(ribbon.getPosX(), ribbon.getPosY(), ribbon.getPosZ());
                ribbon.prevPositions[0] = ribbon.getPrevPos();
            }

        }
    }

    /**
     * <strong>纹理平移组件</strong><br>
     * 实现纹理沿着带状粒子平移的效果。<br>
     * 通过计算粒子的生命周期进度，动态调整纹理坐标偏移量，创造出纹理流动的视觉效果。<br>
     */
    public static class PanTexture extends ParticleComponent {
        float startOffset = 0.0F;
        float speed = 1.0F;

        public PanTexture(float startOffset, float speed) {
            this.startOffset = startOffset;
            this.speed = speed;
        }

        public void preRender(AdvancedParticleBase particle, float partialTicks) {
            if (particle instanceof ParticleRibbon ribbon) {
                float time = (ribbon.getAge() - 1.0F + partialTicks) / (float)ribbon.getLifetime();
                float t = (this.startOffset + time * this.speed) % 1.0F;
                ribbon.texPanOffset = (ribbon.getMaxUPublic() - ribbon.getMinUPublic()) / 2.0F * t;
            }

        }
    }

    /**
     * <strong>光束固定组件</strong><br>
     * 将带状粒子固定在两个点之间，形成稳定的光束效果。<br>
     * 无论粒子如何更新，都会将带状粒子的各个点均匀分布在起始点和结束点之间，创造出直线光束效果。<br>
     * ps：应该是，常用于制作激光、连接线等特效。
     */
    public static class BeamPinning extends ParticleComponent {
        private final Vec3[] startLocation;
        private final Vec3[] endLocation;

        public BeamPinning(Vec3[] startLocation, Vec3[] endLocation) {
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }

        public void postUpdate(AdvancedParticleBase particle) {
            if (particle instanceof ParticleRibbon ribbon && this.validateLocation(this.startLocation) && this.validateLocation(this.endLocation)) {
                ribbon.setPos(this.startLocation[0].x(), this.startLocation[0].y(), this.startLocation[0].z());
                Vec3 increment = this.endLocation[0].subtract(this.startLocation[0]).scale(1.0F / (float)(ribbon.positions.length - 1));

                for(int i = 0; i < ribbon.positions.length; ++i) {
                    Vec3 newPos = this.startLocation[0].add(increment.scale(i));
                    ribbon.prevPositions[i] = ribbon.positions[i] == null ? newPos : ribbon.positions[i];
                    ribbon.positions[i] = newPos;
                }
            }

        }

        private boolean validateLocation(Vec3[] location) {
            return location != null && location.length >= 1 && location[0] != null;
        }
    }

    /**
     * <strong>长度属性控制组件</strong><br>
     * 控制带状粒子沿长度方向的属性变化。<br>
     * 通过AnimData定义的变化曲线，可以实现带状粒子从起点到终点的颜色渐变、透明度变化或粗细变化等效果。<br>
     * 支持的颜色和透明度属性包括：<br>
     * - red, green, blue: rgb颜色分量<br>
     * - alpha: 透明度<br>
     * - scale: 缩放<br>
     * @see EnumRibbonProperty
     */
    public static class PropertyOverLength extends ParticleComponent {
        private final ParticleComponent.AnimData animData;
        private final EnumRibbonProperty property;

        public PropertyOverLength(EnumRibbonProperty property, ParticleComponent.AnimData animData) {
            this.animData = animData;
            this.property = property;
        }

        public float evaluate(float t) {
            return this.animData.evaluate(t);
        }

        public EnumRibbonProperty getProperty() {
            return this.property;
        }

        public enum EnumRibbonProperty {
            /**颜色分量：红色*/
            RED,
            /**颜色分量：绿色*/
            GREEN,
            /**颜色分量：蓝色*/
            BLUE,
            /**透明度*/
            ALPHA,
            /**缩放*/
            SCALE;

            EnumRibbonProperty() {
            }
        }
    }
}
