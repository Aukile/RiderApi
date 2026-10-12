package net.ankrya.rider_api.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.ankrya.rider_api.compat.animation.PlayerAnimator;
import net.ankrya.rider_api.effect.EffectAnchor;
import net.ankrya.rider_api.effect.PlayerEffect;
import net.ankrya.rider_api.effect.PlayerEffectManager;
import net.ankrya.rider_api.effect.render.PlayerEffectRenderer;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmor;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmorBase;
import net.ankrya.rider_api.item.renderer.base.BaseGeoArmorRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PlayerRender {
    private static boolean geckolib;

    public static boolean isGeckolib() {
        geckolib = ModList.get().isLoaded("geckolib");
        return geckolib;
    }

    @SubscribeEvent
    public static void renderHandEvent(RenderArmEvent event) {
        if (!isGeckolib()) return;

        Minecraft mc = Minecraft.getInstance();

        AbstractClientPlayer player = event.getPlayer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = mc.getFrameTime();

        EquipmentSlot slot = EquipmentSlot.CHEST;
        ItemStack chest = player.getItemBySlot(slot);
        boolean otherLimit = true;
        if (chest.getItem() instanceof BaseRiderArmorBase armor) {
            otherLimit = armor.limitToRenderArmor(player, chest);
        }
        renderArm(event, player, slot, poseStack, bufferSource, partialTick, packedLight, otherLimit);
    }

    public static void renderArm(RenderArmEvent event, AbstractClientPlayer player, EquipmentSlot slot, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, boolean otherLimit) {
        ItemStack chest = player.getItemBySlot(slot);
        if (chest == null) return;
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?> entityrenderer = entityrenderdispatcher.getRenderer(player);
        PlayerModel<AbstractClientPlayer> playermodel = ((PlayerRenderer) entityrenderer).getModel();
        poseStack.pushPose();

        if (!chest.isEmpty() && chest.getItem() instanceof GeoItem items && otherLimit) {
            GeoArmorRenderer geoArmorRender = (GeoArmorRenderer) getArmorModelHook(player, chest, slot, playermodel);

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(geoArmorRender.getTextureLocation(items)));
            RenderType renderType = RenderType.entityTranslucent(geoArmorRender.getTextureLocation(items));
            Color renderColor = Color.WHITE;

            BakedGeoModel model = geoArmorRender.getGeoModel().getBakedModel(geoArmorRender.getGeoModel().getModelResource(items));

            SetAllBoneNoVisible(geoArmorRender);
            GeoBone right = geoArmorRender.getRightArmBone();
            // 安装了 playerAnimator 且当前动画使用第三人称模型时，不锁定手臂骨骼（让动画姿势在第一人称也生效）
            boolean needLock = !PlayerAnimator.instance().installed()
                    || !(player instanceof IAnimatedPlayer animated)
                    || animated.playerAnimator_getAnimation().getFirstPersonMode() != FirstPersonMode.THIRD_PERSON_MODEL;
            if (right != null) {
                right.setHidden(event.getArm() != HumanoidArm.RIGHT);
                if (needLock) {
                    right.updateRotation(0, 0, 0);
                    right.updatePosition(0, 0, 0);
                }
            }

            GeoBone left = geoArmorRender.getLeftArmBone();
            if (left != null) {
                left.setHidden(event.getArm() != HumanoidArm.LEFT);
                if (needLock) {
                    left.updateRotation(0, 0, 0);
                    left.updatePosition(0, 0, 0);
                }
            }
            geoArmorRender.actuallyRender(poseStack, (Item) items, model, renderType, bufferSource, buffer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderColor.getRed() / 255f, renderColor.getGreen() / 255f,
                    renderColor.getBlue() / 255f, renderColor.getAlpha() / 255f);
            event.setCanceled(true);
        }

        poseStack.popPose();
    }

    public static void SetAllBoneNoVisible(GeoArmorRenderer<?> render) {
        try {
            for (GeoBone geoBone : Arrays.asList(render.getHeadBone()
                    , render.getBodyBone(), render.getRightLegBone()
                    , render.getRightBootBone(), render.getLeftLegBone()
                    , render.getLeftBootBone())) {
                // 原来遇到第一个不存在的骨骼就抛异常，后面的骨骼都不会被隐藏
                if (geoBone != null)
                    geoBone.setHidden(true);
            }
            if (render instanceof BaseGeoArmorRenderer<?> renderer && renderer.getGeoArmorInterface() != null) {
                Map<String, Boolean> map = renderer.getGeoArmorInterface().visibilityBones(renderer);
                for (Map.Entry<String, Boolean> entry : map.entrySet())
                    renderer.getGeoModel().getBone(entry.getKey()).ifPresent(bone -> bone.setHidden(entry.getValue()));
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        PlayerEffectManager.get().clearAll();
    }

    /** 主分支写在 RegisterClientEvent 里；1.20 的 RegisterClientEvent 在 MOD 总线上，收不到这个事件，所以放在这里 */
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        net.ankrya.rider_api.compat.animation.GeoArmorBender.clearAll();
        PlayerEffectManager.get().clearAll();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!Minecraft.getInstance().isPaused()) PlayerEffectManager.get().tick();
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        List<PlayerEffect> effects = PlayerEffectManager.get().getPlayerEffects(player.getUUID());
        if (effects.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        float partialTick = event.getPartialTick();
        int packedLight = event.getPackedLight();

        for (PlayerEffect effect : effects) {
            poseStack.pushPose();
            if (effect.getConfig().anchor() instanceof EffectAnchor.FollowPlayer follow) {
                poseStack.translate(follow.offsetX(), follow.offsetY(), follow.offsetZ());
                poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot)));
            }

            PlayerEffectRenderer.getInstance().render(
                    effect, poseStack, bufferSource, packedLight, partialTick);

            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || Minecraft.getInstance().level == null) {
            return;
        }

        List<PlayerEffect> effects = PlayerEffectManager.get().getWorldEffects();
        if (effects.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        float partialTick = event.getPartialTick();

        for (PlayerEffect effect : effects) {
            if (!(effect.getConfig().anchor() instanceof EffectAnchor.WorldPosition worldPos)) {
                continue;
            }

            Vec3 effectPos = worldPos.getPosition();

            // 距离过远则跳过
            if (cameraPos.distanceToSqr(effectPos) > 4096.0) continue;

            poseStack.pushPose();
            // 转换到世界坐标（相对摄像机）
            poseStack.translate(
                    effectPos.x - cameraPos.x,
                    effectPos.y - cameraPos.y,
                    effectPos.z - cameraPos.z
            );

            int packedLight = net.minecraft.client.renderer.LevelRenderer.getLightColor(Minecraft.getInstance().level, BlockPos.containing(effectPos));

            PlayerEffectRenderer.getInstance().render(
                    effect, poseStack, bufferSource, packedLight, partialTick);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    @SubscribeEvent
    public static void renderPlayerEvent(RenderPlayerEvent.Pre event) {
        if (!isGeckolib()) return;
        if (event.getEntity() == null) return;
        Player player = event.getEntity();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?> entityrenderer = entityrenderdispatcher.getRenderer(player);
        PlayerModel<AbstractClientPlayer> playermodel = ((PlayerRenderer) entityrenderer).getModel();
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leg = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boot = player.getItemBySlot(EquipmentSlot.FEET);
        if (needVisiblePlayerPart(head, player) || needVisiblePlayerPart(chest, player) || needVisiblePlayerPart(leg, player) || needVisiblePlayerPart(boot, player))
            if (head != ItemStack.EMPTY || chest != ItemStack.EMPTY || leg != ItemStack.EMPTY || boot != ItemStack.EMPTY) {
                playermodel.head.visible = !(head.getItem() instanceof GeoItem);
                playermodel.hat.visible = !(head.getItem() instanceof GeoItem);
                playermodel.body.visible = !(chest.getItem() instanceof GeoItem);
                playermodel.rightArm.visible = !(chest.getItem() instanceof GeoItem);
                playermodel.leftArm.visible = !(chest.getItem() instanceof GeoItem);
                playermodel.rightLeg.visible = !(boot.getItem() instanceof GeoItem);
                playermodel.leftLeg.visible = !(boot.getItem() instanceof GeoItem);
                playermodel.leftSleeve.visible = !(chest.getItem() instanceof GeoItem);
                playermodel.rightSleeve.visible = !(chest.getItem() instanceof GeoItem);
                playermodel.leftPants.visible = !(boot.getItem() instanceof GeoItem);
                playermodel.rightPants.visible = !(boot.getItem() instanceof GeoItem);
                playermodel.jacket.visible = !(chest.getItem() instanceof GeoItem);
            }
    }

    public static boolean needVisiblePlayerPart(ItemStack stack, Player player) {
        return stack.getItem() instanceof BaseRiderArmor riderArmor && riderArmor.needInvisibility(player.level(), player, stack, riderArmor.getSlot());
    }

    private static net.minecraft.client.model.Model getArmorModelHook(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel model) {
        return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    }

    @SubscribeEvent
    public static void renderPlayerEvent(RenderHandEvent event) {
        if (!isGeckolib()) return;
        if (Minecraft.getInstance().player == null) return;
        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;
        PoseStack poseStack = event.getPoseStack();
        int packedLight = event.getPackedLight();
        float partialTicks = event.getPartialTick();
        float equipProgress = event.getEquipProgress();
        float swingProgress = event.getSwingProgress();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();

        MultiBufferSource buffsource = event.getMultiBufferSource();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        if (chest.getItem() instanceof GeoItem) {
            poseStack.pushPose();
            if (!player.isScoping()) {
                if (stack.isEmpty()) {
                    if (hand == InteractionHand.MAIN_HAND) {
                        renderPlayerArm(poseStack, buffsource, packedLight, equipProgress, swingProgress,
                                player.getMainArm().getOpposite()
                        );
                    }
                }
            }
            poseStack.popPose();
        }
    }


    public static void renderPlayerArm(PoseStack p_109347_, MultiBufferSource p_109348_, int p_109349_, float p_109350_, float p_109351_, HumanoidArm p_109352_) {
        boolean flag = p_109352_ != HumanoidArm.RIGHT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(p_109351_);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * Mth.sin(p_109351_ * (float) Math.PI);
        p_109347_.translate(f * (f2 + 0.64000005F), f3 + -0.6F + p_109350_ * -0.6F, f4 + -0.71999997F);
        p_109347_.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(p_109351_ * p_109351_ * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        p_109347_.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
        p_109347_.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayer abstractclientplayer = Minecraft.getInstance().player;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
        p_109347_.translate(f * -1.0F, 3.6F, 3.5F);
        p_109347_.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        p_109347_.mulPose(Axis.XP.rotationDegrees(200.0F));
        p_109347_.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        p_109347_.translate(f * 5.6F, 0.0F, 0.0F);

        PlayerRenderer playerrenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(abstractclientplayer);
        if (flag) {
            playerrenderer.renderRightHand(p_109347_, p_109348_, p_109349_, abstractclientplayer);
        } else {
            playerrenderer.renderLeftHand(p_109347_, p_109348_, p_109349_, abstractclientplayer);
        }
    }
}
