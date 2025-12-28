package net.ankrya.rider_api.interfaces;

import net.ankrya.rider_api.interfaces.geo.IGeoItem;
import net.minecraft.resources.ResourceLocation;

/**
 * 协助完成基础物品-模型路径规范化<p>
 * 可以在自己模组中更简化一点：
 * <pre>{@code
 * public interface GeoBase extends IGeoBase {
 *     @Override
 *     default String modid(){
 *         return Mod.MODID;
 *     }
 *
 *     @Override
 *     default ResourceLocation modItemTextures() {
 *         return modLocation("textures/item/geo/" + path() + name() + ".png");
 *     }
 * }}</pre>
 * 然后再：<p>
 * 使用{@link IGeoBase#modGeo()}输出GEO模型的模型路径<p>
 * 使用{@link IGeoBase#modAnimations()} ()}输出GEO模型的动画路径<p>
 * 使用{@link IGeoBase#modItemTextures()} ()} ()}输出GEO模型的贴图路径<p>
 * 制作基础物品且规范化文件路径（同时别放错文件位置）<p>
 * 使用示例：
 * <pre>{@code
 * public abstract class BaseModItem extends BaseGeoItem implements GeoBase {
 *     public static final String PATH = "category";
 *     public BaseModItem(Properties properties) {
 *         super(properties);
 *     }
 *
 *     @Override
 *     public String path() {
 *         return PATH;
 *     }
 *
 *     @Override
 *     public ResourceLocation getModel() {
 *         return modGeo();
 *     }
 *
 *     @Override
 *     public ResourceLocation getAnimationFile() {
 *         return modAnimations();
 *     }
 *
 *     @Override
 *     public ResourceLocation getTexture() {
 *         return modItemTextures();
 *     }
 * }
 * }</pre>
 * ps:利用此产生的类可以使用{@link net.ankrya.rider_api.help.json.JsonCreator#createStartForIGeoBase}结合{@link net.ankrya.rider_api.init.ClassRegister}来自动创建物品需要的JSON文件}<p>
 * 示例：
 * <pre>{@code
 * @EventBusSubscriber(modid = Mod.MODID)
 * public class JsonCreatHelper{
 *     @SubscribeEvent
 *     public static void toGatherJson(GatherDataEvent event) {
 *         JsonCreator jsonCreate = JsonCreator.simply(Mod.MODID);
 *         String outputFolder = event.getGenerator().getPackOutput().getOutputFolder().toString();
 *         if (outputFolder.contains("generated"))
 *             outputFolder = outputFolder.replace("generated", "main");
 *
 *         //生成注册物品/方块的json和lang
 *         for (Supplier<?> supplier : ModRegister.register().getRegisterObjects(Item.class).values()) {
 *             if (supplier.get() instanceof GeoBase geo) {
 *                 jsonCreate.createStartForIGeoBase(geo, outputFolder);
 *             }
 *         }
 *
 *         //生成音频文件的json
 *         jsonCreate.createSoundsFile(SoundNames.class, Path.of(outputFolder + JsonCreator.GatherType.SOUND.resourcesPathString(jsonCreate)));
 *         //生成音频文件的lang
 *         jsonCreate.createSoundsLang(SoundNames.class, Path.of(outputFolder + JsonCreator.GatherType.SOUND.resourcesPathString(jsonCreate)));
 *     }
 * }
 * }</pre>
 */
public interface IGeoBase extends IGeoItem {

    String modid();
    String path();
    String name();

    default ResourceLocation modLocation(String path){
        return ResourceLocation.fromNamespaceAndPath(modid(), path);
    }

    default ResourceLocation modItemTextures(){
        return modLocation("textures/" + path() + name() + ".png");
    }

    default ResourceLocation modGeo(){
        return modLocation("geo/" + path() + name() + ".geo.json");
    }

    default ResourceLocation modAnimations(){
        return modLocation("animations/" + path() + name() + ".animation.json");
    }

    @Override
    default ResourceLocation getTexture() {
        return modItemTextures();
    }

    @Override
    default ResourceLocation getAnimationFile(){
        return modAnimations();
    }

    @Override
    default ResourceLocation getModel() {
        return modGeo();
    }
}
