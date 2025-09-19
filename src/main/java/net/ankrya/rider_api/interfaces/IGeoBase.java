package net.ankrya.rider_api.interfaces;

import net.minecraft.resources.ResourceLocation;

/**
 * 协助完成基础物品-模型路径
 */
public interface IGeoBase {

    String modid();
    String path();
    String name();

    default ResourceLocation modLocation(String path){
        return ResourceLocation.fromNamespaceAndPath(modid(), path);
    }

    default ResourceLocation modItemTextures(){
        return modLocation("textures/" + path() + name() + ".png");
    }

    default ResourceLocation modGeo(){
        return modLocation("geo/" + path() + name() + ".geo.json");
    }

    default ResourceLocation modAnimations(){
        return modLocation("animations/" + path() + name() + ".animation.json");
    }
}
