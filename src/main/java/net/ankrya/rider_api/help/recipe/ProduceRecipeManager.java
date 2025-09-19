package net.ankrya.rider_api.help.recipe;

import net.ankrya.rider_api.help.recipe.json.RecipeJsonManager;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏内配方制作<br>
 * 还没完成(XxX)
 */
public class ProduceRecipeManager {
    private final Map<ResourceLocation, Recipe<?>> pendingRecipes = new HashMap<>();

    public ProduceRecipeManager addShapedRecipe(RecipeJsonManager.RecipeData recipeData) {
        ResourceLocation id = recipeData.getId();
        int width = recipeData.pattern().getWidth();
        int height = recipeData.pattern().pattern().size();
        NonNullList<Ingredient> inputs = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int i = 0; i < recipeData.ingredients().length; i++) {
            RecipeJsonManager.RecipeItem ingredient = recipeData.ingredients()[i];
            if (ingredient.type() == RecipeJsonManager.RecipeItem.Type.ITEM)
                inputs.set(i, ingredient.toIngredient());
        }
        ItemStack result = recipeData.result()[0].asItemStack(true);

        ShapedRecipe recipe = new ShapedRecipe(
                id,
                "",
                CraftingBookCategory.MISC,
                width,
                height,
                inputs,
                result.copy(),
                true
        );

        pendingRecipes.put(id, recipe);
        return this;
    }

    public ProduceRecipeManager addShapedLessRecipe(RecipeJsonManager.RecipeData recipeData) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(recipeData.type().getNamespace(), recipeData.id());
        RecipeJsonManager.RecipeItem[] ingredients = recipeData.getRecipeIngredientsWithOutput();
        NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.length, Ingredient.EMPTY);
        for (int i = 0; i < ingredients.length; i++)
            inputs.set(i, ingredients[i].toIngredient());
        ItemStack result = recipeData.result()[0].asItemStack(true);

        ShapelessRecipe recipe = new ShapelessRecipe(
                id,
                "",
                CraftingBookCategory.MISC,
                result.copy(),
                inputs
        );

        pendingRecipes.put(id, recipe);
        return this;
    }

    private void addIngredient(NonNullList<Ingredient> ingredients, Object object){
        if (object instanceof ItemLike item) {
            ingredients.add(Ingredient.of(item));
        } else if (object instanceof Ingredient ing) {
            ingredients.add(ing);
        } else if (object instanceof ItemStack stack) {
            ingredients.add(Ingredient.of(stack));
        }
    }
}
