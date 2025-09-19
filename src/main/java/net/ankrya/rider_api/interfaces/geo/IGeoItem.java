package net.ankrya.rider_api.interfaces.geo;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.item.renderer.base.BaseGeoArmorRenderer;
import net.ankrya.rider_api.item.renderer.base.BaseGeoItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**懒狗化GeoItem~*/
public interface IGeoItem extends GeoItem {
    /**nbt更改动画使用*/
    String ANIMATION = "run_animation";
    /**nbt重置动画使用，使用{@link IGeoItem#playAnimationAndReset}即可*/
    String ANIMATION_STOP = "animation_stop";

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
                if (isArmor && armorRenderer == null) armorRenderer = new BaseGeoArmorRenderer<>(IGeoItem.this);
                return armorRenderer;
            }
        });
    }

    /**播放动画*/
    static void playAnimation(ItemStack itemStack, String animation){
        GJ.ToItem.setNbt(itemStack, nbt -> nbt.putString(ANIMATION, animation));
    }

    /**播放动画并重置*/
    static void playAnimationAndReset(ItemStack itemStack, String animation){
        GJ.ToItem.setNbt(itemStack, nbt -> nbt.putBoolean(ANIMATION_STOP, true));
        playAnimation(itemStack, animation);
    }

    private PlayState predicate(AnimationState<IGeoItem> state) {
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

    @Override
    default void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    /**默认动画*/
    default String getAnimation(ItemStack stack) {
        String animation = GJ.ToItem.getNbt(stack).getString(ANIMATION);
        return animation.isEmpty() ? getAnimation() : animation;
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
}
