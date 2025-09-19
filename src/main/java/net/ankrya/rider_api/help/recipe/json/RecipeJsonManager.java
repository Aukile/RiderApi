package net.ankrya.rider_api.help.recipe.json;

import com.google.gson.*;
import net.ankrya.rider_api.RiderApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.ankrya.rider_api.help.recipe.json.RecipeConstant.*;

public class RecipeJsonManager {
    private static final Logger LOGGER = RiderApi.LOGGER;
    private static final String DIR = "config/rider_api/recipes";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record RecipeItem(Type type, String result, int count, String nbt) {

        public enum Type {
            ITEM(RecipeConstant.ITEM),
            TAG(RecipeConstant.TAG),
            FLUID_TAG(RecipeConstant.FLUID_TAG);
            final String type;

            Type(String type) {
                this.type = type;
            }

            public String getType() {
                return type;
            }
        }

        public static RecipeItem of(ItemStack stack) {
            return new RecipeItem(Type.ITEM, stack.getItem().toString(), stack.getCount(), stack.getOrCreateTag().toString());
        }

        public ItemStack asItemStack(boolean disallowsAirInRecipe){
            return CraftingHelper.getItemStack(RecipeJsonManager.recipeItemToJsonObject(this), true, disallowsAirInRecipe);
        }

        public static String nbtStringFromNbts(String[] nbts) {
            StringBuilder nbtString = new StringBuilder();
            for (int i = 0; i < nbts.length; i++) {
                nbtString.append(nbts[i]);
                if (i != nbts.length - 1) {
                    nbtString.append(",");
                }
            }
            return nbtString.toString();
        }

        public static Ingredient.Value toIngredientValue(RecipeItem recipeItem) {
            String input = recipeItem.result();
            if (input.contains("item")) {
                ItemStack stack = recipeItem.asItemStack(true);
                return new Ingredient.ItemValue(stack);
            } else if (input.contains("tag")) {

                int count = 0;
                int start = -1;
                int end = -1;

                for (int i = 0; i < input.length(); i++) {
                    if (input.charAt(i) == '"') {
                        count++;
                        if (count == 3) {
                            start = i + 1;
                        } else if (count == 4) {
                            end = i;
                            break;
                        }
                    }
                }

                if (start != -1 && end != -1) {
                    input = input.substring(start, end);
                } else {
                    return new Ingredient.ItemValue(recipeItem.asItemStack(true));
                }

                ResourceLocation resourcelocation = ResourceLocation.parse(input);
                TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
                return new Ingredient.TagValue(tagkey);
            } else {
                throw new JsonParseException("An ingredient entry needs either a tag or an item");
            }
        }

        public Ingredient toIngredient() {
            return Ingredient.fromValues(Arrays.stream(new Ingredient.Value[]{toIngredientValue(this)}));
        }
    }

    public record Pattern(List<String> pattern, Map<RecipeItem, String> mapping){

        public int getWidth(){
            if (pattern().isEmpty()) return 0;
            String patternFristString = pattern().get(0);
            return patternFristString.length();
        }

        public static Pattern empty() {
            return new Pattern(List.of(), Map.of());
        }

        public boolean isEmpty() {
            if (!pattern.isEmpty() && mapping.isEmpty())
                LOGGER.warn("Pattern is not empty, but mapping is empty ?");
            return pattern.isEmpty();
        }
    }

    public record RecipeData(String id, ResourceLocation type, Pattern pattern, RecipeItem[] ingredients,
                             RecipeItem[] result, Map<String, JsonObject> extra) {

        public ResourceLocation getId(){
            return ResourceLocation.fromNamespaceAndPath(type().getNamespace(), id());
        }

        public RecipeItem[] getRecipeIngredientsWithOutput() {
            return Arrays.stream(ingredients()).filter(item -> !item.result().equals(AIR)).toArray(RecipeItem[]::new);
        }
    }

//    public static void test(){
//        JsonObject extra = new JsonObject();
//        extra.addProperty("experience", 0.5);
//        RecipeItem item = new RecipeItem(RecipeItem.Type.ITEM, "minecraft:dirt", 1, new String[]{creatStringNbt("Name", "Dirt")});
//        RecipeItem item2 = new RecipeItem(RecipeItem.Type.ITEM, "minecraft:glass", 1, new String[0]);
//        RecipeItem result = new RecipeItem(RecipeItem.Type.ITEM, "minecraft:wood", 1, new String[]{creatStringNbt("Name", "Wood")});
//        RecipeItem result2 = new RecipeItem(RecipeItem.Type.ITEM, "minecraft:dirt", 1, new String[]{creatStringNbt("Name", "Wood")});
//        RecipeItem[] ingredients = new RecipeItem[]{
//                item, AIR_ITEM , item2,
//                AIR_ITEM, item2, AIR_ITEM,
//                AIR_ITEM, item, item,
//        };
//        RecipeData recipe = new RecipeData("test", ResourceLocation.fromNamespaceAndPath("minecraft", "crafting_shapeless"),
//                Pattern.empty(), ingredients, new RecipeItem[]{result,result2}, Map.of( "extra", extra)
//        );
//        saveRecipe(recipe);
//    }

    public static String creatStringNbt(String name, String value) {
        return '{' + name + ':' + '"' + value + '"' + '}';
    }

    public static Pattern createPattern(RecipeItem[] ingredients, int width){
        Map<RecipeItem, String> map = generateLetterMapping(ingredients);
        List<String> pattern = new ArrayList<>();
        if (ingredients.length % width != 0)
            LOGGER.warn("The number of ingredients is not a multiple of the width.");
        int high = ingredients.length / width;
        StringBuilder patternLine = new StringBuilder();
        for (int h = 0; h < high; h++) {
            for (int i = 0; i < width; i++) {
                int j = i + width * h;
                RecipeItem item = ingredients[j];
                patternLine.append(getPatternNumberFromItem(item, map));
                if (i == width - 1) {
                    if (j != ingredients.length - 1){
                        patternLine.append(",");
                    }
                    pattern.add(patternLine.toString());
                    patternLine = new StringBuilder();
                }
            }
        }
        return new Pattern(pattern, map);
    }

    private static String getPatternNumberFromItem(RecipeItem item, Map<RecipeItem, String> map) {
        if (item.result().equals(AIR))
            return " ";
        else if (map.containsKey(item))
            return map.get(item);
        else {
            LOGGER.warn("Pattern is error to create, error item: {}", item);
            return " ";
        }
    }

    private static Map<RecipeItem, String> generateLetterMapping(RecipeItem[] ingredients) {
        Set<RecipeItem> itemSet = Arrays.stream(ingredients).collect(Collectors.toSet());
        itemSet.removeIf(item -> item.result().equals(AIR));
        List<String> letters = IntStream.range(0, Math.min(itemSet.size(), 26))
                .mapToObj(i -> String.valueOf((char) ('A' + i)))
                .toList();

        Map<RecipeItem, String> mapping = new HashMap<>();
        int index = 0;
        for (RecipeItem item : itemSet) {
            mapping.put(item, letters.get(index));
            index++;
        }
        return mapping;
    }

    public static void saveRecipe(RecipeData recipe) {
        String parent_add = recipe.type().getNamespace().equals(MINECRAFT_NAMESPACE) ? "" : "/" + recipe.type().getNamespace();
        File file = new File(DIR + parent_add, sanitizeFileName(recipe.id) + ".json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TYPE, recipe.type().toString());
        if (recipe.pattern().isEmpty()) {
            RecipeItem[] ingredients = recipe.getRecipeIngredientsWithOutput();
            JsonArray patternArray = recipeItemsToJsonArray(ingredients);
            jsonObject.add(INGREDIENTS, patternArray);
        } else {
            JsonArray patternArray = new JsonArray();
            for (String pattern : recipe.pattern().pattern())
                patternArray.add(pattern);
            jsonObject.add(PATTERN, patternArray);
            jsonObject.add(KEY, recipeItemsToKey(recipe.pattern().mapping()));
        }
        if (recipe.result().length == 1)
            jsonObject.add(RESULT, recipeItemToJsonObject(recipe.result[0]));
        else jsonObject.add(RESULTS, recipeItemsToJsonArray(recipe.result));
        if (!recipe.extra().isEmpty()) {
            recipe.extra().forEach(jsonObject::add);
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(jsonObject, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save recipe: {}", e.getMessage());
        }
    }

    private static JsonObject recipeItemsToKey(Map<RecipeItem, String> map){
        JsonObject key = new JsonObject();
        for (Map.Entry<RecipeItem, String> entry : map.entrySet()) {
            JsonObject itemJson = recipeItemToJsonObject(entry.getKey());
            key.add(entry.getValue(), itemJson);
        }
        return key;
    }

    private static JsonArray recipeItemsToJsonArray(RecipeItem[] items) {
        JsonArray array = new JsonArray();
        for (RecipeItem item : items) {
            array.add(recipeItemToJsonObject(item));
        }
        return array;
    }

    public static JsonObject recipeItemToJsonObject(RecipeItem item) {
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty(ITEM, item.result());
        if (item.count() > 1)
            itemJson.addProperty(COUNT, item.count);
        if (!item.nbt().isEmpty())
            itemJson.addProperty(NBT, item.nbt());
        return itemJson;
    }

    public static boolean deleteRecipe(ResourceLocation recipeId){
        try {
            Path path = FMLPaths.GAMEDIR.get().resolve(DIR);
            if (!recipeId.getNamespace().equals(MINECRAFT_NAMESPACE))
                path = path.resolve(recipeId.getNamespace());
            return Files.deleteIfExists(path.resolve(sanitizeFileName(recipeId.getPath()) + ".json"));
        } catch (Exception e) {
            LOGGER.error("Failed to delete recipe: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 清理文件名中的非法字符
     */
    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[<>:\"/\\\\|?*]", "_");
    }

    /**
     * 检查配方文件是否存在
     */
    public static boolean fileExists(String recipeId) {
        File file = new File(DIR, sanitizeFileName(recipeId) + ".json");
        return file.exists();
    }

    /**
     * 获取配方目录路径
     */
    public static String getDirectory() {
        return DIR;
    }
}
