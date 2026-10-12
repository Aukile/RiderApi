package net.ankrya.rider_api.help.advancement;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.stream.Collectors;

public final class SuggestionSources {

    private SuggestionSources() {}

    /** 所有配方 ID，客户端可用（配方会同步给客户端） */
    public static List<String> recipes() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();
        return mc.level.getRecipeManager().getRecipes().stream()
                .map(h -> h.getId().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 战利品表 ID：客户端默认拿不到全量。
     * 单机开发时可从集成服务器读取；多人环境需登录同步包。
     */
    public static List<String> lootTables() {
        var mc = Minecraft.getInstance();
        if (mc.getSingleplayerServer() != null) {
            return mc.getSingleplayerServer()
                    .getLootData()
                    .getKeys(net.minecraft.world.level.storage.loot.LootDataType.TABLE)
                    .stream()
                    .map(net.minecraft.resources.ResourceLocation::toString)
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}