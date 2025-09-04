package net.ankrya.rider_api.interfaces.geo;

import net.ankrya.rider_api.item.renderer.base.BaseGeoArmorRenderer;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**是的~就是不想新建一个渲染类*/
public interface IGeoArmor extends IGeoItem {


    /**隐藏块（盔甲）*/
    default Map<String, Boolean> visibilityBones(BaseGeoArmorRenderer<?> renderer) {return new HashMap<>();}

    /**隐藏块~二选一即可*/
    default void visibilityBones(BaseGeoArmorRenderer<?> renderer, EquipmentSlot currentSlot){}
    /**做披风物理用的*/
    default void transformations(BaseGeoArmorRenderer<?> renderer){}
    /**会让这个组里面的都发光*/
    default Set<String> lightBones(BaseGeoArmorRenderer<?> renderer){return new HashSet<>();}
}