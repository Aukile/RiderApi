package net.ankrya.rider_api.item.base.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * 骑士盔甲的最底板 <br>
 * 后面走向盔甲{@link BaseRiderArmor}和腰带{@link BaseDriver}两个分支了就 <br>
 * 这里主要是识别变身状态的方法
 */
public abstract class BaseRiderArmorBase extends BaseGeoArmor {
    protected Map<EquipmentSlot, Class<? extends BaseRiderArmorBase>> armorClass = Map.of(EquipmentSlot.HEAD, BaseRiderArmor.class, EquipmentSlot.CHEST, BaseRiderArmor.class, EquipmentSlot.LEGS, BaseDriver.class, EquipmentSlot.FEET, BaseRiderArmor.class);
    public BaseRiderArmorBase(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** 技能轮盘（{@link net.ankrya.rider_api.help.skill.SkillWheelScreen}）显示的技能列表，默认没有 */
    public java.util.List<net.ankrya.rider_api.help.skill.Skill> getSkillList(net.minecraft.world.entity.player.Player player) {
        return java.util.List.of();
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
        boolean equip = true;
        if (getArmorClass().containsKey(slot))
            equip = getArmorClass().get(slot).isAssignableFrom(slotToClass(entity, slot));
        return equip;
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

    public Map<EquipmentSlot, Class<? extends BaseRiderArmorBase>> getArmorClass() {
        return armorClass;
    }

    /** 渲染玩家时是否限制只渲染盔甲（见 PlayerRender），默认 true */
    public boolean limitToRenderArmor(net.minecraft.client.player.AbstractClientPlayer player, ItemStack chest) {
        return true;
    }
}
