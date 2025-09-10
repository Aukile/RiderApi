package net.ankrya.rider_api.help.json;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
public class DateGather {

    @SubscribeEvent
    public static void gatherJson(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ItemModels(output, helper));
        generator.addProvider(event.includeClient(), new BlockModels(output, helper));
        generator.addProvider(event.includeClient(), new BlockStates(output, helper));
    }

    public static class ItemModels extends ItemModelProvider {
        public ItemModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper){
            this(output, RiderApi.MODID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            Class<?> clazz = Item.class;
            if (ApiRegister.isRegistered(clazz)){
                ApiRegister.getRegisterObjects(clazz).forEach((name, registerObject) -> {
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

    public static class BlockModels extends BlockModelProvider {
        public BlockModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        public BlockModels(PackOutput output, ExistingFileHelper existingFileHelper){
            this(output, RiderApi.MODID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            Class<?> clazz = Block.class;
            if (ApiRegister.isRegistered(clazz)){
                ApiRegister.getRegisterObjects(clazz).forEach((name, registerObject) -> {
                    Object object = registerObject.get();
                    if (object instanceof Block block) {
                        String blockName = block.getName().getString();
                        cubeAll(blockName, GJ.Easy.getApiResource(blockName));
                    }
                });
            }
        }

        @Override
        public @NotNull String getName() {
            return "Rider Jade Block Models";
        }
    }

    public static class BlockStates extends BlockStateProvider {
        public BlockStates(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        public BlockStates(PackOutput output, ExistingFileHelper existingFileHelper){
            this(output, RiderApi.MODID, existingFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            Class<?> clazz = Block.class;
            if (ApiRegister.isRegistered(clazz)){
                ApiRegister.getRegisterObjects(clazz).forEach((name, registerObject) -> {
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
}
