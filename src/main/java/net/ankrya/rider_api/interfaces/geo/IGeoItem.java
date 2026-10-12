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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    @OnlyIn(Dist.CLIENT)
    default PlayState singleSynAnimate(AnimationState<IGeoItem> state) {
        AnimationController<IGeoItem> controller = state.getController();
        ItemStack itemStack = state.getData(DataTickets.ITEMSTACK);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (itemStack == null) return PlayState.STOP;
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
     * 触发的动画名会存进物品 NBT {@link GeoItemAnimData#TRIGGERED_ANIMATION}，
     * 关闭游戏重进（或其他玩家后来才看到这个物品）时会自动恢复：<br>
     * hold_on_last_frame 的动画直接恢复到最后一帧；loop 的动画重新开始循环；play_once 的动画不恢复。<br>
     * 想回到默认动画请用 {@link IGeoItem#clearTriggeredAnimation}
     */
    default AnimationController<IGeoItem> synAnimate(String name){
        boolean[] restored = {false};
        AnimationController<IGeoItem> controller = new TriggerRestoreController<>(this, name, 0, state -> {
            // 控制器是按物品动画 id 创建的，新建（例如重进游戏）后第一次运行时检查一次就够了
            if (!restored[0]) {
                ItemStack itemStack = state.getData(DataTickets.ITEMSTACK);
                if (itemStack != null) {
                    restored[0] = true;
                    restoreTriggeredAnimation(state.getController(), itemStack);
                }
            }
            return PlayState.CONTINUE;
        });
        for (String animationName : getAllAnimationName())
            controller.triggerableAnim(animationName, RawAnimation.begin().thenPlay(animationName));
        return controller;
    }

    /** 根据物品上保存的触发动画恢复控制器状态（客户端） */
    private void restoreTriggeredAnimation(AnimationController<IGeoItem> controller, ItemStack itemStack) {
        String animation = GeoItemAnimData.get(itemStack);
        if (animation == null || animation.isEmpty() || !(controller instanceof TriggerRestoreController<?> restoreController))
            return;
        BakedAnimations animations = GeckoLibCache.getBakedAnimations().get(getAnimationFile());
        Animation baked = animations == null ? null : animations.getAnimation(animation);
        if (baked == null || baked.loopType() == Animation.LoopType.PLAY_ONCE)
            return; // 只播一次的动画早就播完了，不需要恢复
        restoreController.restoreTriggered(animation, baked.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME);
    }

    default PlayState idle(AnimationState<IGeoItem> state) {
        // 触发动画（"controller" 控制器）播放时 idle 停下，否则触发动画没覆盖到的骨骼会继续动
        if (state.getController().isPlayingTriggeredAnimation() || isMainControllerTriggered(state))
            return PlayState.STOP;
        state.getController().setAnimation(RawAnimation.begin().thenLoop(getAnimation()));
        return PlayState.CONTINUE;
    }

    private boolean isMainControllerTriggered(AnimationState<IGeoItem> state) {
        ItemStack itemStack = state.getData(DataTickets.ITEMSTACK);
        if (itemStack == null) return false;
        long id = GeoItem.getId(itemStack);
        if (id == Long.MAX_VALUE) return false;
        AnimationController<?> main = getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
        return main != null && main != state.getController() && main.isPlayingTriggeredAnimation();
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
            GeoItemAnimData.set(itemStack, animation);
            item.triggerAnim(entity, GeoItem.getOrAssignId(itemStack, serverLevel), "controller", animation);
        }
    }

    /**触发{@link IGeoItem#synAnimate}动画使用例*/
    static void triggerAnimation(ItemStack itemStack, Entity entity, String animation){
        if (itemStack.getItem() instanceof IGeoItem item){
            playAnimationAndReset(itemStack, animation);
            GeoItemAnimData.set(itemStack, animation);
            if (entity.level() instanceof ServerLevel serverLevel) {
                item.triggerAnim(entity, GeoItem.getOrAssignId(itemStack, serverLevel), "controller", animation);
            }
            else if (GJ.ToItem.getNbt(itemStack).contains(GeoItem.ID_NBT_KEY))
                item.triggerAnim(entity, GeoItem.getId(itemStack), "controller", animation);
        }
    }

    /**
     * 停止触发动画并清除保存的状态，回到默认（idle）动画。<br>
     * 停在最后一帧的动画需要用这个方法才能回去
     */
    static void clearTriggeredAnimation(ItemStack itemStack, Entity entity) {
        if (!(itemStack.getItem() instanceof IGeoItem item)) return;
        GeoItemAnimData.remove(itemStack);
        long id = GeoItem.getId(itemStack);
        if (id != Long.MAX_VALUE)
            item.stopTriggeredAnim(entity, id, "controller", null);
    }

    /** 获取保存的触发动画名（没有返回空字符串） */
    static String getTriggeredAnimation(ItemStack itemStack) {
        String animation = GeoItemAnimData.get(itemStack);
        return animation == null ? "" : animation;
    }

    /**获取当前播放的动画的动画名*/
    static String getNowAnimation(ItemStack itemStack){
        String name = GJ.ToItem.getNbt(itemStack).getString(NOW_ANIMATION);
        if (name.isEmpty()) name = GJ.ToItem.getNbt(itemStack).getString(ANIMATION);
        return name;
    }

    @Override
    default void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        if (this instanceof ArmorItem)
            controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
        else {
        controllerRegistrar.add(synAnimate("controller"));
        controllerRegistrar.add(new AnimationController<>(this, "idle", 0, this::idle));
        }
    }

    /**隐藏块（物品）*/
    @OnlyIn(Dist.CLIENT)
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
    @OnlyIn(Dist.CLIENT)
    default GeoRenderLayer<?>[] getRenderLayers(GeoRenderer<?> renderer) {
        return new GeoRenderLayer[0];
    }

    /**默认模型完整路径*/
    ResourceLocation getModel();

    /**默认动画完整路径*/
    ResourceLocation getAnimationFile();

    /**默认贴图完整路径*/
    ResourceLocation getTexture();

    @OnlyIn(Dist.CLIENT)
    default <T extends Item & IGeoItem> void withRender(BaseGeoItemRenderer<T> tBaseGeoItemRenderer, PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){}

    /** 需要发光渲染（beaconBeam）的骨骼名 */
    default <T extends Item & IGeoItem> java.util.Set<String> lightBones(BaseGeoItemRenderer<T> renderer) {
        return new java.util.HashSet<>();
    }

    /** 指定骨骼使用的渲染类型 */
    default <T extends Item & IGeoItem> Map<String, RenderType> boneByRenderType(BaseGeoItemRenderer<T> renderer) {
        return new HashMap<>();
    }
}
