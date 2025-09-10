package net.ankrya.rider_api.entity;

import net.ankrya.rider_api.RiderApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class SpecialEffectEntity extends Entity implements GeoEntity {
    public static final EntityDataAccessor<Integer> DEAD_TIME = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> AUTO_CLEAR = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MODEL = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MODID = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public Player owner;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public SpecialEffectEntity(EntityType<?> type, Level level) {
        this(type, level, null, null, null, 0);
    }

    public SpecialEffectEntity(EntityType<?> type, Level level, Player owner, String model, String texture, int dead) {
        super(type, level);
        if (dead != 0) this.entityData.set(DEAD_TIME, dead);
        if (model != null)this.entityData.set(MODEL, model);
        if (texture != null)this.entityData.set(TEXTURE, texture);
        this.owner = owner;
        if (owner != null){
            this.setOwnerUUID(owner.getUUID());
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DEAD_TIME, 20);
        this.entityData.define(AUTO_CLEAR, true);
        this.entityData.define(ANIMATION, "idle");
        this.entityData.define(TEXTURE, "null");
        this.entityData.define(MODEL, "null");
        this.entityData.define(MODID, RiderApi.MODID);
        this.entityData.define(OWNER_UUID, Optional.empty());
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
        if (tag.contains("texture"))
            this.entityData.set(TEXTURE, tag.getString("texture"));
        if (tag.contains("model"))
            this.entityData.set(MODEL, tag.getString("model"));
        if (tag.contains("modid"))
            this.entityData.set(MODID, tag.getString("modid"));
        if (tag.contains("owner_uuid")) {
            this.entityData.set(OWNER_UUID, Optional.of(tag.getUUID("owner_uuid")));
            this.owner = null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("deadTime", this.entityData.get(DEAD_TIME));
        tag.putBoolean("autoClear", this.entityData.get(AUTO_CLEAR));
        tag.putString("animation", this.entityData.get(ANIMATION));
        tag.putString("texture", this.entityData.get(TEXTURE));
        tag.putString("model", this.entityData.get(MODEL));
        tag.putString("modid", this.entityData.get(MODID));
        tag.putUUID("owner_uuid", this.entityData.get(OWNER_UUID).get());
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

    public void showSet(Entity entity)
    {
        this.showSet(entity, Entity::setPos);
    }
    protected void showSet(Entity entity, Entity.MoveFunction function) {
        if(this.getOwner() != null)
        {
            LivingEntity e = this.getOwner();
            double d0 = e.getY()-0.136;
            function.accept(entity, e.getX(), d0, e.getZ());
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        followTick();
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
        if (AutoClear() && (owner == null || !owner.isAlive())) this.discard();
        else positionSet(Entity::setPos);
    }

    public final void positionSet(MoveFunction function) {
        LivingEntity owner = getOwner();
        Vec3 point = owner.position();
        function.accept(this, point.x, point.y, point.z);
    }

    private PlayState predicate(AnimationState<SpecialEffectEntity> state) {
        String animation = this.animationName();
        if (animation.equals("null"))
            state.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        else {
            state.getController().setAnimation(RawAnimation.begin().then(animation, Animation.LoopType.LOOP));
        }
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

    public String texture(){
        return this.entityData.get(TEXTURE);
    }

    public String modid(){
        return this.entityData.get(MODID);
    }

    public void setModid(String modid){
        this.entityData.set(MODID,modid);
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

    public LivingEntity getOwner() {
        UUID uuid = this.entityData.get(OWNER_UUID).orElse(null);
        if (this.owner != null)
            return this.owner;
        else if (uuid != null)
            return this.level().getPlayerByUUID(uuid);
        else return null;
    }

//    public static AttributeSupplier.Builder createAttributes() {
//        AttributeSupplier.Builder builder = Mob.createMobAttributes();
//        builder = builder.add(Attributes.MOVEMENT_SPEED, 0);
//        builder = builder.add(Attributes.MAX_HEALTH, 2);
//        builder = builder.add(Attributes.ARMOR, 0);
//        builder = builder.add(Attributes.ATTACK_DAMAGE, 0);
//        builder = builder.add(Attributes.FOLLOW_RANGE, 16);
//        builder = builder.add(Attributes.JUMP_STRENGTH, 0.6);
//        return builder;
//    }

    public String getModel() {
        return this.entityData.get(MODEL);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    public int getDead() {
        return this.entityData.get(DEAD_TIME);
    }

    /**是否自动识别 “_glowmask” 后缀发光，启用后必须保证贴图存在*/
    public boolean autoGlow(){
        return false;
    }
}
