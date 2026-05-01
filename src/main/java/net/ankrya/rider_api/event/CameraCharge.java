package net.ankrya.rider_api.event;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber
public class CameraCharge {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Player player = Minecraft.getInstance().player;
        AnimationApplier animationPlayer = ((IAnimatedPlayer) player).playerAnimator_getAnimation();
        animationPlayer.setTickDelta((float) event.getPartialTick());
        if (animationPlayer.isActive()) {
            Camera camera = event.getCamera();
            Vec3f pos = animationPlayer.get3DTransform("camera", TransformType.POSITION, Vec3f.ZERO);
            Vec3 pos1 = new Vec3(pos.getX(), -pos.getY(), pos.getZ());
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            Vec3 transformedOffset = transformToViewSpace(pos1.scale(0.075), yaw);
            if(pos.getX()!=0||pos.getY()!=0||pos.getZ()!=0)
                camera.position=new Vec3(player.getX()+transformedOffset.x,player.getEyeY()+transformedOffset.y-0.05,player.getZ()+transformedOffset.z);
            Vec3f rot = animationPlayer.get3DTransform("camera", TransformType.ROTATION, Vec3f.ZERO);
            if(rot.getY()!=0||rot.getX()!=0||rot.getZ()!=0) {
                float deltaYaw = (float) Math.toDegrees(rot.getY());
                float newYaw = yaw + Math.abs(deltaYaw); // 修正为加法
                event.setYaw(newYaw);
                //System.out.println(newYaw);

                float deltaPitch = (float) Math.toDegrees(rot.getX());
                float newPitch = pitch + Math.abs(deltaPitch); // 修正为加法
                event.setPitch(newPitch);

                event.setRoll((float) Math.toDegrees(rot.getZ()));
            }
        }
    }
    //矩阵旋转坐标轴将相机移动向量进行转换
    public static Vec3 transformToViewSpace(Vec3 originalVec, float yawDegrees) {
        // 恢复 yawDegrees - 90 的偏移
        double theta = Math.toRadians(yawDegrees);
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        double x = originalVec.x;
        double z = originalVec.z;

        double xTransformed = x * cosTheta + z * sinTheta;
        double zTransformed = x * sinTheta - z * cosTheta;

        return new Vec3(xTransformed, originalVec.y, zTransformed);
    }
}
