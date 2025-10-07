package net.ankrya.rider_api.mixin;

import net.ankrya.rider_api.api.event.ArmorBrokenEvent;
import net.ankrya.rider_api.help.GJ;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hurtAndBreak",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 1))
    public <T extends LivingEntity> void hurtAndBreak(int p_41623_, T entity, Consumer<T> consumer, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        MinecraftForge.EVENT_BUS.post(new ArmorBrokenEvent(entity, itemStack));
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        if (entity instanceof Player player){
            ItemStack itemStack = (ItemStack) (Object) this;
            boolean remove = GJ.ToItem.getNbt(itemStack).getBoolean(GJ.ToItem.REMOVE);
            if (remove) GJ.ToItem.playerRemoveItem(player, itemStack, 64);

            if (!GJ.ToItem.getNbt(itemStack).contains(GeoItem.ID_NBT_KEY, Tag.TAG_LONG)
                    && level instanceof ServerLevel serverLevel)
                GeoItem.getOrAssignId(itemStack, serverLevel);
        }
    }
}
