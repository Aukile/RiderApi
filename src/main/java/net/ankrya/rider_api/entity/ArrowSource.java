package net.ankrya.rider_api.entity;

import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class ArrowSource extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> stackED = SynchedEntityData.defineId(ArrowSource.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> back = SynchedEntityData.defineId(ArrowSource.class, EntityDataSerializers.BOOLEAN);
    public static final String NAME = "arrow_source";

    public ArrowSource(PlayMessages.SpawnEntity packet, Level world) {
        super(getInstance(), world);
    }

    public ArrowSource(EntityType<? extends ArrowSource> type, Level world) {
        super(type, world);
    }

    public ArrowSource(EntityType<? extends ArrowSource> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world);
    }

    public ArrowSource(EntityType<? extends ArrowSource> type, LivingEntity entity, Level world) {
        super(type, entity, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(back, false);
        this.entityData.define(stackED, ItemStack.EMPTY);
    }

    public void setItemStack(ItemStack stack){
        this.entityData.set(stackED,stack);
    }

    public ItemStack getItemStack(){
        return this.entityData.get(stackED);
    }

    public void setIfBack(boolean ifBack){
        this.entityData.set(back,ifBack);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull ItemStack getItem() {
        return this.entityData.get(stackED);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        if (this.entityData.get(back))
            return this.entityData.get(stackED);
        return ItemStack.EMPTY;
    }

    @Override
    protected void doPostHurtEffects(@NotNull LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static Projectile shoot(ItemStack itemStack, boolean pick, Level level, Entity shooter, float damage, int knockback) {
        ArrowSource entityToSpawn = new ArrowSource(ArrowSource.getInstance(), level);
        entityToSpawn.setItemStack(itemStack);
        entityToSpawn.setIfBack(pick);
        entityToSpawn.setOwner(shooter);
        entityToSpawn.setBaseDamage(damage);
        entityToSpawn.setKnockback(knockback);
        entityToSpawn.setSilent(true);
        return entityToSpawn;
    }
    @Override
    public void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        ItemEntity itemEntity = new ItemEntity(entityHitResult.getEntity().level(), entityHitResult.getEntity().getX(),entityHitResult.getEntity().getY(),entityHitResult.getEntity().getZ(),this.getItemStack());
        itemEntity.setDeltaMovement((entityHitResult.getEntity().getLookAngle().x), (entityHitResult.getEntity().getLookAngle().y), (entityHitResult.getEntity().getLookAngle().z));
        entityHitResult.getEntity().level().addFreshEntity(itemEntity);
    }
    
    @SuppressWarnings("unchecked")
    public static EntityType<ArrowSource> getInstance(){
        return (EntityType<ArrowSource>)ApiRegister.get().getRegisterObject(NAME, EntityType.class).get();
    }
}
