package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.NMessageCreater;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于发送网络包至服务器再发送至客户端（仅限于{@link INMessage}的实现类）<br>
 * <br>
 * 用法：<br>
 * <ul>
 *     <li>客户端：{@code MessageLoader.sendToServer(new NMessageCreater(new AllPackt(message)))}<br>
 *     服务端会先执行一次 message，然后转发给所有正在追踪发送者的玩家（默认不包括发送者自己，
 *     因为发送者一般已经在本地执行过了；需要包括时用 {@link #AllPackt(INMessage, boolean)}）</li>
 *     <li>服务端：直接把 AllPackt 发给客户端也可以，客户端收到后执行 message</li>
 *     <li>不确定在哪一端时，用 {@link #sendToAllTracking(INMessage, Player)}，两端都能调用</li>
 * </ul>
 * 客户端执行被转发的 message 时，{@code GJ.Easy.getPlayer(ctx)} 返回的是<b>最初的发送者</b>，而不是本地玩家。<br>
 * <br>
 * <b>安全：</b>为了防止恶意客户端借服务器转发任意消息（比如 {@link MoveMessage} 改别人的速度），
 * 只有通过 {@link #allowRelay(Class)} 登记过的消息类才会被转发。没登记的消息仍然会在服务端执行，
 * 但不会转发，并在日志里给出警告。
 */
public class AllPackt implements INMessage {
    /** 客户端执行被转发的消息时的原始发送者，{@link GJ.Easy#getPlayer(NetworkEvent.Context)} 会优先返回它 */
    private static final ThreadLocal<Player> SOURCE = new ThreadLocal<>();

    /** 当前正在执行的被转发消息的原始发送者；不在转发中时为 null */
    public static Player currentSource(){
        return SOURCE.get();
    }

    /** 允许由服务器转发给其他客户端的消息类 */
    private static final Set<Class<? extends INMessage>> RELAY_WHITELIST = ConcurrentHashMap.newKeySet();
    private static final Set<Class<?>> WARNED = ConcurrentHashMap.newKeySet();

    static {
        allowRelay(PlayerAnimationMessage.class);
        allowRelay(PlayerAnimationStopMessage.class);
        allowRelay(PlayerAnimationBlenderMessage.class);
    }

    List<Integer> types = new ArrayList<>();
    List<?> values = new ArrayList<>();
    final String clazz;
    final INMessage message;
    /** 最初发送者的实体 id，由服务器在转发时填写；-1 表示没有 */
    int sourceId = -1;
    /** 转发时是否也发回给发送者自己 */
    boolean includeSender = false;

    public AllPackt(String name, INMessage message, boolean hasData, int data) {
        this.clazz = name;
        this.message = message;
        if (this.message instanceof AllPackt)
            throw new RuntimeException("AllPackt can't be AllPackt");
    }

    public AllPackt(INMessage message){
        this(message.getClass().getName(), message, true, 2);
    }

    public AllPackt(String name, INMessage message){
        this(name, message, true, 2);
    }

    public AllPackt(Class<?> clazz, INMessage message) {
        this(clazz.getName(), message, true, 2);
    }

    /**
     * @param includeSender 服务器转发时是否也发回给发送者自己
     */
    public AllPackt(INMessage message, boolean includeSender){
        this(message);
        this.includeSender = includeSender;
    }

    /** 反序列化用 */
    public AllPackt(String name, INMessage message, int sourceId, boolean includeSender){
        this(name, message);
        this.sourceId = sourceId;
        this.includeSender = includeSender;
    }

    /**
     * 登记一个允许被服务器转发的消息类。<br>
     * 只登记"在客户端执行也安全"的消息（比如播放动画、粒子）。不要登记会修改其他玩家状态的消息
     */
    public static void allowRelay(Class<? extends INMessage> messageClass){
        RELAY_WHITELIST.add(messageClass);
    }

    public static boolean canRelay(Class<?> messageClass){
        return RELAY_WHITELIST.contains(messageClass);
    }

    /**
     * 客户端、服务端都可以调用：让所有正在追踪 player 的客户端（包括 player 自己）执行 message。<br>
     * 客户端调用时 message 的类必须先用 {@link #allowRelay(Class)} 登记
     */
    public static void sendToAllTracking(INMessage message, Player player){
        if (player.level().isClientSide())
            MessageLoader.getApiLoader().sendToServer(new NMessageCreater(new AllPackt(message, true)));
        else MessageLoader.getApiLoader().sendToEntityAndSelf(new NMessageCreater(message), player);
    }

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf) {
        message.toBytes(buf);
        buf.writeUtf(clazz);
        buf.writeInt(sourceId);
        buf.writeBoolean(includeSender);
    }

    /**
     * 原来的逻辑是反的：服务端收到后只在服务端执行、不转发，
     * 客户端收到后又去调用只有服务端能用的 sendToAllTracking
     */
    @Override
    public void run(NetworkEvent.Context ctx) {
        ctx.enqueueWork(()->{
            if (message instanceof AllPackt) return;
            if (ctx.getDirection().getReceptionSide().isServer()) {
                Player sender = ctx.getSender();
                if (sender == null) return;
                // 1. 服务端执行一次（和原来的行为一致）
                message.run(ctx);
                // 2. 转发。发送者 id 由服务器自己填写，不信任客户端发来的值
                if (!canRelay(message.getClass())) {
                    if (WARNED.add(message.getClass()))
                        RiderApi.LOGGER.warn("[RiderApi] AllPackt: {} is not registered by AllPackt.allowRelay(), it will not be relayed to other clients", message.getClass().getName());
                    return;
                }
                NMessageCreater relay = new NMessageCreater(new AllPackt(clazz, message, sender.getId(), includeSender));
                if (includeSender) MessageLoader.getApiLoader().sendToEntityAndSelf(relay, sender);
                else MessageLoader.getApiLoader().sendToAllTracking(relay, sender);
            } else {
                // 客户端：执行期间 GJ.Easy.getPlayer(ctx) 返回最初的发送者
                Player source = null;
                Player local = GJ.Easy.getPlayer(ctx);
                if (sourceId >= 0 && local != null) {
                    Entity entity = local.level().getEntity(sourceId);
                    if (entity instanceof Player player) source = player;
                }
                if (source == null) message.run(ctx);
                else {
                    SOURCE.set(source);
                    try {
                        message.run(ctx);
                    } finally {
                        SOURCE.remove();
                    }
                }
            }
        });
    }
}
