package net.ankrya.rider_api.entity;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SpecialArrow extends AbstractArrow implements GeoEntity {
    public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SpecialArrow.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SpecialArrow.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MODEL = SynchedEntityData.defineId(SpecialArrow.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> MODID = SynchedEntityData.defineId(SpecialArrow.class, EntityDataSerializers.STRING);
    public static final String NAME = "special_arrow";
    private ItemStack stack = ItemStack.EMPTY;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    public SpecialArrow(EntityType<? extends SpecialArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SpecialArrow(Level level, LivingEntity livingEntity, String modid, String model, String texture, String animation) {
        super(getInstance(), livingEntity, level);

        if (modid != null)
            this.entityData.set(MODID, modid);
        if (model != null)
            this.entityData.set(MODEL, model);
        if (texture != null)
            this.entityData.set(TEXTURE, texture);
        if (animation != null)
            this.entityData.set(ANIMATION, animation);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION, "idle");
        this.entityData.define(TEXTURE, "null");
        this.entityData.define(MODEL, "null");
        this.entityData.define(MODID, "null");
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("animation"))
            this.setAnimation(tag.getString("animation"));
        if (tag.contains("texture"))
            this.setTexture(tag.getString("texture"));
        if (tag.contains("model"))
            this.setModel(tag.getString("model"));
        if (tag.contains("modid"))
            this.setModid(tag.getString("modid"));
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return stack;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("animation", this.getAnimation());
        tag.putString("texture", this.getTexture());
        tag.putString("model", this.getModel());
        tag.putString("modid", this.getModid());
    }


    private PlayState predicate(AnimationState<SpecialArrow> state) {
        AnimationController<SpecialArrow> controller = state.getController();
        controller.setAnimation(RawAnimation.begin().then(getAnimation(), Animation.LoopType.PLAY_ONCE));
        if(controller.getAnimationState() == AnimationController.State.STOPPED)
            state.resetCurrentAnimation();
        return PlayState.CONTINUE;
    }

    public String getAnimation(){
        return this.entityData.get(ANIMATION);
    }

    public void setAnimation(String animation){
        this.entityData.set(ANIMATION, animation);
    }

    public String getTexture(){
        return this.entityData.get(TEXTURE);
    }

    public void setTexture(String texture){
        this.entityData.set(TEXTURE, texture);
    }

    public String getModel(){
        return this.entityData.get(MODEL);
    }

    public void setModel(String model){
        this.entityData.set(MODEL, model);
    }

    public String getModid(){
        return this.entityData.get(MODID);
    }

    public void setModid(String modid){
        this.entityData.set(MODID, modid);
    }

    @SuppressWarnings("unchecked")
    public static EntityType<SpecialArrow> getInstance() {
        return (EntityType<SpecialArrow>) ApiRegister.get().getRegisterObject(NAME, EntityType.class).get();
    }

    public static AbstractArrow shoot(AbstractArrow specialArrow, double vx, double vy, double vz, float power, double damage, int knockback, float scattering){
        Level world = specialArrow.level();
        specialArrow.shoot(vx, vy, vz, power, scattering);
        specialArrow.setSilent(true);
        specialArrow.setCritArrow(false);
        specialArrow.setBaseDamage(damage);
        specialArrow.setKnockback(knockback);
        world.addFreshEntity(specialArrow);
        return specialArrow;
    }

    public static AbstractArrow shoot(AbstractArrow specialArrow, LivingEntity entity, float power, double damage, int knockback, float scattering) {
        GJ.ToEntity.turnTo(specialArrow, entity);
        return shoot(specialArrow, entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power, damage, knockback, scattering);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"idle",this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean autoGlow() {
        return false;
    }
}
