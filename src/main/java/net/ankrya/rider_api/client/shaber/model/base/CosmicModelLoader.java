package net.ankrya.rider_api.client.shaber.model.base;

import net.ankrya.rider_api.client.shaber.model.CosmicBakeModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CosmicModelLoader implements IGeometryLoader<CosmicModelLoader.CosmicGeometry> {
    public static final CosmicModelLoader INSTANCE = new CosmicModelLoader();

    @Override
    public @NotNull CosmicGeometry read(JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject cosmicObj = modelContents.getAsJsonObject("cosmic");
        if (cosmicObj == null) {
            throw new IllegalStateException("Missing 'cosmic' object.");
        } else {
            List<String> maskTexture = new ArrayList<>();
            if (cosmicObj.has("mask") && cosmicObj.get("mask").isJsonArray()) {
                JsonArray masks = cosmicObj.getAsJsonArray("mask");
                for (int i = 0; i < masks.size(); i++)
                    maskTexture.add(masks.get(i).getAsString());
            } else maskTexture.add(GsonHelper.getAsString(cosmicObj, "mask"));
            JsonObject clean = modelContents.deepCopy();
            clean.remove("cosmic");
            clean.remove("loader");
            BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
            return new CosmicModelLoader.CosmicGeometry(baseModel, maskTexture);
        }
    }

    public static class CosmicGeometry implements IUnbakedGeometry<CosmicGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;

        public CosmicGeometry(final BlockModel baseModel, final List<String> maskTextures) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides itemOverrides) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(ResourceLocation.parse(mask)));
            return new CosmicBakeModel(baseBakedModel, textures);
        }

        @Override
        public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> modelGetter, @NotNull IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
