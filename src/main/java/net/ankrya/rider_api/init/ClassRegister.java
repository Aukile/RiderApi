package net.ankrya.rider_api.init;

import com.mojang.serialization.MapCodec;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.init.assist.RegisterAssist;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *神人注册机，如是说，虽然导致优先级方面有问题，但是豪玩！<br>
 * 注册使用方法(最后加上)：{@link ClassRegister#initSource}<br>
 * 使用例：{@link ApiRegister}
 */
public abstract class ClassRegister {
    abstract String modid();
    public Map<Class<?>,  DeferredRegister<?>> registers = new HashMap<>();
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

    /**大概没什么用*/
    @SuppressWarnings("unchecked")
    public <T> DeferredHolder<T, T> registerAsHolder(Class<? extends T> type, final String name, final Supplier<? extends T> sup){
        DeferredRegister<T> r = (DeferredRegister<T>) getRegisterSource(type);
        DeferredHolder<T, T> object = r.register(name, sup);
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
    private void soundRegister(String... names){
        for (String name : names)
              register(SoundEvent.class, name, () -> SoundEvent.createVariableRangeEvent(GJ.Easy.getApiResource(name)));
    }

    public record Tectonic<T>(String name, T t) {}
}
