package net.ankrya.rider_api.interfaces;

import net.minecraft.world.item.ItemStack;

public interface ItemToArrow {
    default boolean startThrow(ItemStack stack){
        return true;
    }
}
