package net.ankrya.rider_api.data;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.interfaces.inside_use.IVariable;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.SyncVariableMessage;
import com.google.common.primitives.Primitives;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class Variables implements INBTSerializable<ListTag> {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RiderApi.MODID);
    public static final Supplier<AttachmentType<Variables>> VARIABLES = ATTACHMENT_TYPES.register("variables", () -> AttachmentType.serializable(Variables::new).build());
    AtomicInteger idPool = new AtomicInteger();
    Map<String, Data<?>> variables = new HashMap<>();
    public static final Map<String, Data<?>> variablesDefault = new HashMap<>();
    private boolean dirty = false;

    public Variables(){
        ModVariable.init(this);
    }

    public <T> void add(T value, String name){
        add(value, name, VariableSerializer.auto(value));
    }

    public <T> void add(T value, String name, IVariable<T> data){
        if (variables.containsKey(name))
            throw new RuntimeException("Name already exists");
        else {
            boolean save = variables.get(name).isSave();
            variables.put(name, new Data<>(value, name, data, save));
        }
    }

    public Variables cloneIfSave(Variables  variables){
        variables.variables.forEach((name, data) -> {
            if (data.isSave()){
                this.variables.put(name, data);
            } else this.variables.put(name, Variables.variablesDefault.get(name));
        });
        return this;
    }

    public Variables synIfSave(Variables  variables){
        variables.variables.forEach((name, data) -> {
            if (data.isSave()){
               this.variables.put(name, data);
            }
        });
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Data<T> get(String name){
        return (Data<T>) variables.get(name);
    }

    public CompoundTag serializeCompoundTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag listTag = serializeNBT(provider);
        compoundTag.put("variables", listTag);
        return compoundTag;
    }

    public void deserializeCompoundTag(HolderLookup.@NotNull Provider provider, CompoundTag tags){
        ListTag listTag = tags.getList("variables", Tag.TAG_LIST);
        deserializeNBT(provider, listTag);
    }

    @Override
    public @UnknownNullability ListTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        ListTag listTag = new ListTag();
        variables.forEach((name, data) -> {
            CompoundTag tag = new CompoundTag();
            Tag value = data.write();
            tag.put("value", value);
            tag.putInt("type", data.getLoad().typeId());
            tag.putString("name", name);
            tag.putBoolean("save", data.isSave());
            listTag.add(tag);
        });
        return listTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, ListTag tags) {
        tags.forEach(tag -> {
            CompoundTag compoundTag = (CompoundTag) tag;
            String name = compoundTag.getString("name");
            int type = compoundTag.getInt("type");
            Tag value = compoundTag.get("value");
            boolean save = compoundTag.getBoolean("save");
            Data<?> data = Data.read(value, type, name, save);
            variables.put(name, data);
        });
        dirty = false;
    }

    /**
     * 注册同步的数据<br>
     * 能支持的数据类型看{@link VariableSerializer}
     * @param clazz 类型
     * @param name 名称
     * @param defaultValue 默认值
     * @param save 是否保存（玩家死亡是否保存/世界维度间是否同步）
     */
    public <T> void registerVariable(Class<T> clazz, String name, T defaultValue, boolean save){
        syncVariables(name, defaultValue, VariableSerializer.auto(clazz), save);
    }

    public <T> void registerVariable(String name, @NotNull T defaultValue, boolean save){
        syncVariables(name, defaultValue, VariableSerializer.auto(defaultValue), save);
    }

    /**自动化了还要什么手操，我给你收起来*/
    private <T> void syncVariables(String name, T defaultValue, IVariable<T> type, boolean save){
        Data<T> data = new Data<>(defaultValue, name, type, save);
        variables.put(name, data);
        variablesDefault.put(name, data);
    }

    /**设置玩家的数据*/
    public  <T> void setToVariable(String name, T value, Entity entity){
        if (variables.containsKey(name)){
            Data<T> data = this.get(name);
            data.value = value;
            dirty = true;
        } else throw new RuntimeException("Variable does not exist");
        syncVariables(entity);
    }

    public  <T> void setToVariable(String name, T value, Level level){
        if (variables.containsKey(name)){
            Data<T> data = this.get(name);
            data.value = value;
            dirty = true;
        } else throw new RuntimeException("Variable does not exist");
        syncVariables(level);
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariable(String name){
        if (!variables.containsKey(name))
            return (T) variablesDefault.get(name).getValue();
        return (T) variables.get(name).value;
    }

    /**同步实体的同步数据*/
    public void syncVariables(Entity entity){
        if (entity instanceof ServerPlayer player)
            MessageLoader.sendToPlayersInDimension(new SyncVariableMessage(entity.getId(), this), player);
        else MessageLoader.sendToEntityAndSelf(new SyncVariableMessage(entity.getId(), this), entity);
    }

    /**同步世界的同步数据*/
    public void syncVariables(Level level){
        if (level instanceof ServerLevel serverLevel)
            MessageLoader.sendToPlayersInDimension(new SyncVariableMessage(-1, this), serverLevel);
    }

    /**设置实体的同步数据*/
    public static <T> void setVariable(Entity entity, String name, T value){
        Variables data = entity.getData(Variables.VARIABLES);
        data.setToVariable(name, value, entity);
    }

    /**设置世界的同步数据*/
    public static <T> void setVariable(Level level, String name, T value){
        Variables data = level.getData(Variables.VARIABLES);
        data.setToVariable(name, value, level);
    }

    /**获取实体的同步数据*/
    public static <T> T getVariable(Entity entity, String name){
        Variables data = entity.getData(Variables.VARIABLES);
        return data.getVariable(name);
    }

    /**获取世界的同步数据*/
    public static <T> T getVariable(Level level, String name){
        Variables data = level.getData(Variables.VARIABLES);
        return data.getVariable(name);
    }

    public static class Data<T>{
        T value;
        final String name;
        final IVariable<T> load;
        final boolean save;

        public Data(T value, String name, IVariable<T> load, boolean save){
            this.value = value;
            this.name = name;
            this.load = load;
            this.save = save;
        }

        public void setValue(T value){
            if (load != VariableSerializer.auto(value))
                throw new RuntimeException("Type mismatch");
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public IVariable<T> getLoad() {
            return load;
        }

        public boolean isSave() {
            return save;
        }

        @SuppressWarnings("unchecked")
        public Class<? extends T> type(){
            return (Class<T>) Primitives.unwrap(value.getClass());
        }

        public Tag write(){
            return load.write(value);
        }

        public static <T> Data<T> read(Tag nbt, int typeId, String name, boolean save){
            IVariable<T> iData = VariableSerializer.auto(typeId);
            return new Data<>(iData.read(nbt), name, iData, save);
        }
    }
}
