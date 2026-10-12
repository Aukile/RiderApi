package net.ankrya.rider_api.help.json;

import net.ankrya.rider_api.init.ClassRegister;
import net.ankrya.rider_api.interfaces.IGeoBase;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

public abstract class DateGather {
    protected abstract String modid();
    protected abstract ClassRegister register();

    public static DateGather simply(String modid, ClassRegister register) {
        return new DateGather() {
            @Override
            public String modid() {
                return modid;
            }

            @Override
            public ClassRegister register() {
                return register;
            }
        };
    }

    public void gatherJson(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ItemModels(output, modid(), helper));
        generator.addProvider(event.includeClient(), new BlockModels(output, modid(), helper));
        generator.addProvider(event.includeClient(), new BlockStates(output, modid(), helper));
    }

    public class ItemModels extends ItemModelProvider {
        public ItemModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            Class<?> clazz = Item.class;
            if (register().isRegistered(clazz)){
                register().registerObjects.get(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof IGeoBase item){
                        basicItem(new ResourceLocation(item.modid(), item.path() + item.name()));
                    } else if (object instanceof Item item) {
                        basicItem(item);
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Item Models";
        }
    }

    public class BlockModels extends BlockModelProvider {
        public BlockModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            Class<?> clazz = Block.class;
            if (register().isRegistered(clazz)){
                register().getRegisterObjects(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof IGeoBase item){
                        cubeAll(item.name(), new ResourceLocation(item.modid(), item.path() + item.name()));
                    } else if (object instanceof Block block) {
                        String blockName = block.getName().getString();
                        cubeAll(blockName, new ResourceLocation(modid, blockName));
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Block Models";
        }
    }

    public class BlockStates extends BlockStateProvider {
        public BlockStates(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            Class<?> clazz = Block.class;
            if (register().isRegistered(clazz)){
                register().getRegisterObjects(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof Block block) {
                        cubeAll(block);
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Block States";
        }
    }

    /**
     * 战利品表生成器的骨架（主分支新增）。默认没有任何战利品表，继承后重写 BlockLoot/EntityLoot 的 generate 添加。<br>
     * 注意：原版 BlockLootSubProvider/EntityLootSubProvider 会检查“所有已注册方块/实体”是否都有战利品表，
     * 这里把检查范围限定为空，避免空骨架直接让数据生成失败
     */
    public static class LootTable extends net.minecraft.data.loot.LootTableProvider {
        public LootTable(PackOutput output, java.util.Set<ResourceLocation> requiredTables, java.util.List<SubProviderEntry> subProviders) {
            super(output, requiredTables, subProviders);
        }

        public LootTable(PackOutput output, java.util.Set<ResourceLocation> requiredTables) {
            this(output, requiredTables, java.util.List.of(
                    new SubProviderEntry(BlockLoot::new, net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK),
                    new SubProviderEntry(EntityLoot::new, net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY)));
        }

        public static class BlockLoot extends net.minecraft.data.loot.BlockLootSubProvider {
            protected BlockLoot(java.util.Set<Item> explosionResistant, net.minecraft.world.flag.FeatureFlagSet enabledFeatures) {
                super(explosionResistant, enabledFeatures);
            }

            public BlockLoot() {
                this(java.util.Collections.emptySet(), net.minecraft.world.flag.FeatureFlags.REGISTRY.allFlags());
            }

            @Override
            protected void generate() {
            }

            @Override
            protected @NotNull Iterable<Block> getKnownBlocks() {
                return java.util.Collections.emptyList();
            }
        }

        public static class EntityLoot extends net.minecraft.data.loot.EntityLootSubProvider {
            protected EntityLoot(net.minecraft.world.flag.FeatureFlagSet required) {
                super(required);
            }

            public EntityLoot() {
                this(net.minecraft.world.flag.FeatureFlags.REGISTRY.allFlags());
            }

            @Override
            public void generate() {
            }

            @Override
            protected java.util.stream.@NotNull Stream<net.minecraft.world.entity.EntityType<?>> getKnownEntityTypes() {
                return java.util.stream.Stream.empty();
            }
        }
    }
}
