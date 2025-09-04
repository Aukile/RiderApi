package net.ankrya.rider_api.item.base.armor;

import net.ankrya.rider_api.api.event.RiderArmorEquipEvent;
import net.ankrya.rider_api.api.event.RiderArmorRemoveEvent;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.item.data.ArmorData;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class BaseRiderArmor extends BaseRiderArmorBase {
    //干脆记下来算了？
    public final EquipmentSlot slot;
    public static final DataComponentType<ArmorData> BACKUP_ARMOR =
            DataComponentType.<ArmorData>builder().persistent(ArmorData.CODEC)
                    .networkSynchronized(ArmorData.STREAM_CODEC).build();

    public BaseRiderArmor(Holder<ArmorMaterial> material, Item.Properties properties, EquipmentSlot slot) {
        super(material, getType(slot), properties);
        this.slot = slot;
    }

    public static ArmorItem.Type getType(EquipmentSlot slot) {
        return switch (slot){
            case HEAD -> ArmorItem.Type.HELMET;
            case CHEST -> ArmorItem.Type.CHESTPLATE;
            case LEGS -> ArmorItem.Type.LEGGINGS;
            case FEET -> ArmorItem.Type.BOOTS;
            default -> throw new IllegalArgumentException("Invalid slot: " + slot);
        };
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof Player player){
            ItemStack carried = player.containerMenu.getCarried();
            if (carried.is(stack.getItem())) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
                return;
            }
        }
        if (entity instanceof LivingEntity livingEntity){
            if (allArmorEquip(livingEntity)){
                livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false));
                for (Map.Entry<Holder<MobEffect>, Integer> entry : getEffects().entrySet()){
                    if (entry.getKey() == MobEffects.NIGHT_VISION) livingEntity.addEffect(new MobEffectInstance(entry.getKey(), 240, entry.getValue(), false, false));
                    else livingEntity.addEffect(new MobEffectInstance(entry.getKey(), 10, entry.getValue(), false, false));
                }
            } else {
                if (entity instanceof Player player) {
                    if (player.getItemBySlot(slot) == stack) unequip(player, slot);
                    else {
                        ItemStack backupArmor = BaseRiderArmor.getBackupArmor(stack);
                        GJ.ToItem.playerRemoveItem(player, this, 1);
                        if (player.getItemBySlot(slot).isEmpty()) GJ.ToItem.equipBySlot(player, slot, backupArmor);
                        else ItemHandlerHelper.giveItemToPlayer(player, backupArmor);
                    }
                } else unequip(livingEntity, slot);
                for (Holder<MobEffect> effect : getEffects().keySet()){
                    livingEntity.removeEffect(effect);
                }
                livingEntity.removeEffect(MobEffects.INVISIBILITY);
            }
        }
    }

    public abstract Map<Holder<MobEffect>, Integer> getEffects();

    // 存储备用盔甲
    public static void storeBackupArmor(ItemStack storageArmor, ItemStack backupArmor) {
        storageArmor.set(BACKUP_ARMOR, ArmorData.fromItemStack(backupArmor));
    }

    // 获取备用盔甲
    public static ItemStack getBackupArmor(ItemStack storageArmor) {
        ArmorData data = storageArmor.get(BACKUP_ARMOR);
        return data != null ? data.toItemStack() : ItemStack.EMPTY;
    }

    /**
     * 装备对应槽位的骑士盔甲的方法<br>
     * 会触发{@link RiderArmorEquipEvent}
     */
    public static void equip(LivingEntity entity, EquipmentSlot slot, ItemStack stack){
        if (NeoForge.EVENT_BUS.post(new RiderArmorEquipEvent.Pre(entity, slot, stack)).canRun()){
            ItemStack original = entity.getItemBySlot(slot);
            if (!original.isEmpty()) storeBackupArmor(stack, original);
            if (entity instanceof Player player) {
                GJ.ToItem.equipBySlot(player, slot, stack);
            } else entity.setItemSlot(slot, stack);
            NeoForge.EVENT_BUS.post(new RiderArmorEquipEvent.Post(entity, slot, stack));
        }
    }

    /**
     * 解除盔甲装备时的方法<br>
     * 会触发{@link RiderArmorRemoveEvent}
     */
    public static void unequip(LivingEntity entity, EquipmentSlot slot){
        if (NeoForge.EVENT_BUS.post(new RiderArmorRemoveEvent.Pre(entity, slot)).canRun()){
            ItemStack stack = entity.getItemBySlot(slot);
            ItemStack backup = getBackupArmor(stack);
            entity.setItemSlot(slot, backup);
            NeoForge.EVENT_BUS.post(new RiderArmorRemoveEvent.Post(entity, slot));
        }
    }

    public EquipmentSlot getSlot() {
        return slot;
    }
}
