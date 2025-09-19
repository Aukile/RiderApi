package net.ankrya.rider_api.help.recipe.packet;

import com.google.gson.JsonObject;
import net.ankrya.rider_api.help.recipe.json.RecipeJsonManager;
import net.ankrya.rider_api.help.recipe.json.RecipeJsonManager.RecipeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class CreateRecipePacket {
    public final RecipeData recipe;

    public CreateRecipePacket(RecipeData recipe) {
        this.recipe = recipe;
    }

    public static void toBuf(CreateRecipePacket message, FriendlyByteBuf buf){
        RecipeData data = message.recipe;
        buf.writeUtf(data.id());
        buf.writeResourceLocation(data.type());
        buf.writeInt(data.pattern().getWidth());
        buf.writeInt(data.ingredients().length);
        for (int i = 0; i < data.ingredients().length; i++) {
            data.ingredients()[i].toIngredient().toNetwork(buf);
        }
        buf.writeInt(data.result().length);
        for (int i = 0; i < data.result().length; i++) {
            data.result()[i].toIngredient().toNetwork(buf);
        }
        buf.writeInt(data.extra().size());
        for (int i = 0; i < data.extra().size(); i++) {
            data.extra().forEach((key, value) -> {
                buf.writeUtf(key);
                buf.writeUtf(value.getAsString());
            });
        }
    }

//    public static CreateRecipePacket fromBuf(FriendlyByteBuf buf){
//        String id = buf.readUtf();
//        ResourceLocation type = buf.readResourceLocation();
//        int width = buf.readInt();
//        int ingredientSize = buf.readInt();
//        Ingredient[] ingredients = new Ingredient[ingredientSize];
//        for (int i = 0; i < ingredientSize; i++) {
//            ingredients[i] = Ingredient.fromNetwork(buf);
//        }
//        int resultSize = buf.readInt();
//        Ingredient[] result = new Ingredient[resultSize];
//        for (int i = 0; i < resultSize; i++) {
//            result[i] = Ingredient.fromNetwork(buf);
//        }
//        int extraSize = buf.readInt();
//        java.util.Map<String, JsonObject> extra = new java.util.HashMap<>();
//        for (int i = 0; i < extraSize; i++) {
//            String key = buf.readUtf();
//            String value = buf.readUtf();
//            JsonObject jsonObject = new JsonObject();
//            try {
//                if (value.matches("-?\\f+\\.\\f+"))
//                    jsonObject.addProperty(key, Float.parseFloat(value));
//                else jsonObject.addProperty(key, Integer.parseInt(value));
//            } catch (Exception e) {
//                jsonObject.addProperty(key, value);
//            }
//            extra.put(buf.readUtf(), jsonObject);
//        }
//        RecipeJsonManager.RecipeItem[] ingredientsValue = RecipeData.fromIngredients(ingredients);
//        RecipeJsonManager.RecipeItem[] resultValue = RecipeData.fromIngredients(ingredients);
//        return new CreateRecipePacket(new RecipeData(id, type, RecipeJsonManager.createPattern(ingredientsValue, width), ingredientsValue, resultValue, extra));
//    }
}
