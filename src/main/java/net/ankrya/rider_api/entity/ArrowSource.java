package net.ankrya.rider_api.entity;

import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class ArrowSource extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> stackED = SynchedEntityData.defineId(ArrowSource.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> back = SynchedEntityData.defineId(ArrowSource.class, EntityDataSerializers.BOOLEAN);
    public static final String NAME = "arrow_source";
    int knockback = 0;

    public ArrowSource(EntityType<? extends ArrowSource> type, Level world) {
        super(type, world);
    }

    protected ArrowSource(double x, double y, double z, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(getInstance(), x, y, z, level, pickupItemStack, firedFromWeapon);
        if (firedFromWeapon != null)
            setKnockback(firedFromWeapon.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK)));
    }

    protected ArrowSource(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(getInstance(), owner, level, pickupItemStack, firedFromWeapon);
        if (firedFromWeapon != null)
            setKnockback(firedFromWeapon.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK)));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(back, false);
        builder.define(stackED, ItemStack.EMPTY);
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

    public int getKnockback() {
        return knockback;
    }

    public void setKnockback(int knockback) {
        this.knockback = knockback;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return super.getAddEntityPacket(entity);
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
    protected @NotNull ItemStack getDefaultPickupItem() {
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
    protected void doKnockback(@NotNull LivingEntity livingEntity, @NotNull DamageSource damageSource) {
        if (knockback > 0.0) {
            double d1 = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(knockback * 0.6 * d1);
            if (vec3.lengthSqr() > 0.0) {
                livingEntity.push(vec3.x, 0.1, vec3.z);
            }
        } else {
            super.doKnockback(livingEntity, damageSource);
        }
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
        return (EntityType<ArrowSource>) ApiRegister.get().getRegisterObject(NAME, EntityType.class).get();
    }
}
