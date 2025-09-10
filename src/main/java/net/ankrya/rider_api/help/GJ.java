package net.ankrya.rider_api.help;

import com.mojang.datafixers.util.Pair;
import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.client.particle.base.AdvancedParticleBase;
import net.ankrya.rider_api.client.particle.base.advanced.AdvancedParticleData;
import net.ankrya.rider_api.client.particle.base.advanced.ParticleComponent;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonComponent;
import net.ankrya.rider_api.client.particle.base.advanced.RibbonParticleData;
import net.ankrya.rider_api.client.sound.DelayPlaySound;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.NMessageCreater;
import net.ankrya.rider_api.message.common.LoopSoundMessage;
import net.ankrya.rider_api.message.ex_message.PlayLoopSound;
import net.ankrya.rider_api.message.ex_message.StopLoopSound;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * <strong>超大号工具箱</strong><br>
 * 拖家带口说是<br>
 * 怕以后多了找不到我做了分类<br>
 * 不好分类的放外面了
 */
public abstract class GJ {

    /**杂项*/
    public static abstract class ToOther {
        public static void setPersonFront(CameraType cameraType) {
            if (Minecraft.getInstance().options.getCameraType() != cameraType) {
                Minecraft.getInstance().options.setCameraType(cameraType);
            }
        }
    }
    
    /**简化作业*/
    public static abstract class Easy {

        /**Api模组自用的*/
        @ApiStatus.Internal
        public static ResourceLocation getApiResource(String path){
            return ResourceLocation.fromNamespaceAndPath(RiderApi.MODID, path);
        }

        /**
         * @return 类中的全部的public static final String变量
         */
        public static String[] getAllString(Class<?> clazz){
            try {
                Field[] fields = clazz.getDeclaredFields();
                List<String> soundNames = new ArrayList<>();

                for (Field field : fields) {
                    if (field.getType() == String.class && Modifier.isStatic(field.getModifiers())) {
                        String string = (String) field.get(null);
                        if (string != null)
                            soundNames.add(string);
                    }
                }

                return soundNames.toArray(new String[0]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new String[0];
            }
        }
    }

    /**世界相关*/
    public static abstract class ToWorld {
        public static void playMSound(Level level, double x, double y, double z, String name) {
            SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(name));
            if (soundEvent != null) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), soundEvent, SoundSource.NEUTRAL, 1, 1);
                } else {
                    level.playLocalSound(x, y, z, soundEvent, SoundSource.NEUTRAL, 1, 1, false);
                }
            }
        }

        public static List<LivingEntity> rangeFind(Level level, Vec3 center, int radius) {
            return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center)
                            .inflate(radius / 2d), e -> true).stream()
                    .sorted(Comparator
                            .comparingDouble(livingEntity ->
                                    livingEntity.distanceToSqr(center)))
                    .toList();
        }
    }

    public static boolean isFront(Entity entity, Entity target, float width) {
        return (target.getX() - entity.getX()) * entity.getLookAngle().x >= 0 && (target.getZ() - entity.getZ()) * entity.getLookAngle().z >= width;
    }

    public static List<LivingEntity> rangeFind(Entity entity, double radius) {
        return ToWorld.rangeFind(entity.level(), entity.position(), (int) radius);
    }

    /**玩家相关*/
    public static abstract class ToPlayer {
        public static void addHitCooling(Player player, int value){
            Variables.setVariable(player, ModVariable.HIT_COOLING, value);
        }

        public static void playSound(Player player, String name) {
            playSound(player, name, false);
        }

        public static void playSound(Player player, String name, boolean loop) {
            if (player instanceof ServerPlayer serverPlayer) {
                MessageLoader.getLoader().sendToPlayersNearby(createLoopSoundMessage(player, name, loop), serverPlayer);
            }
        }

        private static LoopSoundMessage createLoopSoundMessage(Player player, String name, boolean loop) {
            return new LoopSoundMessage(GJ.Easy.getApiResource(name), loop, 16, PlayLoopSound.PLAYERS, player.getId());
        }

        public static void playDelaySound(Player player, String name, boolean loop, int delay) {
            if (player instanceof ServerPlayer serverPlayer) {
                DelayPlaySound.add(serverPlayer, createLoopSoundMessage(player, name, loop), delay);
            }
        }

        public static void cancelDelaySound(Player player, String name) {
            ResourceLocation soundId = GJ.Easy.getApiResource(name);
            DelayPlaySound.cancel(player, soundId);
            GJ.ToPlayer.stopSound(player, name);
        }

        public static void stopSound(Player player, String name) {
            if (player instanceof ServerPlayer serverPlayer) {
                MessageLoader.getLoader().sendToPlayersNearby(
                        new NMessageCreater(new StopLoopSound(player.getId(), PlayLoopSound.PLAYERS
                                , GJ.Easy.getApiResource(name))), serverPlayer);
            }
        }

        public static boolean hasItem(Entity entity, ItemStack itemstack) {
            if (entity instanceof Player player)
                return player.getInventory().contains(itemstack);
            return false;
        }

        public static GameType getEntityGameType(Entity entity) {
            if (entity instanceof ServerPlayer serverPlayer) {
                return serverPlayer.gameMode.getGameModeForPlayer();
            } else if (entity instanceof Player player && player.level().isClientSide()) {
                PlayerInfo playerInfo = Objects.requireNonNull(Minecraft.getInstance().getConnection()).getPlayerInfo(player.getGameProfile().getId());
                if (playerInfo != null)
                    return playerInfo.getGameMode();
            }
            return null;
        }

    }

    /**实体相关*/
    public static abstract class ToEntity {
        public static void fixHealth(LivingEntity entity) {
            if (entity.getMaxHealth() < entity.getHealth())
                entity.setHealth(entity.getMaxHealth());
        }

        public static void playExplosionSound(Entity entity) {
            ToWorld.playMSound(entity.level(), entity.getX(), entity.getY() + 1, entity.getZ(), "entity.generic.explode");
        }
    }


    /**物品相关*/
    public static abstract class ToItem {
        public static final String REMOVE = "dooms_remove";
        public static ItemStack getDriver(LivingEntity entity) {
            return entity.getItemBySlot(EquipmentSlot.LEGS);
        }

        /**1.20就没必要用了*/
        public static void setNbt(ItemStack itemStack, Consumer<CompoundTag> updater){
            updater.accept(itemStack.getOrCreateTag());
        }

        /**1.20就没必要用了*/
        public static CompoundTag getNbt(ItemStack itemStack){
            return itemStack.getOrCreateTag();
        }

        public static void equipBySlot(Entity entity, EquipmentSlot slot, ItemStack stack){
            if (entity instanceof Player player) {
                player.getInventory().armor.set(slot.getIndex(), stack);
                player.getInventory().setChanged();
                List<Pair<EquipmentSlot, ItemStack>> slots = List.of(Pair.of(slot, stack));
                if (player instanceof ServerPlayer serverPlayer)
                    serverPlayer.connection.send(new ClientboundSetEquipmentPacket(player.getId(), slots));
            } else if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setItemSlot(slot, stack);
            }
        }

        public static void playerRemoveItem(Player player, Item item, int count){
            playerRemoveItem(player, itemStack -> itemStack.is(item), count);
        }

        public static void playerRemoveItem(Player player, ItemStack stack, int count){
            if (!getNbt(stack).getBoolean(REMOVE))
                setNbt(stack, tag -> tag.putBoolean(REMOVE, true));
            playerRemoveItem(player, itemStack -> itemStack == stack, count);
        }

        public static void playerRemoveItem(Player player, Predicate<ItemStack> condition, int count){
            player.getInventory().clearOrCountMatchingItems(condition, count, player.getInventory());
        }
    }

    /**数学相关*/
    public static abstract class ToMath {
        public static void transform(Vector3f vector3f, Quaternionf quaternionf) {
            Quaternionf quaternion = new Quaternionf(quaternionf);
            quaternion.mul(new Quaternionf(vector3f.x(), vector3f.y(), vector3f.z(), 0.0F));
            Quaternionf quaternion1 = new Quaternionf(quaternionf);
            quaternion1.conjugate();
            quaternion.mul(quaternion1);
            vector3f.set(quaternion.x(), quaternion.y(), quaternion.z());
        }

        public static void transform(Vector4f vector4f, Matrix4f matrix4f) {
            float f = vector4f.x;
            float f1 = vector4f.y;
            float f2 = vector4f.z;
            float f3 = vector4f.w;
            vector4f.x = matrix4f.m00() * f + matrix4f.m01() * f1 + matrix4f.m02() * f2 + matrix4f.m03() * f3;
            vector4f.y = matrix4f.m10() * f + matrix4f.m11() * f1 + matrix4f.m12() * f2 + matrix4f.m13() * f3;
            vector4f.z = matrix4f.m20() * f + matrix4f.m21() * f1 + matrix4f.m22() * f2 + matrix4f.m23() * f3;
            vector4f.w = matrix4f.m30() * f + matrix4f.m31() * f1 + matrix4f.m32() * f2 + matrix4f.m33() * f3;
        }
    }

    /**粒子相关*/
    public static abstract class ToParticle {
        public static <T extends ParticleOptions> void particle(ServerLevel serverLevel, T pType, Vec3 pos, Vec3 move, double pSpeed, int pParticleCount) {
            serverLevel.sendParticles(pType, pos.x, pos.y, pos.z, pParticleCount, move.x, move.y, move.z, pSpeed);
        }

        public static void ExplosionTo(Entity source, Entity target, Level world, int amount) {
            ToEntity.playExplosionSound(target);
            target.hurt(world.damageSources().explosion(target, source), amount);
            if (world instanceof ServerLevel level)
                level.sendParticles(ParticleTypes.EXPLOSION, (target.getX()), (target.getY() + 1), (target.getZ()), 2, 0.1, 0.1, 0.1, 0.1);
        }
    }

    public static class TimerControl {
        public static void timerStartUp(Level level, LivingEntity livingEntity, int timerState){
            timerStartUp(level, timerState);
            Variables.setVariable(livingEntity, ModVariable.TIME_STATUS, timerState);
        }

        public static int getWorldTime(Level level){
            return (int) (int) Variables.getVariable(level, ModVariable.TIME_STATUS);
        }

        public static void timerStartUp(Level level, int timerState){
            Variables.setVariable(level, ModVariable.TIME_STATUS, timerState);
        }

        public static boolean isSlowEntity(Entity entity) {
            return (int) Variables.getVariable(entity, ModVariable.TIME_STATUS) == ITimer.timeSlow;
        }

        public static boolean isPauseEntity(Entity entity) {
            return (int) Variables.getVariable(entity, ModVariable.TIME_STATUS) == ITimer.timeStop;
        }
    }

    /**
     * 超能粒子相关/案例<br>
     * (乱叫喵~)<br>
     * {@link AdvancedParticleBase}粒子使用例
     */
    public static abstract class AdvancedParticleHelper {

        /**
         * 全类型
         * @param drag 粒子的阻力
         * @param duration 粒子的持续时间
         * @param emissive 粒子是否是发光的
         * @param canCollide 粒子是否可以与方块发生碰撞
         * @param components 粒子组件
         */
        public static void addRobbin(Level world, ParticleType<AdvancedParticleData> particle, double x, double y, double z, double motionX, double motionY, double motionZ, boolean faceCamera, double yaw, double pitch, double roll, double faceCameraAngle, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide, ParticleComponent... components) {
            AdvancedParticleBase.spawnParticle(world, particle, x, y, z, motionX, motionY, motionZ, faceCamera, yaw, pitch, roll, faceCameraAngle, scale, r, g, b, a, drag, duration, emissive, canCollide, components);
        }

        /**
         * 创建一个带拖尾的粒子<br>
         * 此方法为创建例<br>
         * ps:好可怕，35个参数，写死你<br>
         * ps:Is never over~
         * @param world           世界
         * @param particle        粒子类型
         * @param ribbon          拖尾粒子类型
         * @param x               坐标X
         * @param y               坐标Y
         * @param z               坐标Z
         * @param motionX         粒子运动X
         * @param motionY         粒子运动Y
         * @param motionZ         粒子运动Z
         * @param faceCamera      是否面向摄像机
         * @param yaw             粒子旋转Y
         * @param pitch           粒子旋转X
         * @param roll            粒子旋转Z
         * @param faceCameraAngle 粒子面向摄像机角度
         * @param scale           粒子大小
         * @param r               粒子颜色R
         * @param g               粒子颜色G
         * @param b               粒子颜色B
         * @param a               粒子透明度
         * @param drag            粒子运动速度
         * @param duration        粒子生命时长
         * @param emissive        粒子是否发光
         * @param canCollide      粒子是否可碰撞
         * @param startTime       粒子开始运动时间
         * @param finalTime       粒子结束运动时间
         * @param rMin            粒子运动速度最小值
         * @param rMax            粒子运动速度最大值
         * @param rotStartX       粒子运动旋转X开始
         * @param rotEndX         粒子运动旋转X结束
         * @param rotStartY       粒子运动旋转Y开始
         * @param rotEndY         粒子运动旋转Y结束
         * @param rotStartZ       粒子运动旋转Z开始
         * @param rotEndZ         粒子运动旋转Z结束
         * @param firstRot        粒子运动旋转X开始
         * @param yAdd            粒子运动Y轴偏移
         * @param length          粒子拖尾长度（>0）
         */
        public static void addPlateRobbin(Level world, ParticleType<AdvancedParticleData> particle, ParticleType<RibbonParticleData> ribbon, double x, double y, double z, double motionX, double motionY, double motionZ, boolean faceCamera, double yaw, double pitch, double roll, double faceCameraAngle, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide
                , float startTime, float finalTime, float rMin, float rMax, float rotStartX, float rotEndX, float rotStartY, float rotEndY, float rotStartZ, float rotEndZ, float firstRot, double yAdd
                , float ribbonScale, int length, RibbonComponent... components) {
            addRobbin(world, particle, x, y, z, motionX, motionY, motionZ, faceCamera, yaw, pitch, roll, faceCameraAngle, scale, r, g, b, a, drag, duration, emissive, canCollide
                    , creatOrbit(new Vec3(x, y, z), startTime, finalTime, rMin, rMax, rotStartX, rotEndX, rotStartY, rotEndY, rotStartZ, rotEndZ, firstRot, faceCamera, yAdd)
                    , creatRibbon(ribbon, length, yaw, pitch, roll, ribbonScale, r, g, b, a, true, emissive, components));
        }
        /**演示使用*/
        @Deprecated
        public static void addCaseRobbin(Level level, ParticleType<AdvancedParticleData> particle, ParticleType<? extends RibbonParticleData> ribbon, Vec3 location, float finalTime, float rMax, float rotStartX, float rotEndX, int ribbonLength, float ribbonScale, float firstRot, double yAdd) {
            AdvancedParticleBase.spawnParticle(level, particle, location.x, location.y, location.z, 0, 0, 0, true, 0.0, 0.0, 0.0, 0.0, 1, (double) 245 / 255, (double) 205 / 255, 1, 1, 1, 45, true, true,
                    new ParticleComponent[]{
                            creatOrbit(location, finalTime, rMax, rotStartX, rotEndX, firstRot, yAdd),
                            creatRibbon(ribbon, ribbonLength, ribbonScale)}
            );
        }

        /**
         * @param particle   粒子类型
         * @param length     拖尾粒子长度
         * @param yaw        粒子旋转Y
         * @param pitch      粒子旋转X
         * @param roll       粒子旋转Z
         * @param scale      粒子大小
         * @param r          粒子颜色R
         * @param g          粒子颜色G
         * @param b          粒子颜色B
         * @param a          粒子透明度
         * @param faceCamera 是否面向摄像机
         * @param emissive   粒子是否发光
         * @param components 组件
         * @return 拖尾粒子的创建
         */
        public static RibbonComponent creatRibbon(ParticleType<? extends RibbonParticleData> particle, int length, double yaw, double pitch, double roll, double scale, double r, double g, double b, double a, boolean faceCamera, boolean emissive, ParticleComponent... components) {
            return new RibbonComponent(particle, length > 0 ? length : 1, yaw, pitch, roll, scale, r / 255, g / 255, b / 255, a, faceCamera, emissive, components);
        }

        /**
         * 默认组件版
         *
         * @param particle   粒子类型
         * @param length     拖尾粒子长度
         * @param yaw        粒子旋转Y
         * @param pitch      粒子旋转X
         * @param roll       粒子旋转Z
         * @param scale      粒子大小
         * @param r          粒子颜色R
         * @param g          粒子颜色G
         * @param b          粒子颜色B
         * @param a          粒子透明度
         * @param faceCamera 是否面向摄像机
         * @param emissive   粒子是否发光
         * @return 拖尾粒子的创建
         */
        public static RibbonComponent creatRibbon(ParticleType<? extends RibbonParticleData> particle, int length, double yaw, double pitch, double roll, double scale, double r, double g, double b, double a, boolean faceCamera, boolean emissive) {
            return creatRibbon(particle, length, yaw, pitch, roll, scale, r, g, b, a, faceCamera, emissive
                    , new RibbonComponent.PropertyOverLength(RibbonComponent.PropertyOverLength.EnumRibbonProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(1.0F, 0.0F))
                    , new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(1.0F, 0.0F), false));
        }

        //使用例
        @Deprecated
        private static RibbonComponent creatRibbon(ParticleType<? extends RibbonParticleData> particle, int length, float scale) {
            return creatRibbon(particle, length, 0.0, 0.0, 0.0, scale, (double) 236, (double) 204, 255, 1, true, true);
        }

        /**
         * 创建一个旋转的粒子
         * @param location   粒子位置
         * @param startTime  粒子开始时间
         * @param finalTime  粒子结束时间
         * @param rMin       粒子最小半径
         * @param rMax       粒子最大半径
         * @param rotStartX  粒子旋转起始角度X
         * @param rotEndX    粒子旋转结束角度X
         * @param rotStartY  粒子旋转起始角度Y
         * @param rotEndY    粒子旋转结束角度Y
         * @param rotStartZ  粒子旋转起始角度Z
         * @param rotEndZ    粒子旋转结束角度Z
         * @param firstRot   粒子旋转起始角度
         * @param faceCamera 是否面向摄像机
         * @param yAdd       粒子位置Y轴偏移
         */
        public static ParticleComponent creatOrbit(Vec3 location, float startTime, float finalTime, float rMin, float rMax, float rotStartX, float rotEndX, float rotStartY, float rotEndY, float rotStartZ, float rotEndZ, float firstRot, boolean faceCamera, double yAdd) {
            return creatOrbit(location
                    , ParticleComponent.KeyTrack.startAndEnd(startTime, finalTime)
                    , new ParticleComponent.KeyTrack(new float[]{rMin, (rMax - rMin)/2 + rMin, rMax}, new float[]{0.0F, 0.5f, 1.0F})
                    , ParticleComponent.KeyTrack.startAndEnd(rotStartX, rotEndX)
                    , ParticleComponent.KeyTrack.startAndEnd(rotStartY, rotEndY)
                    , ParticleComponent.KeyTrack.startAndEnd(rotStartZ, rotEndZ)
                    , firstRot, faceCamera, yAdd);
        }

        /**
         * 创建一个旋转的粒子
         *
         * @param location   粒子位置
         * @param timePeriod 时间周期
         * @param revolve    粒子旋转半径
         * @param xMove      粒子位置X轴偏移
         * @param yMove      粒子位置Y轴偏移
         * @param zMove      粒子位置Z轴偏移
         * @param firstRot   粒子旋转起始角度
         * @param faceCamera 是否面向摄像机
         * @param yAdd       粒子位置Y轴偏移
         */
        public static ParticleComponent creatOrbit(Vec3 location, ParticleComponent.KeyTrack timePeriod, ParticleComponent.KeyTrack revolve, ParticleComponent.KeyTrack xMove, ParticleComponent.KeyTrack yMove, ParticleComponent.KeyTrack zMove, float firstRot, boolean faceCamera, double yAdd) {
            return new ParticleComponent.Orbit(new Vec3[]{(location)}
                    , timePeriod, revolve, xMove, yMove, zMove
                    , firstRot, faceCamera, yAdd);
        }

        //使用例
        @Deprecated
        private static ParticleComponent creatOrbit(Vec3 location, float finalTime, float rMiddon, float rotStartX,
                                                    float rotEndX, float firstRot, double yAdd) {
            return creatOrbit(location
                    , ParticleComponent.KeyTrack.startAndEnd(0f, finalTime)
                    , new ParticleComponent.KeyTrack(new float[]{0, rMiddon, 0}, new float[]{0.0F, 0.5f, 1.0F})
                    , ParticleComponent.KeyTrack.startAndEnd(0, 0)
                    , ParticleComponent.KeyTrack.startAndEnd(rotStartX, rotEndX)
                    , ParticleComponent.KeyTrack.startAndEnd(0, 0)
                    , firstRot, false, yAdd);
        }
    }

    protected final Player player;

    protected GJ(Player player) {
        this.player = player;
    }

    public void use() {
    }
}
