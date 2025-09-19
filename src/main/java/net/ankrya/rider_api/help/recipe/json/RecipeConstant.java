package net.ankrya.rider_api.help.recipe.json;

public class RecipeConstant {

    public static final String MINECRAFT_NAMESPACE = "minecraft";
    public static final String TYPE = "type";
    public static final String PATTERN = "pattern";
    public static final String KEY = "key";
    public static final String INGREDIENTS = "ingredients";
    public static final String RESULT = "result";
    public static final String RESULTS = "results";
    public static final String OUTPUT = "output";
    public static final String COUNT = "count";
    public static final String ITEM = "item";
    public static final String NBT = "nbt";
    public static final String TAG = "tag";
    public static final String FLUID_TAG = "fluidTag";

    public static final String AIR = MINECRAFT_NAMESPACE + ":air";
    public static final RecipeJsonManager.RecipeItem AIR_ITEM = new RecipeJsonManager.RecipeItem(RecipeJsonManager.RecipeItem.Type.ITEM, AIR, 1, null);
}
