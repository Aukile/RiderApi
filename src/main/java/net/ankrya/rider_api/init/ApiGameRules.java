package net.ankrya.rider_api.init;

import net.minecraft.world.level.GameRules;

public class ApiGameRules {

    public static final GameRules.Key<GameRules.BooleanValue> INVISIBILITY =
            GameRules.register("riderArmorInvisibility", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    public static void init(){
    }
}
