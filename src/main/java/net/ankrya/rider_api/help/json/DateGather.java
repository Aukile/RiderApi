package net.ankrya.rider_api.help.json;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.init.ApiRegister;
import net.ankrya.rider_api.init.ClassRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class DateGather {
    protected abstract String modid();
    protected abstract ClassRegister register();

    public void gatherJson(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ItemModels(output, helper));
        generator.addProvider(event.includeClient(), new BlockModels(output, helper));
        generator.addProvider(event.includeClient(), new BlockStates(output, helper));
    }

    public class ItemModels extends ItemModelProvider {
        public ItemModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper){
            this(output, modid(), existingFileHelper);
        }

        @Override
        protected void registerModels() {
            Class<?> clazz = Item.class;
            if (ApiRegister.get().isRegistered(clazz)){
                ApiRegister.get().getRegisterObjects(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof Item item) {
                        basicItem(item);
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Rider Jade Item Models";
        }
    }

    public class BlockModels extends BlockModelProvider {
        public BlockModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        public BlockModels(PackOutput output, ExistingFileHelper existingFileHelper){
            this(output, modid(), existingFileHelper);
        }

        @Override
        protected void registerModels() {
            Class<?> clazz = Block.class;
            if (ApiRegister.get().isRegistered(clazz)){
                ApiRegister.get().getRegisterObjects(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof Block block) {
                        String blockName = block.getName().getString();
                        cubeAll(blockName, ResourceLocation.fromNamespaceAndPath(modid(), blockName));
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Rider Jade Block Models";
        }
    }

    public class BlockStates extends BlockStateProvider {
        public BlockStates(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        public BlockStates(PackOutput output, ExistingFileHelper existingFileHelper){
            this(output, modid(), existingFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            Class<?> clazz = Block.class;
            if (ApiRegister.get().isRegistered(clazz)){
                ApiRegister.get().getRegisterObjects(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof Block block) {
                        cubeAll(block);
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Rider Jade Block States";
        }
    }

    public static class LootTable extends LootTableProvider{
        public LootTable(PackOutput output, Set<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> requiredTables, List<SubProviderEntry> subProviders, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, requiredTables, subProviders, registries);
        }

        public LootTable(PackOutput output, Set<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> requiredTables, CompletableFuture<HolderLookup.Provider> registries) {
            this(output, requiredTables, List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK), new SubProviderEntry(EntityLoot::new, LootContextParamSets.ENTITY)), registries);
        }

        private static class BlockLoot extends BlockLootSubProvider {
            protected BlockLoot(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures, HolderLookup.Provider registries) {
                super(explosionResistant, enabledFeatures, registries);
            }

            public BlockLoot(HolderLookup.Provider provider) {
                this(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), provider);
            }

            @Override
            protected void generate() {

            }
        }

        private static class EntityLoot extends EntityLootSubProvider {
            protected EntityLoot(FeatureFlagSet required, HolderLookup.Provider registries) {
                super(required, registries);
            }

            public EntityLoot(HolderLookup.Provider provider) {
                this(FeatureFlags.REGISTRY.allFlags(), provider);
            }

            @Override
            public void generate() {

            }
        }
    }
}
