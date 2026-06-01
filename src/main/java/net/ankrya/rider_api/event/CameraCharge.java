package net.ankrya.rider_api.event;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class CameraCharge {
    private static CameraType previousCameraType = null;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Player player = Minecraft.getInstance().player;
        AnimationApplier animationPlayer = ((IAnimatedPlayer) player).playerAnimator_getAnimation();
        animationPlayer.setTickDelta((float) event.getPartialTick());

        if (animationPlayer.isActive()) {
            Camera camera = event.getCamera();
            Vec3f pos = animationPlayer.get3DTransform("camera", TransformType.POSITION, Vec3f.ZERO);
            Vec3f rot = animationPlayer.get3DTransform("camera", TransformType.ROTATION, Vec3f.ZERO);
            if ((noVec3fZero(pos) || noVec3fZero(rot)) && previousCameraType == null) {
                previousCameraType = Minecraft.getInstance().options.getCameraType();
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            if (noVec3fZero(rot)) {
                event.setYaw(event.getYaw() + rot.getY());
                event.setPitch(event.getPitch() + rot.getX());
                event.setRoll(rot.getZ());
            }
            if (noVec3fZero(pos)) {
                Vec3 localOffset = new Vec3(pos.getX(), pos.getY(), pos.getZ());
                Vec3 worldOffset = localOffset
                        .xRot(-event.getPitch() * Mth.DEG_TO_RAD)
                        .yRot(-event.getYaw() * Mth.DEG_TO_RAD);

                camera.position = player.position().add(worldOffset);
            }
        } else if (previousCameraType != null) {
            Minecraft.getInstance().options.setCameraType(previousCameraType);
            previousCameraType = null;
            event.setYaw(player.getYRot());
            event.setPitch(player.getXRot());
            event.setRoll(0.0F);
        }
    }

    @SubscribeEvent
    public static void eventdo(ViewportEvent.ComputeFov event) {
        Player player = Minecraft.getInstance().player;
        AnimationApplier animationPlayer = ((IAnimatedPlayer) player).playerAnimator_getAnimation();
        animationPlayer.setTickDelta((float) event.getPartialTick());

        if (animationPlayer.isActive() && previousCameraType != null) {
            Vec3f fov = animationPlayer.get3DTransform("camera", TransformType.SCALE, Vec3f.ZERO);
            if (noVec3fZero(fov))
                event.setFOV(anyValue(fov));
        }
    }

    private static boolean noVec3fZero(Vec3f vec) {
        return vec != null && (vec.getX() != 0 || vec.getY() != 0 || vec.getZ() != 0);
    }

    private static double anyValue(Vec3f vec) {
        if (vec.getX() != 0)
            return vec.getX();
        if (vec.getY() != 0)
            return vec.getY();
        return vec.getZ();
    }
}
