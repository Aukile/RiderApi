package net.ankrya.rider_api.interfaces.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmorBase;
import net.ankrya.rider_api.item.renderer.base.BaseGeoArmorRenderer;
import net.ankrya.rider_api.item.renderer.base.BaseGeoItemRenderer;
import net.ankrya.rider_api.item.renderer.base.BaseRiderArmorRender;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.GeoItemIdAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**懒狗化GeoItem~*/
public interface IGeoItem extends GeoItem {
    /**nbt更改动画使用（{@link IGeoItem#triggerAnimation}用不到）*/
    String ANIMATION = "run_anim";
    /**nbt重置动画使用，使用{@link IGeoItem#playAnimationAndReset}即可*/
    String ANIMATION_STOP = "anim_stop";
    String NOW_ANIMATION = "now_anim";
    String MASTER = "master";

    default void createGeoRenderer(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final boolean isArmor = IGeoItem.this instanceof ArmorItem;
            private BaseGeoItemRenderer<?> itemRenderer;
            private BaseGeoArmorRenderer<?> armorRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (!isArmor && itemRenderer == null) itemRenderer = new BaseGeoItemRenderer<>(IGeoItem.this);
                return itemRenderer;
            }

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (isArmor && armorRenderer == null) {
                    if (IGeoItem.this instanceof BaseRiderArmorBase) armorRenderer = new BaseRiderArmorRender<>();
                    else armorRenderer = new BaseGeoArmorRenderer<>(IGeoItem.this);
                }
                this.armorRenderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return armorRenderer;
            }
        });
    }

    /**播放动画*/
    static void playAnimation(ItemStack itemStack, String animation){
        GJ.ToItem.setNbt(itemStack, nbt -> nbt.putString(ANIMATION, animation));
    }


    static void playAnimationAndReset(ItemStack itemStack, String animation){
        GJ.ToItem.setNbt(itemStack, nbt -> nbt.putBoolean(ANIMATION_STOP, true));
        playAnimation(itemStack, animation);
    }

    /**动画设置（仅本地，全局生效）*/
    default PlayState predicate(AnimationState<IGeoItem> state) {
        AnimationController<IGeoItem> controller = state.getController();
        ItemStack itemStack = state.getData(DataTickets.ITEMSTACK);

        if (itemStack != null && GJ.ToItem.getNbt(itemStack).getBoolean(ANIMATION_STOP)) {
            GJ.ToItem.setNbt(itemStack, nbt -> nbt.putBoolean(ANIMATION_STOP, false));
            controller.stop();
        }

        controller.setAnimation(RawAnimation.begin().then(getAnimation(itemStack), Animation.LoopType.PLAY_ONCE));
        if(controller.getAnimationState() == AnimationController.State.STOPPED)
            state.resetCurrentAnimation();
        return PlayState.CONTINUE;
    }

    /**通用性动画触发器，使用方法{@link IGeoItem#playAnimation}或{@link IGeoItem#playAnimationAndReset}触发*/
    default PlayState singleSynAnimate(AnimationState<IGeoItem> state) {
        AnimationController<IGeoItem> controller = state.getController();
        ItemStack itemStack = state.getData(DataTickets.ITEMSTACK);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null){
            if (!(server == null)){
                ResourceKey<Level> dimension = player.level().dimension();
                GeoItem.getOrAssignId(itemStack, server.getLevel(dimension));
            }

            String animation = getAnimation(itemStack);
            if (GJ.ToItem.getNbt(itemStack).getBoolean(ANIMATION_STOP)) {
                GJ.ToItem.setNbt(itemStack, nbt -> nbt.putBoolean(ANIMATION_STOP, false));
                controller.stop();
            }

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                controller.triggerableAnim(animation, RawAnimation.begin().thenPlay(animation));
                controller.receiveTriggeredAnimations();

                if (player.getInventory().contains(itemStack) && !controller.isPlayingTriggeredAnimation())
                    MessageLoader.getApiLoader().sendToServer(new GeoItemIdAnimation(itemStack, animation, player.getUUID()));
            }

        } else return PlayState.STOP;

        if (controller.getCurrentAnimation() != null)
            GJ.ToItem.setNbt(itemStack, nbt -> nbt.putString(NOW_ANIMATION, controller.getCurrentAnimation().animation().name()));
        else GJ.ToItem.setNbt(itemStack, nbt -> nbt.putString(NOW_ANIMATION, ""));
        return PlayState.CONTINUE;
    }

    /**
     * 同步动画触发器
     * 请必须写好全部的动画名在{@link IGeoItem#getAllAnimationName}(默认已自动解析)<br>
     * 使用{@link IGeoItem#triggerAnimation}调用动画<br>
     * <strong>！注意！</strong><br>
     * 因为动画是一次性触发导致动画无法在关闭游戏后仍然保留<br>
     * 即关闭游戏重进之后会恢复至默认状态
     */
    default PlayState synAnimate(AnimationState<IGeoItem> state){
        AnimationController<IGeoItem> controller = state.getController();
        for (String animation : getAllAnimationName())
            controller.triggerableAnim(animation, RawAnimation.begin().thenPlayAndHold(animation));
        return PlayState.CONTINUE;
    }

    private String getAnimation(ItemStack itemStack) {
        String animation = GJ.ToItem.getNbt(itemStack).getString(ANIMATION);
        return animation.isEmpty() ? getAnimation() : animation;
    }

    /**全部动画名，配合{@link IGeoItem#synAnimate}使用*/
    default List<String> getAllAnimationName() {
        BakedAnimations animations = GeckoLibCache.getBakedAnimations().get(getAnimationFile());
        return animations.animations().keySet().stream().toList();
    }

    /**触发{@link IGeoItem#synAnimate}动画使用例（更安全）*/
    static void triggerAnimation(ItemStack itemStack, Entity entity, ServerLevel serverLevel, String animation){
        if (itemStack.getItem() instanceof IGeoItem item){
            playAnimationAndReset(itemStack, animation);
            item.triggerAnim(entity, GeoItem.getOrAssignId(itemStack, serverLevel), "controller", animation);
        }
    }

    /**触发{@link IGeoItem#synAnimate}动画使用例*/
    static void triggerAnimation(ItemStack itemStack, Entity entity, String animation){
        if (itemStack.getItem() instanceof IGeoItem item){
            playAnimationAndReset(itemStack, animation);
            if (entity.level() instanceof ServerLevel serverLevel) {
                item.triggerAnim(entity, GeoItem.getOrAssignId(itemStack, serverLevel), "controller", animation);
            }
            else if (GJ.ToItem.getNbt(itemStack).contains(GeoItem.ID_NBT_KEY))
                item.triggerAnim(entity, GeoItem.getId(itemStack), "controller", animation);
        }
    }

    /**获取当前播放的动画的动画名*/
    static String getNowAnimation(ItemStack itemStack){
        String name = GJ.ToItem.getNbt(itemStack).getString(NOW_ANIMATION);
        if (name.isEmpty()) name = GJ.ToItem.getNbt(itemStack).getString(ANIMATION);
        return name;
    }

    @Override
    default void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::synAnimate));
    }

    /**隐藏块（物品）*/
    default Map<String, Boolean> visibilityBones(BaseGeoItemRenderer<?> renderer) {return new HashMap<>();}

    /**是否自动识别 “_glowmask” 后缀发光，启用后必须保证贴图存在*/
    default boolean autoGlow() {return false;}

    /**默认动画名设置*/
    default String getAnimation() {
        return "idle";
    }

    /**改渲染*/
    default RenderType getRenderType(ResourceLocation texture) {
        return null;
    }

    /**添加Layer渲染*/
    default GeoRenderLayer<?>[] getRenderLayers(GeoRenderer<?> renderer) {
        return new GeoRenderLayer[0];
    }

    /**默认模型完整路径*/
    ResourceLocation getModel();

    /**默认动画完整路径*/
    ResourceLocation getAnimationFile();

    /**默认贴图完整路径*/
    ResourceLocation getTexture();

    default <T extends Item & IGeoItem> void withRender(BaseGeoItemRenderer<T> tBaseGeoItemRenderer, PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){}
}
