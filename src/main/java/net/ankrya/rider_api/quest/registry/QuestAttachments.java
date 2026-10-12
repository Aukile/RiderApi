package net.ankrya.rider_api.quest.registry;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.quest.data.PlayerQuestData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 玩家任务数据。<br>
 * 主分支（NeoForge）用的是 Attachment，1.20 Forge 没有 Attachment，这里用 Capability 实现同样的效果：
 * 跟随玩家存档、死亡后保留（copyOnDeath）。<br>
 * 获取数据统一用 {@link #get(Player)}（对应主分支的 player.getData(QuestAttachments.QUEST_DATA.get())）
 */
public class QuestAttachments {
    public static final Capability<PlayerQuestData> QUEST_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = new ResourceLocation(RiderApi.MODID, "quest_data");

    public static PlayerQuestData get(Player player) {
        // 能力总是会附加到玩家上；拿不到时（比如 Clone 时旧玩家的能力已失效）返回空数据，避免空指针
        return player.getCapability(QUEST_DATA).resolve().orElseGet(PlayerQuestData::new);
    }

    /** 在主类构造里调用，传入 MOD 总线 */
    public static void register(IEventBus modBus) {
        modBus.addListener(QuestAttachments::registerCaps);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, QuestAttachments::attach);
        MinecraftForge.EVENT_BUS.addListener(QuestAttachments::onClone);
    }

    private static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(PlayerQuestData.class);
    }

    private static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player)
            event.addCapability(ID, new Provider());
    }

    /** 死亡/从末地返回时复制任务进度（对应主分支的 copyOnDeath） */
    private static void onClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        // Forge 在 Clone 时旧玩家的能力已经失效，需要先恢复
        original.reviveCaps();
        original.getCapability(QUEST_DATA).ifPresent(old ->
                event.getEntity().getCapability(QUEST_DATA).ifPresent(now -> {
                    PlayerQuestData copy = PlayerQuestData.fromNbt(old.toNbt());
                    now.clearAll();
                    copy.all().values().forEach(now::put);
                }));
        original.invalidateCaps();
    }

    private static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerQuestData data = new PlayerQuestData();
        private final LazyOptional<PlayerQuestData> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return QUEST_DATA.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.toNbt();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            PlayerQuestData loaded = PlayerQuestData.fromNbt(tag);
            data.clearAll();
            loaded.all().values().forEach(data::put);
        }
    }
}
