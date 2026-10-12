package net.ankrya.rider_api.init;

import com.mojang.serialization.MapCodec;
import net.ankrya.rider_api.init.assist.RegisterAssist;
import net.ankrya.rider_api.interfaces.IGeoBase;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
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
    /** 已经注册到事件总线的注册源，防止 easyRegister + initSource 重复注册导致崩溃 */
    private final Set<DeferredRegister<?>> boundRegisters = new HashSet<>();

    public <T> DeferredRegister<T> registerSource(Class<T> type, String registerName) {
        return registerSource(type, registerName, modid());
    }

    public <T> void registerSource(Class<T> type, ResourceKey<Registry<T>> registerTo) {
        registerSource(type, registerTo, modid());
    }

    private <T> DeferredRegister<T> registerSource(Class<T> type, String registerName, String modid) {
        ResourceKey<Registry<T>> key = ResourceKey.createRegistryKey(new ResourceLocation(registerName));
        return registerSource(type, key, modid());
    }

    @SuppressWarnings("unchecked")
    public <T> DeferredRegister<T> registerSource(Class<T> type, ResourceKey<Registry<T>> registerTo, String modid) {
        // 原来同一个类型重复调用时会返回一个新的、没有被保存的 DeferredRegister，往里面注册的东西永远不会生效
        DeferredRegister<?> existing = registers.get(type);
        if (existing != null) return (DeferredRegister<T>) existing;

        DeferredRegister<T> register = DeferredRegister.create(registerTo, modid());
        if (RegisterAssist.registerSourceSafe(type, registers)){
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

    private DeferredRegister<?> requireRegisterSource(Class<?> type){
        DeferredRegister<?> register = registers.get(type);
        if (register == null)
            throw new IllegalStateException("[" + modid() + "] 还没有注册 " + type.getSimpleName() + " 的注册源，请先调用 registerSource(" + type.getSimpleName() + ".class)");
        return register;
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
        DeferredRegister<T> r = (DeferredRegister<T>) requireRegisterSource(type);
        Supplier<T> object = r.register(name, sup);
        updateRegisters(type, name, object);
        return object;
    }

    /**
     * 为方块注册对应的方块物品（BlockItem）<br>
     * @param sup 用 register(Block.class, ...) 返回的对象
     */
    @SuppressWarnings("unchecked")
    public <T extends Item> Supplier<T> registerBlockItem(final String name, Supplier<?> sup){
        requireRegisterSource(Item.class);
        return (Supplier<T>) register(Item.class, name, () -> new BlockItem((Block) sup.get(), new Item.Properties()));
    }

    /** 和 register 一样，但返回 RegistryObject */
    @SuppressWarnings("unchecked")
    public <T> RegistryObject<T> registerAsHolder(Class<? extends T> type, final String name, final Supplier<? extends T> sup){
        DeferredRegister<T> r = (DeferredRegister<T>) requireRegisterSource(type);
        RegistryObject<T> object = r.register(name, sup);
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
        // 原来这里注册一次、initSource 又注册一次 → "Cannot register DeferredRegister to more than one event bus" 崩溃
        if (boundRegisters.add(source)) source.register(bus);
    }

    /**
     * {@link  ClassRegister#registerSource} 注册注册源<br>
     * {@link  ClassRegister#register} 注册对象<br>
     */
    public void initSource(IEventBus bus){
        for (DeferredRegister<?> register : registers.values()){
            if (boundRegisters.add(register)) register.register(bus);
        }
    }

    /**
     * 获取注册对象
     * IGeoBase 体系使用，必须实现{@link IGeoBase}
     * @see IGeoBase
     */
    /**
     * 原来返回 void，查到的结果直接丢掉了，方法等于什么也没做。
     * 为了不改变已有方法的签名（其他模组可能已经编译调用），保留它，请改用 {@link #findIGeoBaseObject}
     */
    @Deprecated
    public <T extends IGeoBase> void getIGoBaseObject(T target, Class<?> as) {
        findIGeoBaseObject(target, as);
    }

    /** 获取 IGeoBase 对象的注册 Supplier，找不到返回 null */
    public <T extends IGeoBase> Supplier<?> findIGeoBaseObject(T target, Class<?> as) {
        Map<String, Supplier<?>> map = registerObjects.get(as);
        return map == null ? null : map.get(target.name());
    }

    /**
     * 获取物品类的注册对象，使用反射获取实例，仅支持构造器中仅有Properties类的情况
     * IGeoBase 体系使用，必须实现{@link IGeoBase}
     * @see IGeoBase
     */
    /** 同上，保留原签名，请改用 {@link #findIGeoBaseEasyItem} */
    @Deprecated
    public <T extends IGeoBase> void getIGoBaseEasyItem(Class<T> target, Class<?> as){
        findIGeoBaseEasyItem(target, as);
    }

    public <T extends IGeoBase> Supplier<?> findIGeoBaseEasyItem(Class<T> target, Class<?> as){
        try {
            String name = target.getDeclaredConstructor(Item.Properties.class).newInstance(new Item.Properties()).name();
            Map<String, Supplier<?>> map = registerObjects.get(as);
            return map == null ? null : map.get(name);
        } catch (Throwable e) {
            throw new RuntimeException("getIGoBaseEasyItem only supports classes that have properties using the constructor.");
        }
    }

    public void soundRegister(String... names){
        for (String name : names)
              register(SoundEvent.class, name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(modid(), name)));
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
        // 没注册过这个类型时返回空表，而不是 null（原来 getRegisterObject 会直接空指针）
        Map<String, Supplier<?>> map = registerObjects.get(clazz);
        return map == null ? Collections.emptyMap() : map;
    }

    /**获取注册的东西*/
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> getRegisterObject(String name, Class<T> clazz){
        return (Supplier<T>) getRegisterObjects(clazz).get(name);
    }

    // ---------------- 按名字获取注册好的实例 ----------------
    // 注意：必须在注册完成之后调用（例如不能在静态字段初始化、registerSource 之前调用），否则 Forge 会报 "Trying to access unbound value"
    // name 可以写 "xxx" 也可以写 "本模组id:xxx"

    /** 按名字获取已注册的实例，找不到时抛出带提示的异常 */
    public Object getRegistered(Class<?> type, String name) {
        Supplier<?> supplier = findRegisteredSupplier(type, name);
        if (supplier == null) {
            Set<String> names = new java.util.TreeSet<>();
            registerObjects.forEach((clazz, map) -> { if (type.isAssignableFrom(clazz)) names.addAll(map.keySet()); });
            throw new IllegalArgumentException("[" + modid() + "] 没有注册名为 \"" + name + "\" 的 " + type.getSimpleName() + "，已注册的有：" + names);
        }
        return supplier.get();
    }

    /** 按名字获取已注册的实例，找不到返回 null */
    public Object findRegistered(Class<?> type, String name) {
        Supplier<?> supplier = findRegisteredSupplier(type, name);
        return supplier == null ? null : supplier.get();
    }

    /** 是否注册过这个名字 */
    public boolean hasRegistered(Class<?> type, String name) {
        return findRegisteredSupplier(type, name) != null;
    }

    /** 按名字查找注册时得到的 Supplier（也是 RegistryObject），找不到返回 null；注册源用的是子类（如 SimpleParticleType）也能找到 */
    public Supplier<?> findRegisteredSupplier(Class<?> type, String name) {
        if (name == null) return null;
        int colon = name.indexOf(':');
        if (colon >= 0) {
            if (!name.substring(0, colon).equals(modid())) return null;
            name = name.substring(colon + 1);
        }
        Map<String, Supplier<?>> map = registerObjects.get(type);
        if (map != null && map.containsKey(name)) return map.get(name);
        for (Map.Entry<Class<?>, Map<String, Supplier<?>>> entry : registerObjects.entrySet()) {
            if (entry.getKey() != type && type.isAssignableFrom(entry.getKey()) && entry.getValue().containsKey(name))
                return entry.getValue().get(name);
        }
        return null;
    }

    /** 物品 */
    public Item getRegisteredItem(String name) {
        return (Item) getRegistered(Item.class, name);
    }

    /** 物品的 ItemStack（数量 1），做创造物品栏图标之类的时候方便 */
    public ItemStack getRegisteredItemStack(String name) {
        return new ItemStack(getRegisteredItem(name));
    }

    /** 方块 */
    public Block getRegisteredBlock(String name) {
        return (Block) getRegistered(Block.class, name);
    }

    /** 方块实体类型 */
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityType<T> getRegisteredBlockEntity(String name) {
        return (BlockEntityType<T>) getRegistered(BlockEntityType.class, name);
    }

    /** 实体类型 */
    @SuppressWarnings("unchecked")
    public <T extends Entity> EntityType<T> getRegisteredEntity(String name) {
        return (EntityType<T>) getRegistered(EntityType.class, name);
    }

    /** 粒子类型 */
    @SuppressWarnings("unchecked")
    public <T extends ParticleOptions> ParticleType<T> getRegisteredParticle(String name) {
        return (ParticleType<T>) getRegistered(ParticleType.class, name);
    }

    /** 简单粒子（SimpleParticleType 本身就是 ParticleOptions，可以直接 level.addParticle） */
    public SimpleParticleType getRegisteredSimpleParticle(String name) {
        Object particle = getRegistered(ParticleType.class, name);
        if (particle instanceof SimpleParticleType simple) return simple;
        throw new IllegalArgumentException("[" + modid() + "] 粒子 \"" + name + "\" 不是 SimpleParticleType，请用 getRegisteredParticle");
    }

    /** 音效 */
    public SoundEvent getRegisteredSound(String name) {
        return (SoundEvent) getRegistered(SoundEvent.class, name);
    }

    /** 音效的 Holder（有些原版方法需要 Holder<SoundEvent>） */
    @SuppressWarnings("unchecked")
    public Holder<SoundEvent> getRegisteredSoundHolder(String name) {
        Supplier<?> supplier = findRegisteredSupplier(SoundEvent.class, name);
        if (supplier instanceof RegistryObject<?> object && object.getHolder().isPresent())
            return (Holder<SoundEvent>) object.getHolder().get();
        return Holder.direct(getRegisteredSound(name));
    }

    /** 创造模式物品栏 */
    public CreativeModeTab getRegisteredTab(String name) {
        return (CreativeModeTab) getRegistered(CreativeModeTab.class, name);
    }

    public record Tectonic<T>(String name, T t) {}
}
