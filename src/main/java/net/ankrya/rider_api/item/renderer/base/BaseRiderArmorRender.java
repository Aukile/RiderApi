package net.ankrya.rider_api.item.renderer.base;

import net.ankrya.rider_api.item.base.armor.BaseRiderArmorBase;
import net.minecraft.world.entity.EquipmentSlot;

public class BaseRiderArmorRender<T extends BaseRiderArmorBase> extends BaseGeoArmorRenderer<T>{
    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        switch (currentSlot) {
            case HEAD:
                this.setBoneVisible(this.head, true);
                break;
            case CHEST, LEGS:
                this.setBoneVisible(this.body, true);
                this.setBoneVisible(this.rightArm, true);
                this.setBoneVisible(this.leftArm, true);
                break;
            case FEET:
                this.setBoneVisible(this.rightLeg, true);
                this.setBoneVisible(this.leftLeg, true);
                this.setBoneVisible(this.rightBoot, true);
                this.setBoneVisible(this.leftBoot, true);
        }
    }
}
