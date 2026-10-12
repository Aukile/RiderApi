package net.ankrya.rider_api.entity;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.entity.renderer.SpecialEffectEntityRenderer;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**为什么你跟不紧，为什么乘骑也奇奇怪怪的，恶啊，我要淘汰你*/
public class SpecialEffectEntity extends Entity implements GeoEntity {
    public static final EntityDataAccessor<Integer> DEAD_TIME = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> AUTO_CLEAR = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MODID = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MODEL = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public static final EntityDataAccessor<Boolean> GLOW = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.BOOLEAN);
    public Player owner;
    public static final String NAME = "special_effect";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public SpecialEffectEntity(EntityType<?> type, Level level) {
        this(type, level, null, null, null, null, 0);
    }

    public SpecialEffectEntity(EntityType<?> type, Level level, Player owner, String modid, String model, String texture, int dead) {
        this(type, level, owner, modid, model, texture, dead, false);
    }

    public SpecialEffectEntity(EntityType<?> type, Level level, Player owner, String modid, String model, String texture, int dead, boolean glow){
        super(type, level);
        if (dead != 0) this.entityData.set(DEAD_TIME, dead);
        if (modid != null)this.entityData.set(MODID, modid);
        if (model != null)this.entityData.set(MODEL, model);
        if (texture != null)this.entityData.set(TEXTURE, texture);
        this.owner = owner;
        if (owner != null){
            this.setOwnerUUID(owner.getUUID());
        }
        // 原来这里固定写 false，构造器传入的 glow 参数完全没用
        this.entityData.set(GLOW, glow);
    }

    public SpecialEffectEntity(Level level, Player owner, String modid, String model, String texture, int dead){
        this(ApiRegister.get().getRegisterObject(NAME, EntityType.class).get(), level, owner, modid, model, texture, dead);
    }

    @Override
    protected void defineSynchedData() {
        SynchedEntityData builder = this.entityData;
        builder.define(DEAD_TIME, 20);
        builder.define(AUTO_CLEAR, true);
        builder.define(ANIMATION, "idle");
        builder.define(MODID, RiderApi.MODID);
        builder.define(TEXTURE, "null");
        builder.define(MODEL, "null");
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(GLOW, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
//        super.readAdditionalSaveData(tag);
        if (tag.contains("deadTime"))
            this.entityData.set(DEAD_TIME, tag.getInt("deadTime"));
        if (tag.contains("autoClear"))
            this.entityData.set(AUTO_CLEAR, tag.getBoolean("autoClear"));
        if (tag.contains("animation"))
            this.entityData.set(ANIMATION, tag.getString("animation"));
        if (tag.contains("modid"))
            this.entityData.set(MODID, tag.getString("modid"));
        if (tag.contains("texture"))
            this.entityData.set(TEXTURE, tag.getString("texture"));
        if (tag.contains("model"))
            this.entityData.set(MODEL, tag.getString("model"));
        if (tag.contains("owner_uuid")) {
            this.entityData.set(OWNER_UUID, Optional.of(tag.getUUID("owner_uuid")));
            this.owner = null;
        }
        if (tag.contains("glow"))
            this.entityData.set(GLOW, tag.getBoolean("glow"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("deadTime", this.entityData.get(DEAD_TIME));
        tag.putBoolean("autoClear", this.entityData.get(AUTO_CLEAR));
        tag.putString("animation", this.entityData.get(ANIMATION));
        tag.putString("modid", this.entityData.get(MODID));
        tag.putString("texture", this.entityData.get(TEXTURE));
        tag.putString("model", this.entityData.get(MODEL));
        if (this.entityData.get(OWNER_UUID).isPresent()) {
            tag.putUUID("owner_uuid", this.entityData.get(OWNER_UUID).get());
        }
        tag.putBoolean("glow", this.entityData.get(GLOW));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance <= 256D;
    }

    @Override
    public void rideTick() {
        this.setDeltaMovement(Vec3.ZERO);
        if (canUpdate())
            this.tick();
        if (this.isPassenger()) {
            this.showSet(this);
        }
    }

    public void showSet(Entity entity) {
        this.showSet(entity, Entity::setPos);
    }

    protected void showSet(Entity entity, Entity.MoveFunction function) {
        if(this.getOwner() != null) {
            LivingEntity e = this.getOwner();
            double d0 = e.getY()-0.136;
            function.accept(entity, e.getX(), d0, e.getZ());
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        followTick();
        if (this.isRemoved()) return;
        if(getDeadTime() != 999999){
            if(getDeadTime() > 0){
                setDeadTime(getDeadTime() - 1);
            }else{
                this.discard();
            }
        }
    }

    public void followTick(){
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            // 只在服务端判定"主人没了就删除"。
            // 原来客户端也会删：主人还没加载到客户端（刚进入视野、跨维度）时，特效在客户端被直接删掉，之后再也看不到
            if (AutoClear() && !this.level().isClientSide()) this.discard();
            return;
        }
        // 服务端和客户端都每 tick 自己跟随主人。客户端不再依赖服务端每 3 tick 一次的位置包，
        // 所以不会出现"被 tp 过去拖着走"的抽搐。渲染时还会再用主人的插值位置修正，见 getRenderPosition
        if (!this.isPassenger()) positionSet(Entity::setPos);
        this.setYRot(owner.yBodyRot);
    }

    public final void positionSet(MoveFunction function) {
        LivingEntity owner = getOwner();
        if (owner == null) return;
        Vec3 point = owner.position();
        function.accept(this, point.x, point.y, point.z);
    }

    /**
     * 有主人时客户端自己跟随，忽略服务端发来的位置/朝向同步，避免被拉回旧位置造成抽搐
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        if (this.level().isClientSide() && !this.isPassenger() && getOwner() != null) return;
        super.lerpTo(x, y, z, yRot, xRot, steps, teleport);
    }

    /**
     * 渲染时使用的位置。有主人（且没有乘骑）时直接使用主人本帧的插值位置，
     * 和玩家渲染器用的是同一个位置，所以完全贴合、不会抖动
     */
    public Vec3 getRenderPosition(float partialTick) {
        LivingEntity owner = getOwner();
        if (owner != null && !this.isPassenger())
            return new Vec3(Mth.lerp(partialTick, owner.xOld, owner.getX()),
                    Mth.lerp(partialTick, owner.yOld, owner.getY()),
                    Mth.lerp(partialTick, owner.zOld, owner.getZ()));
        return new Vec3(Mth.lerp(partialTick, this.xOld, this.getX()),
                Mth.lerp(partialTick, this.yOld, this.getY()),
                Mth.lerp(partialTick, this.zOld, this.getZ()));
    }

    /**
     * 渲染时使用的朝向（Y 轴）。有主人时使用主人身体朝向的插值，与玩家渲染器（LivingEntityRenderer）一致。<br>
     * 原来用的是 getYRot()（头部朝向），所以玩家原地转头时特效和身体方向对不上。<br>
     * 需要跟随头部朝向的特效可以在子类里重写，改成 owner.yHeadRotO / owner.yHeadRot
     */
    public float getRenderYaw(float partialTick) {
        LivingEntity owner = getOwner();
        if (owner != null)
            return Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        return Mth.rotLerp(partialTick, this.yRotO, this.getYRot());
    }

    private PlayState predicate(AnimationState<SpecialEffectEntity> state) {
        AnimationController<SpecialEffectEntity> controller = state.getController();
        controller.setAnimation(RawAnimation.begin().then(animationName(), Animation.LoopType.PLAY_ONCE));
        if(controller.getAnimationState() == AnimationController.State.STOPPED)
            state.resetCurrentAnimation();
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"idle",this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public int getDeadTime(){
        return this.entityData.get(DEAD_TIME);
    }

    public void setDeadTime(int num){
        this.entityData.set(DEAD_TIME,num);
    }

    public String model(){
        return this.entityData.get(MODEL);
    }

    public String animationName(){
        return this.entityData.get(ANIMATION);
    }

    public String modid(){
        return this.entityData.get(MODID);
    }

    public String texture(){
        return this.entityData.get(TEXTURE);
    }

    public void setAnimationName(String animation){
        this.entityData.set(ANIMATION,animation);
    }

    public void setTexture(String texture){
        this.entityData.set(TEXTURE,texture);
    }

    public void setAutoClear(boolean flag){
        this.entityData.set(AUTO_CLEAR,flag);
    }

    public void setOwnerUUID(UUID uuid){
        this.entityData.set(OWNER_UUID,Optional.of(uuid));
    }

    public boolean AutoClear(){
        return this.entityData.get(AUTO_CLEAR);
    }

    public int getDead() {
        return this.entityData.get(DEAD_TIME);
    }

    public LivingEntity getOwner() {
        // 缓存的 owner 失效（主人重生后是新的对象、被移除、换了维度）时重新按 UUID 查找。
        // 原来会一直用旧对象：主人死亡重生后，服务端的特效停在原地，客户端却跟着新玩家走
        if (this.owner != null && !this.owner.isRemoved() && this.owner.level() == this.level())
            return this.owner;
        UUID uuid = this.entityData.get(OWNER_UUID).orElse(null);
        if (uuid == null) return null;
        Player player = this.level().getPlayerByUUID(uuid);
        this.owner = player;
        return player;
    }

//    public static AttributeSupplier.Builder createAttributes() {
//        AttributeSupplier.Builder builder = Mob.createMobAttributes();
//        builder = builder.add(Attributes.MOVEMENT_SPEED, 0);
//        builder = builder.add(Attributes.MAX_HEALTH, 2);
//        builder = builder.add(Attributes.ARMOR, 0);
//        builder = builder.add(Attributes.ATTACK_DAMAGE, 0);
//        builder = builder.add(Attributes.FOLLOW_RANGE, 16);
//        builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
//        return builder;
//    }

    /**是否自动识别 “_glowmask” 后缀发光，启用后必须保证贴图存在*/
    public void setModid(String modid){
        this.entityData.set(MODID, modid);
    }

    public String getModel() {
        return this.entityData.get(MODEL);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    public boolean autoGlow(){
        return this.entityData.get(GLOW);
    }

    public void setAutoGlow(boolean glow){
        this.entityData.set(GLOW, glow);
    }

    @OnlyIn(Dist.CLIENT)
    public RenderType getRenderType(ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends SpecialEffectEntity> Map<String, RenderType> boneByRenderType(SpecialEffectEntityRenderer<T> tSpecialEffectEntityRenderer) {
        return Map.of();
    }
}
