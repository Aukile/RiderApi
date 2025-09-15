package net.ankrya.rider_api.message;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.message.common.LoopSoundMessage;
import net.ankrya.rider_api.message.common.SyncVariableMessage;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public final class MessageLoader {

    @SubscribeEvent
    public static void load(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RiderApi.MODID);
        registrar.playBidirectional(SyncVariableMessage.TYPE, SyncVariableMessage.CODEC, new DirectionalPayloadHandler<>(SyncVariableMessage::handle, SyncVariableMessage::handleServer));
        registrar.playBidirectional(LoopSoundMessage.TYPE, LoopSoundMessage.CODEC, new DirectionalPayloadHandler<>(LoopSoundMessage::handle, LoopSoundMessage::handleServer));
        registrar.playBidirectional(MessageCreater.TYPE, MessageCreater.CODEC, new DirectionalPayloadHandler<>(MessageCreater::run,MessageCreater::serverRun));
        registrar.playBidirectional(EXMessageCreater.TYPE, EXMessageCreater.CODEC, new DirectionalPayloadHandler<>(EXMessageCreater::run, EXMessageCreater::serverRun));
        registrar.playBidirectional(NMessageCreater.TYPE, NMessageCreater.CODEC, new DirectionalPayloadHandler<>(NMessageCreater::run, NMessageCreater::serverRun));

    }

    //下面的方法有点意义不明？ 额，当搬版本的集中处理器。。。大概

    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayersNearby(MSG message, ServerPlayer player) {
        sendToPlayer(message, player);
        PacketDistributor.sendToPlayersNear((ServerLevel) player.level(), player, player.getX(), player.getY(), player.getZ(), 64, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayersInDimension(MSG message, Level level) {
        if (level instanceof ServerLevel serverLevel)
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToPlayersInDimension(MSG message, Entity entity) {
        if (!entity.level().isClientSide)
            PacketDistributor.sendToPlayersInDimension((ServerLevel) entity.level(), message);
    }

    public static <MSG extends CustomPacketPayload> void sendToEntityAndSelf(MSG message, Entity entity) {
        if (!entity.level().isClientSide)
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }
}
