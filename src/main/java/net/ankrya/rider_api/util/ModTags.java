package net.ankrya.rider_api.util;

import net.ankrya.rider_api.help.GJ;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Items {

        public TagKey<Item> creatTag(String name) {
            return TagKey.create(Registries.ITEM, GJ.Easy.getApiResource(name));
        }
    }
    public static class Blocks {

        public TagKey<Block> creatTag(String name) {
            return TagKey.create(Registries.BLOCK, GJ.Easy.getApiResource(name));
        }
    }
}
