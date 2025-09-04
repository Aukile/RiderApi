package net.ankrya.rider_api.client.particle.base.advanced;

import net.minecraft.world.phys.Vec3;

/**
 * 拖尾粒子（虽然功能不止）<br>
 * 也可加组件<br>
 * @see RibbonComponent
 */
public abstract class ParticleRotation {
    public ParticleRotation() {
    }

    public void setPrevValues() {
    }

    /**向量定向*/
    public static class OrientVector extends ParticleRotation {
        public Vec3 orientation;
        public Vec3 prevOrientation;

        public OrientVector(Vec3 orientation) {
            this.orientation = this.prevOrientation = orientation;
        }

        public void setPrevValues() {
            this.prevOrientation = this.orientation;
        }
    }

    /**欧拉角*/
    public static class EulerAngles extends ParticleRotation {
        public float yaw;
        public float pitch;
        public float roll;
        public float prevYaw;
        public float prevPitch;
        public float prevRoll;

        /**
         * @param yaw y轴旋转角度
         * @param pitch x轴旋转角度
         * @param roll z轴旋转角度
         */
        public EulerAngles(float yaw, float pitch, float roll) {
            this.yaw = this.prevYaw = yaw;
            this.pitch = this.prevPitch = pitch;
            this.roll = this.prevRoll = roll;
        }

        public void setPrevValues() {
            this.prevYaw = this.yaw;
            this.prevPitch = this.pitch;
            this.prevRoll = this.roll;
        }
    }

    /**面向摄像机*/
    public static class FaceCamera extends ParticleRotation {
        public float faceCameraAngle;
        public float prevFaceCameraAngle;

        /**@param faceCameraAngle 旋转角度*/
        public FaceCamera(float faceCameraAngle) {
            this.faceCameraAngle = faceCameraAngle;
        }

        public void setPrevValues() {
            this.prevFaceCameraAngle = this.faceCameraAngle;
        }
    }
}

