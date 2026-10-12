package net.ankrya.rider_api.help.advancement;

import java.util.List;

/** 常用触发器，选择器里列出，也可手填其它 */
public final class AdvancementTriggers {
    public static final List<String> COMMON = List.of(
            "minecraft:impossible",
            "minecraft:inventory_changed",
            "minecraft:player_killed_entity",
            "minecraft:entity_killed_player",
            "minecraft:consume_item",
            "minecraft:placed_block",
            "minecraft:recipe_unlocked",
            "minecraft:location",
            "minecraft:tick",
            "minecraft:enter_block",
            "minecraft:used_ender_eye",
            "minecraft:villager_trade",
            "minecraft:changed_dimension",
            "minecraft:levitation",
            "minecraft:effects_changed",
            "minecraft:item_used_on_block",
            "minecraft:brewed_potion",
            "minecraft:cured_zombie_villager",
            "minecraft:enchanted_item",
            "minecraft:fishing_rod_hooked",
            "minecraft:bred_animals",
            "minecraft:tame_animal"
    );
    private AdvancementTriggers() {}
}