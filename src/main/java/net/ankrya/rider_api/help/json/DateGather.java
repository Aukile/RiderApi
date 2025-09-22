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
                        basicItem(ResourceLocation.fromNamespaceAndPath(item.modid(), item.path() + item.name()));
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
                        cubeAll(item.name(), ResourceLocation.fromNamespaceAndPath(item.modid(), item.path() + item.name()));
                    } else if (object instanceof Block block) {
                        String blockName = block.getName().getString();
                        cubeAll(blockName, ResourceLocation.fromNamespaceAndPath(modid, blockName));
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
}
