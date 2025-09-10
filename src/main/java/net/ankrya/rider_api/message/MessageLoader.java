package net.ankrya.rider_api.message;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.message.common.LoopSoundMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MessageLoader {
    private static MessageLoader loader;

    public static MessageLoader getLoader() {
        if (loader == null) loader = new MessageLoader();
        return loader;
    }

    public final SimpleChannel instance;
    private static final String PROTOCOL_VERSION = "1";

    public int id = 0;

    private MessageLoader() {
        instance = NetworkRegistry.newSimpleChannel(GJ.Easy.getApiResource("main")
                , () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    }

    public void load() {
        registerMessage(LoopSoundMessage.class, LoopSoundMessage::toBuf, LoopSoundMessage::fromBuf, LoopSoundMessage::handle);
        registerMessage(MessageCreater.class, MessageCreater::toBuf, MessageCreater::fromBuf, MessageCreater::run);
        registerMessage(EXMessageCreater.class, EXMessageCreater::toBuf, EXMessageCreater::fromBuf, EXMessageCreater::run);
        registerMessage(NMessageCreater.class, NMessageCreater::toBuf, NMessageCreater::fromBuf, NMessageCreater::run);
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

    public <MSG> void sendToPlayersNearby(MSG message, ServerPlayer player) {
        instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
    }

    public <MSG> void sendToPlayersNearbyAndSelf(MSG message, ServerPlayer player) {
        instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), message);
    }

    public <MSG> void sendToAll(MSG message) {
        instance.send(PacketDistributor.ALL.noArg(), message);
    }

    public <MSG> void sendToPlayersInDimension(MSG message, ServerPlayer player) {
        instance.send(PacketDistributor.DIMENSION.with(() -> player.level().dimension()), message);
    }

    public <MSG> void sendToPlayersInDimension(MSG message, ServerLevel level) {
        instance.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public <MSG>  void sendToEntityAndSelf(MSG message, Entity entity) {
        instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
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
