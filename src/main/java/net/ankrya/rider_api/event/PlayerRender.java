package net.ankrya.rider_api.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.Arrays;
import java.util.Objects;

@EventBusSubscriber(Dist.CLIENT)
public class PlayerRender {
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void renderHandEvent(RenderArmEvent event) {
        if (!ModList.get().isLoaded("geckolib")) return;

        Minecraft mc = Minecraft.getInstance();

        AbstractClientPlayer player = event.getPlayer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = mc.getFrameTimeNs();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?> entityrenderer = entityrenderdispatcher.getRenderer(player);
        PlayerModel<AbstractClientPlayer> playermodel = ((PlayerRenderer)entityrenderer).getModel();
        poseStack.pushPose();

        if(chest.getItem() instanceof GeoItem items){
            GeoArmorRenderer geoArmorRender = (GeoArmorRenderer) getArmorModelHook(player, chest, EquipmentSlot.CHEST, playermodel);

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(geoArmorRender.getTextureLocation(items)));
            RenderType renderType = RenderType.entityTranslucent(geoArmorRender.getTextureLocation(items));

            GeoModel geoModel = geoArmorRender.getGeoModel();
            BakedGeoModel model = geoModel.getBakedModel(geoModel.getModelResource(items, geoArmorRender));

            SetAllBoneNoVisible(geoArmorRender);
            GeoBone right =  geoArmorRender.getRightArmBone(geoModel);
            if (right != null) {
                right.setHidden(event.getArm() != HumanoidArm.RIGHT);
                right.updateRotation(0, 0, 0);
                right.updatePosition(0, 0, 0);
            }

            GeoBone left =  geoArmorRender.getLeftArmBone(geoModel);
            if (left != null) {
                left.setHidden(event.getArm() != HumanoidArm.LEFT);
                left.updateRotation(0, 0, 0);
                left.updatePosition(0, 0, 0);
            }
            geoArmorRender.actuallyRender(poseStack, (Item) items,model,renderType,bufferSource, buffer,true,partialTick, packedLight, OverlayTexture.NO_OVERLAY, -1);
            event.setCanceled(true);
        }

        poseStack.popPose();
    }

    private static <T extends Item & GeoAnimatable & GeoItem> void SetAllBoneNoVisible(GeoArmorRenderer<T> render){
        GeoModel<T> model = render.getGeoModel();
        try {
            for (GeoBone geoBone : Arrays.asList(render.getHeadBone(model)
                    , render.getBodyBone(model), render.getRightLegBone(model)
                    , render.getRightBootBone(model), render.getLeftLegBone(model)
                    , render.getLeftBootBone(model))) {
                if (geoBone != null) {
                    geoBone.setHidden(true);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void renderPlayerEvent(RenderPlayerEvent.Pre event) {
        if (!ModList.get().isLoaded("geckolib")) return;
        Player player = event.getEntity();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?> entityrenderer = entityrenderdispatcher.getRenderer(player);
        PlayerModel<AbstractClientPlayer> playermodel = ((net.minecraft.client.renderer.entity.player.PlayerRenderer)entityrenderer).getModel();
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leg = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boot = player.getItemBySlot(EquipmentSlot.FEET);
        if(head.getItem() instanceof GeoItem || chest.getItem() instanceof GeoItem || leg.getItem() instanceof GeoItem ||
                boot.getItem() instanceof GeoItem)
            if(head != ItemStack.EMPTY || chest != ItemStack.EMPTY || leg != ItemStack.EMPTY || boot != ItemStack.EMPTY){
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

    private static <T extends LivingEntity> HumanoidModel<?> getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<T> model) {
        return GeoRenderProvider.of(itemStack).getGeoArmorRenderer(entity, itemStack, slot, model);
    }

    @SubscribeEvent
    public static void renderPlayerEvent(RenderHandEvent event) {
        if (!ModList.get().isLoaded("geckolib")) return;
        if(Minecraft.getInstance().player == null) return;
        Minecraft mc = Minecraft.getInstance();

        LocalPlayer player = mc.player;
        PoseStack poseStack = event.getPoseStack();
        int packedLight =  event.getPackedLight();
        float partialTicks = event.getPartialTick();
        float equipProgress = event.getEquipProgress();
        float swingProgress = event.getSwingProgress();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();

        MultiBufferSource buffsource = event.getMultiBufferSource();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        if(chest.getItem() instanceof GeoItem){
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


    public static void renderPlayerArm(PoseStack p_109347_, MultiBufferSource p_109348_, int p_109349_, float p_109350_, float p_109351_, HumanoidArm p_109352_){
        boolean flag = p_109352_ != HumanoidArm.RIGHT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(p_109351_);
        float f2 = -0.3F * Mth.sin(f1 * (float)Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float)Math.PI * 2F));
        float f4 = -0.4F * Mth.sin(p_109351_ * (float)Math.PI);
        p_109347_.translate(f * (f2 + 0.64000005F), f3 + -0.6F + p_109350_ * -0.6F, f4 + -0.71999997F);
        p_109347_.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(p_109351_ * p_109351_ * (float)Math.PI);
        float f6 = Mth.sin(f1 * (float)Math.PI);
        p_109347_.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
        p_109347_.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayer abstractclientplayer = Minecraft.getInstance().player;
        RenderSystem.setShaderTexture(0, Objects.requireNonNull(abstractclientplayer).getSkin().texture());
        p_109347_.translate(f * -1.0F, 3.6F, 3.5F);
        p_109347_.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        p_109347_.mulPose(Axis.XP.rotationDegrees(200.0F));
        p_109347_.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        p_109347_.translate(f * 5.6F, 0.0F, 0.0F);

        net.minecraft.client.renderer.entity.player.PlayerRenderer playerrenderer = (net.minecraft.client.renderer.entity.player.PlayerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(abstractclientplayer);
        if (flag) {
            playerrenderer.renderRightHand(p_109347_, p_109348_, p_109349_, abstractclientplayer);
        } else {
            playerrenderer.renderLeftHand(p_109347_, p_109348_, p_109349_, abstractclientplayer);
        }
    }
}
