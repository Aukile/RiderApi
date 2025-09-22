package net.ankrya.rider_api.init;

import com.mojang.serialization.MapCodec;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.init.assist.RegisterAssist;
import net.ankrya.rider_api.interfaces.IGeoBase;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *神人注册机，如是说，虽然导致优先级方面有问题，但是豪玩！<br>
 * 注册使用方法(最后加上)：{@link ClassRegister#initSource}<br>
 * 使用例：{@link ApiRegister}
 */
public abstract class ClassRegister {
    protected abstract String modid();
    public Map<Class<?>, DeferredRegister<?>> registers = new HashMap<>();
    public Map<Class<?>, Map<String, Supplier<?>>> registerObjects = new HashMap<>();

    public <T> DeferredRegister<T> registerSource(Class<T> type, String registerName) {
        return registerSource(type, registerName, modid());
    }

    public <T> void registerSource(Class<T> type, ResourceKey<Registry<T>> registerTo) {
        registerSource(type, registerTo, modid());
    }

    private <T> DeferredRegister<T> registerSource(Class<T> type, String registerName, String modid) {
        ResourceKey<Registry<T>> key = ResourceKey.createRegistryKey(ResourceLocation.parse(registerName));
        return registerSource(type, key, modid());
    }

    public <T> DeferredRegister<T> registerSource(Class<T> type, ResourceKey<Registry<T>> registerTo, String modid) {
        DeferredRegister<T> register = DeferredRegister.create(registerTo, modid());
        if (!registers.containsKey(type) && RegisterAssist.registerSourceSafe(type, registers)){
            registers.put(type, register);
        }
        return register;
    }

    /**
     * <strong>特别注意</strong><br>
     * 有的有参型的是不能用的<br>
     * 例如: {@link MapCodec}<br>
     * （好像也就这一个这样，不过可以用{@link ClassRegister#registerSource(Class, ResourceKey)}注册就没事了）
     */
    public <T> DeferredRegister<T> registerSource(Class<T> type){
        return registerSource(type, RegisterAssist.getRegisterName(type));
    }

    private DeferredRegister<?> getRegisterSource(Class<?> type){
        return registers.get(type);
    }

    private <T> void updateRegisters(Class<? extends T> type, final String name, Supplier<?> object){
        if (registerObjects.containsKey(type)){
            Map<String, Supplier<?>> supplierMap = registerObjects.get(type);
            supplierMap.put(name, object);
        }else {
            Map<String, Supplier<?>> supplierMap = new HashMap<>();
            supplierMap.put(name, object);
            registerObjects.put(type, supplierMap);
        }
    }

    /**
     * 主要使用此方法注册<br>
     * 前面一定要先注册注册源<br>
     * 用{@link ClassRegister#registerSource}
     */
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(Class<? extends T> type, final String name, final Supplier<? extends T> sup){
        DeferredRegister<T> r = (DeferredRegister<T>) getRegisterSource(type);
        Supplier<T> object = r.register(name, sup);
        updateRegisters(type, name, object);
        return object;
    }

    /**注册一次，只注册一个东西的时候可以用*/
    public <T> DeferredRegister<? extends T> onceRegister(Class<? extends T> type, final String name, final Supplier<? extends T> sup){
        DeferredRegister<? extends T> source = registerSource(type);
        register(type, name, sup);
        return source;
    }

    /**简易注册（也没那么简），实验品*/
    public <T> void easyRegister(Class<T> clazz, IEventBus bus, Tectonic<T>[] tectonics){
        DeferredRegister<T> source = registerSource(clazz);
        for (Tectonic<T> tectonic : tectonics) {
            register(clazz, tectonic.name(), tectonic::t);
        }
        source.register(bus);
    }

    /**
     * {@link  ClassRegister#registerSource} 注册注册源<br>
     * {@link  ClassRegister#register} 注册对象<br>
     */
    public void initSource(IEventBus bus){
        for (DeferredRegister<?> register : registers.values()){
            register.register(bus);
        }
    }

    /**
     * 获取注册对象
     * IGeoBase 体系使用，必须实现{@link IGeoBase}
     * @see IGeoBase
     */
    public <T extends IGeoBase> void getIGoBaseObject(T target, Class<?> as) {{
            registerObjects.get(as).get(target.name());
        }
    }

    /**
     * 获取物品类的注册对象，使用反射获取实例，仅支持构造器中仅有Properties类的情况
     * IGeoBase 体系使用，必须实现{@link IGeoBase}
     * @see IGeoBase
     */
    public <T extends IGeoBase> void getIGoBaseEasyItem(Class<T> target, Class<?> as){
        try {
            String name = target.getDeclaredConstructor(Item.Properties.class).newInstance(new Item.Properties()).name();
            registerObjects.get(as).get(name);
        } catch (Throwable e) {
            throw new RuntimeException("getIGoBaseEasyItem only supports classes that have properties using the constructor.");
        }
    }

    private void soundRegister(String... names){
        for (String name : names)
              register(SoundEvent.class, name, () -> SoundEvent.createVariableRangeEvent(GJ.Easy.getApiResource(name)));
    }

    /**是否已注册类型*/
    public boolean isRegistered(Class<?> clazz) {
        return registerObjects.containsKey(clazz);
    }

    /**应该用不到*/
    public Map<Class<?>, DeferredRegister<?>> getRegisters() {
        return registers;
    }

    /**获取模组中此类型注册的全部东西*/
    public Map<String, Supplier<?>> getRegisterObjects(Class<?> clazz) {
        return registerObjects.get(clazz);
    }

    /**获取注册的东西*/
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> getRegisterObject(String name, Class<T> clazz){
        return (Supplier<T>) getRegisterObjects(clazz).get(name);
    }

    public record Tectonic<T>(String name, T t) {}
}
