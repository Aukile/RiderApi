package net.ankrya.rider_api.item.base.armor;

import net.ankrya.rider_api.item.renderer.base.BaseRiderArmorRender;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.function.Consumer;

/**
 * 骑士盔甲的最底板 <br>
 * 后面走向盔甲{@link BaseRiderArmor}和腰带{@link BaseDriver}两个分支了就 <br>
 * 这里主要是识别变身状态的方法
 */
public abstract class BaseRiderArmorBase extends BaseGeoArmor {
    protected Class<? extends BaseDriver> driverClass = BaseDriver.class;
    protected Class<? extends BaseRiderArmor> armorClass = BaseRiderArmor.class;
    public BaseRiderArmorBase(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BaseRiderArmorRender<?> renderer;

            @Override
            public <T extends LivingEntity> @NotNull HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (renderer == null) renderer = new BaseRiderArmorRender<>();
                return renderer;
            }
        });
    }

    public static EquipmentSlot[] getSlots() {
        return new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET};
    }

    public static EquipmentSlot[] getAllSlots() {
        return new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    }

    public static Class<?> slotToClass(LivingEntity entity, EquipmentSlot slot){
        return entity.getItemBySlot(slot).getItem().getClass();
    }

    public boolean armorEquip(LivingEntity entity, EquipmentSlot slot){
        return getArmorClass().isAssignableFrom(slotToClass(entity, slot));
    }

    public boolean allArmorEquip(LivingEntity entity){
        boolean equip = true;
        for (EquipmentSlot slot : getAllSlots()){
            if (!armorEquip(entity, slot)){
                equip = false;
                break;
            }
        }
        return equip;
    }

    public static boolean isAllEquip(LivingEntity entity, ItemStack stack){
        return stack.getItem() instanceof BaseRiderArmorBase armor && armor.allArmorEquip(entity);
    }

    public static boolean isAllEquip(LivingEntity entity){
        return isAllEquip(entity, entity.getItemBySlot(EquipmentSlot.LEGS));
    }

    public Class<? extends BaseDriver> getDriverClass() {
        return driverClass;
    }

    public Class<? extends BaseRiderArmor> getArmorClass() {
        return armorClass;
    }
}
