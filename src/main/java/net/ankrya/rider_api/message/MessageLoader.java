package net.ankrya.rider_api.message;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.message.common.*;
import net.ankrya.rider_api.message.common.quest.AcceptQuestPacket;
import net.ankrya.rider_api.message.common.quest.DialogueActionPacket;
import net.ankrya.rider_api.message.common.quest.OpenDialoguePacket;
import net.ankrya.rider_api.message.common.quest.SyncQuestDataPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageLoader {
    private static MessageLoader apiLoader;

    public static MessageLoader getApiLoader() {
        if (apiLoader == null) apiLoader = new MessageLoader(RiderApi.MODID);
        return apiLoader;
    }

    public final SimpleChannel instance;
    private static final String PROTOCOL_VERSION = "1";

    public int id = 0;

    private MessageLoader(String modid) {
        instance = NetworkRegistry.newSimpleChannel(new ResourceLocation(modid, "main")
                , () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    }

    public void load() {
        registerMessage(LoopSoundMessage.class, LoopSoundMessage::toBuf, LoopSoundMessage::fromBuf, LoopSoundMessage::handle);
        registerMessage(MessageCreater.class, MessageCreater::toBuf, MessageCreater::fromBuf, MessageCreater::run);
        registerMessage(EXMessageCreater.class, EXMessageCreater::toBuf, EXMessageCreater::fromBuf, EXMessageCreater::run);
        registerMessage(NMessageCreater.class, NMessageCreater::toBuf, NMessageCreater::fromBuf, NMessageCreater::run);
        registerMessage(SyncVariableMessage.class, SyncVariableMessage::encode, SyncVariableMessage::decode, SyncVariableMessage::handle);
        registerMessage(GeoItemIdAnimation.class, GeoItemIdAnimation::toBuf, GeoItemIdAnimation::fromBuf, GeoItemIdAnimation::handle);
        registerMessage(AddEffectPacket.class, AddEffectPacket::encode, AddEffectPacket::decode, AddEffectPacket::handle);
        registerMessage(RemoveEffectPacket.class, RemoveEffectPacket::encode, RemoveEffectPacket::decode, RemoveEffectPacket::handle);
        registerMessage(SyncQuestDataPacket.class, SyncQuestDataPacket::encode, SyncQuestDataPacket::decode, SyncQuestDataPacket::handle);
        registerMessage(OpenDialoguePacket.class, OpenDialoguePacket::encode, OpenDialoguePacket::decode, OpenDialoguePacket::handle);
        registerMessage(DialogueActionPacket.class, DialogueActionPacket::encode, DialogueActionPacket::decode, DialogueActionPacket::handle);
        registerMessage(AcceptQuestPacket.class, AcceptQuestPacket::encode, AcceptQuestPacket::decode, AcceptQuestPacket::handle);
    }

    private <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        instance.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
    }

    public <MSG> void sendToServer(MSG message) {
        instance.sendToServer(message);
    }

    public <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        instance.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public <MSG> void sendToAllTracking(MSG message, Entity entity) {
        // 在客户端调用服务端的发送方法会崩溃
        if (!entity.level().isClientSide)
            instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public <MSG> void sendToPlayersNearby(MSG message, Player player) {
        if (!player.level().isClientSide)
            instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
    }

    public <MSG> void sendToPlayersNearbyAndSelf(MSG message, Player player) {
        if (!player.level().isClientSide)
            instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), message);
    }

    public <MSG> void sendToAll(MSG message) {
        instance.send(PacketDistributor.ALL.noArg(), message);
    }

    public <MSG> void sendToPlayersInDimension(MSG message, Player player) {
        if (!player.level().isClientSide)
            instance.send(PacketDistributor.DIMENSION.with(() -> player.level().dimension()), message);
    }

    public <MSG> void sendToPlayersInDimension(MSG message, ServerLevel level) {
        instance.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public <MSG> void sendToPlayersInDimension(MSG message, Level level) {
        if (level instanceof ServerLevel serverLevel)
            sendToPlayersInDimension(message, serverLevel);
    }

    public <MSG> void sendToPlayersInDimension(MSG message, Entity entity) {
        if (entity.level() instanceof ServerLevel serverLevel)
            sendToPlayersInDimension(message, serverLevel);
    }

    public <MSG>  void sendToEntityAndSelf(MSG message, Entity entity) {
        if (!entity.level().isClientSide) {
            instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
        }
    }
    // ---------------- 自动判断端：不用管现在是客户端还是服务端 ----------------

    /**
     * 自动判断端发送：<br>
     * 客户端 → 发给服务端<br>
     * 服务端 → 发给所有在线玩家<br>
     * 按当前线程判断，请在主线程（渲染线程/服务器线程）里调用；网络处理里请放在 enqueueWork 中
     */
    public <MSG> void sendAuto(MSG message) {
        if (isClientThread()) sendToServer(message);
        else sendToAll(message);
    }

    /**
     * 按实体所在的端发送：<br>
     * 客户端 → 发给服务端<br>
     * 服务端 → 发给所有能看到这个实体的玩家（实体是玩家时也包括他自己）
     */
    public <MSG> void sendAuto(MSG message, Entity entity) {
        if (entity.level().isClientSide()) sendToServer(message);
        else sendToEntityAndSelf(message, entity);
    }

    /**
     * 按世界所在的端发送：<br>
     * 客户端 → 发给服务端<br>
     * 服务端 → 发给这个维度里的所有玩家
     */
    public <MSG> void sendAuto(MSG message, Level level) {
        if (level.isClientSide()) sendToServer(message);
        else if (level instanceof ServerLevel serverLevel) sendToPlayersInDimension(message, serverLevel);
    }

    /**
     * 按玩家所在的端发送：<br>
     * 客户端 → 发给服务端<br>
     * 服务端 → 只发给这个玩家
     */
    public <MSG> void sendAutoToPlayer(MSG message, Player player) {
        if (player.level().isClientSide()) sendToServer(message);
        else if (player instanceof ServerPlayer serverPlayer) sendToPlayer(message, serverPlayer);
    }

    /** 当前线程是否是客户端逻辑端（专用服务器上永远是 false） */
    public static boolean isClientThread() {
        if (FMLEnvironment.dist.isDedicatedServer()) return false;
        return EffectiveSide.get().isClient();
    }

    public <ITEM> void sendToAny(ITEM message) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            instance.send(PacketDistributor.ALL.noArg(), message);
        } else {
            instance.sendToServer(message);
        }
    }

    public <ITEM> void sendToAny(ITEM message, ServerPlayer player) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            instance.send(PacketDistributor.PLAYER.with(() -> player), message);
        } else {
            instance.sendToServer(message);
        }
    }
}
