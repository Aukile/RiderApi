package net.ankrya.rider_api.help.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.IGeoBase;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * 难说，感觉也就{@link JsonCreator#createSoundsFile}还行<br>
 * 用的话自己弄个main方法吧
 */
public abstract class JsonCreator {
    protected abstract String id();

    /**
     * 自动创建 sounds文件<br>
     * 使用方法：把你所有的音频文件的文件名<br>
     * 作为public final String变量放到一个类里
     * @param clazz 包含全部音频文件名的类
     */
    public void createSoundsFile(Class<?> clazz, Path path){
        JsonObject root = createSounds(GJ.Easy.getAllString(clazz));
        try {
            createStart("sounds", path, root);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public enum GatherType {
        ITEM,
        BLOCK,
        BLOCKSTATE,
        SOUND,
        PARTICLE,
        TROPHIES,
        LANG;

        public Path getPath(JsonCreator creator){
            return switch (this){
                case ITEM -> Path.of("src/main/resources/assets/" + creator.id() + "/models/item");
                case BLOCK -> Path.of("src/main/resources/assets/" + creator.id() + "/models/block");
                case BLOCKSTATE -> Path.of("src/main/resources/assets/" + creator.id() + "/blockstate");
                case SOUND -> Path.of("src/main/resources/assets/" + creator.id());
                case PARTICLE -> Path.of("src/main/resources/assets/" + creator.id() + "/particles");
                case TROPHIES -> Path.of("src/main/resources/data/" + creator.id() + "/trophies");
                case LANG -> Path.of("src/main/resources/assets/" + creator.id() + "/lang");
            };
        }

        public String resourcesPathString(JsonCreator creator){
            return switch (this){
                case ITEM -> ("/assets/" + creator.id() + "/models/item");
                case BLOCK -> ("/assets/" + creator.id() + "/models/block");
                case BLOCKSTATE -> ("/assets/" + creator.id() + "/blockstate");
                case SOUND -> ("/assets/" + creator.id());
                case PARTICLE -> ("/assets/" + creator.id() + "/particles");
                case TROPHIES -> ("/data/" + creator.id() + "/trophies");
                case LANG -> ("/assets/" + creator.id() + "/lang");
            };
        }

    }

    /**
     * IGeoBase体系使用，必须实现{@link IGeoBase}
     */

    public <T extends IGeoBase> void createStartForIGeoBase(T geo, String output) {
        if (output == null){
            if (geo instanceof Item) {
                if (geo instanceof BaseRiderArmor armor) {
                    String armorSlot = armor.slot.getName();
                    //JsonObject root = createGeoItemJson("geo/" + geo.path() + geo.name() + ".item", geo.path() + geo.name());
                    JsonObject root = createItemJson(geo.path() + geo.name() + '_' + armorSlot);
                    createStart(geo.name() + '_' + armorSlot, GatherType.ITEM, root);

                    createLangFile(GatherType.LANG.getPath(this), "item." + id() + '.' + geo.name() + '_' + armorSlot, geo.name() + '_' + armorSlot);
                } else {
                    JsonObject root = createGeoItemJson("geo/" + geo.path() + geo.name() + ".item", geo.path() + geo.name());
                    createStart(geo.name(), GatherType.ITEM, root);

                    createLangFile(GatherType.LANG.getPath(this), "item." + id() + '.' + geo.name(), geo.name());
                }
            } else if (geo instanceof Block) {
                JsonObject root = createBlockJson(geo.path() + geo.name(), "cutout");
                JsonObject root2 = createBlockStateJson(geo.path() + geo.name());
                createStart(geo.name(), GatherType.BLOCK, root);
                createStart(geo.name(), GatherType.BLOCKSTATE, root2);

                createLangFile(GatherType.LANG.getPath(this), "block." + id() + '.' + geo.name(), geo.name());
            }
        } else {
            if (geo instanceof Item) {
                if (geo instanceof BaseRiderArmor armor) {
                    String armorSlot = armor.slot.getName();
                    //JsonObject root = createGeoItemJson("geo/" + geo.path() + geo.name() + ".item", geo.path() + geo.name());
                    JsonObject root = createItemJson(geo.path() + geo.name() + '_' + armorSlot);
                    createStart(geo.name() + '_' + armorSlot, Path.of(output + GatherType.ITEM.resourcesPathString(this)) , root);

                    createLangFile(Path.of(output + GatherType.LANG.resourcesPathString(this)), "item." + id() + '.' + geo.name() + '_' + armorSlot, geo.name() + '_' + armorSlot);
                } else {
                    JsonObject root = createGeoItemJson("geo/" + geo.path() + geo.name() + ".item", geo.path() + geo.name());
                    createStart(geo.name(), Path.of(output + GatherType.ITEM.resourcesPathString(this)), root);

                    createLangFile(Path.of(output + GatherType.LANG.resourcesPathString(this)), "item." + id() + '.' + geo.name(), geo.name());
                }
            } else if (geo instanceof Block) {
                JsonObject root = createBlockJson(geo.path() + geo.name(), "cutout");
                JsonObject root2 = createBlockStateJson(geo.path() + geo.name());
                createStart(geo.name(), Path.of(output  + GatherType.BLOCK.resourcesPathString(this)), root);
                createStart(geo.name(), Path.of(output  + GatherType.BLOCKSTATE.resourcesPathString(this)), root2);

                createLangFile(Path.of(output + GatherType.LANG.resourcesPathString(this)), "block." + id() + '.' + geo.name(), geo.name());
            }
        }
    }

    public <T extends IGeoBase> void createStartForIGeoBase(T geo){
        createStartForIGeoBase(geo, null);
    }

    /**
     * 创建json文件
     * @param name 文件名
     * @param type 文件类型
     */
    public void createStart(String name, GatherType type, JsonObject root){
        createStart(name, type.getPath(this), root);
    }

    /**
     * 创建 json文件
     * @param name 文件路径
     * @param path 文件路径
     * @param root json对象
     */
    public void createStart(String name, Path path, JsonObject root) {
        try {
            path = path.resolve(name + ".json");
            Files.createDirectories(path.getParent());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.write(path, gson.toJson(root).getBytes());
        } catch (IOException e) {
            RiderApi.LOGGER.error("fail to create json: {} ", name, e);
        }
    }

    public JsonObject createItemJson(String texture){
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", "item/generated");
        textures.addProperty("layer0", id() + ":item/" + texture);
        root.add("textures", textures);

        return root;
    }

    public JsonObject createGeoItemJson(String display, String texture){
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", id() + ':' + display);
        if (!texture.isEmpty()){
            textures.addProperty("layer0", id() + ":item/" + texture);
            root.add("textures", textures);
        }

        return root;
    }

    public JsonObject createBlockStateJson(String texture){
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject all = new JsonObject();
        all.addProperty("model", id() + ":block/" + texture);
        variants.add("all", all);
        root.add("variants", variants);
        return root;
    }

    public JsonObject createBlockJson(String texture, String renderType){
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();

        root.addProperty("parent", id() + "block/cube_all");
        textures.addProperty("all", id() + ":blocks/" + texture);
        textures.addProperty("particle", id() + ":blocks/" + texture);
        root.add("textures", textures);

        root.addProperty("render_type", renderType);
        return root;
    }

    public JsonObject createSounds(boolean subtitle, String... sounds) {
        JsonObject root = new JsonObject();

        for (String soundName : sounds) {

            JsonObject soundEntry = new JsonObject();
            JsonArray soundsArray = new JsonArray();
            JsonObject soundObject = new JsonObject();

            soundObject.addProperty("name", id() + ":" + soundName);

            soundObject.addProperty("stream", false);
            soundsArray.add(soundObject);

            soundEntry.addProperty("category", "player");
            if (subtitle) soundEntry.addProperty("subtitle", "subtitles." + soundName);
            soundEntry.add("sounds", soundsArray);
            root.add(soundName, soundEntry);
        }
        return root;
    }

    public JsonObject createSounds(String... sounds){
        return createSounds(true, sounds);
    }

    public JsonObject createParticle(String... textures) {
        JsonObject root = new JsonObject();
        JsonArray texture = new JsonArray();
        for(String t : textures)
            texture.add(id() + ":" + t);
        root.add("textures", texture);
        return root;
    }

    public enum TrophyType {
        BLOCK,
        ENTITY;

        public String get(TrophyType type){
            return switch (type) {
                case BLOCK -> "block";
                case ENTITY -> "entity";
            };
        }

        public String getSequence(TrophyType type){
            return switch (type) {
                case BLOCK -> "blocks";
                case ENTITY -> "entities";
            };
        }
    }

    public void createLangFile(Path path, String name, String lang) {
        createStart("en_us", path, createLang(path, name, lang));
    }

    public JsonObject createLang(Path path, String name, String lang) {
        JsonObject root;
        try {
            String content = Files.readString(path.resolve("en_us.json"));
            Gson gson = new Gson();
            root = gson.fromJson(content, JsonObject.class);
        } catch (IOException e) {
            root = new JsonObject();
        }

        if (root.has(name)) return root;
        else {
            lang = toTitleCase(lang);
            root.addProperty(name, lang);
            return root;
        }
    }

    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (c == '_') {
                result.append(' ');
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    public JsonObject createTrophy(String name, TrophyType type, TrophyPools[] trophyPools) {
        JsonObject root = new JsonObject();
        JsonArray pools = new JsonArray();

        root.addProperty("type", "minecraft:" + type.get(type));

        for (TrophyPools trophyPool : trophyPools) {
            JsonObject pool = new JsonObject();

            // 处理rolls
            int[] rollsValue = trophyPool.getRolls();
            if (rollsValue.length == 1) {
                pool.addProperty("rolls", rollsValue[0]);
            } else {
                JsonObject rollsRange = new JsonObject();
                rollsRange.addProperty("min", rollsValue[0]);
                rollsRange.addProperty("max", rollsValue[1]);
                pool.add("rolls", rollsRange);
            }

            // 处理bonus_rolls
            int[] bonusRollsValue = trophyPool.getBonusRolls();
            if (bonusRollsValue.length > 0) {
                if (bonusRollsValue.length == 1) {
                    pool.addProperty("bonus_rolls", bonusRollsValue[0]);
                } else {
                    JsonObject bonusRange = new JsonObject();
                    bonusRange.addProperty("min", bonusRollsValue[0]);
                    bonusRange.addProperty("max", bonusRollsValue[1]);
                    pool.add("bonus_rolls", bonusRange);
                }
            }

            // 处理entries
            JsonArray entriesArray = new JsonArray();
            for (TrophyPools.Entry entry : trophyPool.entries()) {
                JsonObject entryJson = new JsonObject();
                entryJson.addProperty("type", "minecraft:item");
                entryJson.addProperty("name", entry.name);
                entryJson.addProperty("weight", entry.weight);

                // 处理条件（conditions）
                JsonArray conditionsArray = new JsonArray();
                if (entry.match()) {
                    JsonObject condition = new JsonObject();
                    condition.addProperty("condition", "minecraft:match_tool");

                    JsonObject predicate = new JsonObject();
                    JsonObject enchantments = new JsonObject();
                    JsonArray enchantArray = new JsonArray();

                    JsonObject enchantObj = new JsonObject();
                    enchantObj.addProperty("enchantment", "minecraft:silk_touch");

                    JsonObject levels = new JsonObject();
                    levels.addProperty("min", 1);
                    enchantObj.add("levels", levels);

                    enchantArray.add(enchantObj);
                    enchantments.add("enchantments", enchantArray);
                    predicate.add("predicates", enchantments);

                    condition.add("predicate", predicate);
                    conditionsArray.add(condition);
                }

                if (!conditionsArray.isEmpty()) {
                    entryJson.add("conditions", conditionsArray);
                }

                // 处理函数（functions）
                JsonArray functionsArray = new JsonArray();

                // set_count 函数
                if (entry.countMin != entry.countMax) {
                    JsonObject setCount = new JsonObject();
                    setCount.addProperty("function", "minecraft:set_count");

                    JsonObject countObj = new JsonObject();
                    countObj.addProperty("min", entry.countMin);
                    countObj.addProperty("max", entry.countMax);
                    setCount.add("count", countObj);

                    functionsArray.add(setCount);
                }

                // explosion_decay 函数
                if (entry.explosion_decay()) {
                    JsonObject explosionDecay = new JsonObject();
                    explosionDecay.addProperty("function", "minecraft:explosion_decay");
                    functionsArray.add(explosionDecay);
                }

                // 时运效果
                if (entry.fortune) {
                    JsonObject applyBonus = new JsonObject();
                    applyBonus.addProperty("function", "minecraft:apply_bonus");
                    applyBonus.addProperty("enchantment", "minecraft:fortune");
                    applyBonus.addProperty("formula", "minecraft:ore_drops");
                    functionsArray.add(applyBonus);
                }

                // enchant_with_levels 函数
                if (entry.hasEnchantLevel()) {
                    JsonObject enchantWith = new JsonObject();
                    enchantWith.addProperty("function", "minecraft:enchant_with_levels");

                    JsonObject levelsObj = new JsonObject();
                    levelsObj.addProperty("min", entry.enchantLevelMin);
                    levelsObj.addProperty("max", entry.enchantLevelMax);
                    enchantWith.add("levels", levelsObj);

                    enchantWith.addProperty("treasure", true);
                    functionsArray.add(enchantWith);
                }

                if (!functionsArray.isEmpty()) {
                    entryJson.add("functions", functionsArray);
                }

                entriesArray.add(entryJson);
            }

            pool.add("entries", entriesArray);
            pools.add(pool);
        }

        root.add("pools", pools);
        root.addProperty("random_sequence", id() + ":" + type.get(type) + "/" + name);
        return root;
    }


    /**
     *
     * @param rollsMin 基础抽取最小
     * @param rollsMax 基础抽取最大
     * @param bonusMin 额外抽取最小
     * @param bonusMax 额外抽取最大
     * @param entries 条目
     */
    public record TrophyPools(int rollsMin, int rollsMax, int bonusMin, int bonusMax, Entry[] entries){

        public int[] getRolls() {
            return new int[]{rollsMin, rollsMax};
        }

        public int[] getBonusRolls() {
            return bonusMin == bonusMax ? new int[]{bonusMax} : new int[]{bonusMin, bonusMax};
        }

        /**
         *
         * @param name 物品注册名
         * @param weight 权重
         * @param countMin 最小掉落数量
         * @param countMax 最大掉落数量
         * @param enchantLevelMin 附魔等级影响最小值
         * @param enchantLevelMax 附魔等级影响最大值
         * @param fortune 时运等级影响
         * @param explosion_decay 爆炸衰减
         * @param match 匹配工具模式
         */
        public record Entry(String name, int weight, int countMin, int countMax, int enchantLevelMin, int enchantLevelMax, boolean fortune, boolean explosion_decay, boolean match) {
            public boolean hasEnchantLevel() {
                return enchantLevelMin > 0 || enchantLevelMax > 0;
            }
        }
    }
}
