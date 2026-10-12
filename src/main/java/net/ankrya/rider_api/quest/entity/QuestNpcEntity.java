package net.ankrya.rider_api.quest.entity;

import net.ankrya.rider_api.message.MessageLoader;
import net.ankrya.rider_api.message.common.quest.OpenDialoguePacket;
import net.ankrya.rider_api.quest.data.QuestDefinition;
import net.ankrya.rider_api.quest.data.QuestState;
import net.ankrya.rider_api.quest.event.QuestEvents;
import net.ankrya.rider_api.quest.manager.QuestManager;
import net.ankrya.rider_api.quest.registry.NpcRegistry;
import net.ankrya.rider_api.quest.registry.QuestAttachments;
import net.ankrya.rider_api.quest.registry.QuestRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class QuestNpcEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> NPC_ID =
            SynchedEntityData.defineId(QuestNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DISPLAY_NAME_KEY =
            SynchedEntityData.defineId(QuestNpcEntity.class, EntityDataSerializers.STRING);

    public QuestNpcEntity(EntityType<? extends QuestNpcEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setNoAi(false);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(NPC_ID, BuiltInRegistries.ENTITY_TYPE.getKey(getType()).toString());
        entityData.define(DISPLAY_NAME_KEY, "entity.api.quest_npc");
    }

    public ResourceLocation getNpcId() {
        return new ResourceLocation(entityData.get(NPC_ID));
    }

    public void setNpcId(ResourceLocation id) {
        entityData.set(NPC_ID, id.toString());
    }

    public void setDisplayNameKey(String key) {
        entityData.set(DISPLAY_NAME_KEY, key);
        if (!key.equals("entity.api.quest_npc")){
            this.setCustomName(Component.translatable(key));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide ||!(player instanceof ServerPlayer sp)) {
            return InteractionResult.SUCCESS;
        }
        handleInteract(sp);
        return InteractionResult.CONSUME;
    }

    private void handleInteract(ServerPlayer sp) {
        ResourceLocation npcId = getNpcId();  // 现在是实体注册名
        var questData = QuestAttachments.get(sp);
    
        // 找绑定此 NPC 且玩家正在进行的任务
        Optional<QuestDefinition> activeQuestDef = QuestRegistry.INSTANCE.all().stream()
                .filter(def -> npcId.equals(def.npcId()))
                .filter(def -> questData.get(def.id())
                        .map(p -> p.getState() == QuestState.ACTIVE).orElse(false))
                .findFirst();
    
        if (activeQuestDef.isPresent()) {
            QuestDefinition def = activeQuestDef.get();
    
            // 直接发对话台词，不再把目标塞进对话行
            // 目标由 DialogueScreen 在最后一句自动显示
            List<String> lines = def.dialogues().isEmpty()
                    ? List.of(def.description())   // 没对话就用描述兗底
                    : def.dialogues();

            // openDialogue 内部已经发过包了，原来这里又发一次，对话界面会被打开两次
            openDialogue(sp, npcId, def.id(), lines, false, getDialogueStyle(def));
    
            // 标记对话目标完成（玩家来对话了）
            QuestManager.onDialogueComplete(sp, def.id(), npcId);
            return;
        }
    
        // 找可接取（AVAILABLE）的任务
        Optional<QuestDefinition> availableDef = QuestRegistry.INSTANCE.all().stream()
                .filter(def -> npcId.equals(def.npcId()))
                .filter(def -> questData.get(def.id())
                        .map(p -> p.getState() == QuestState.AVAILABLE).orElse(false))
                .findFirst();
    
        if (availableDef.isPresent()) {
            QuestDefinition def = availableDef.get();
            List<String> lines = def.dialogues().isEmpty()
                    ? List.of(def.description())
                    : def.dialogues();
            openDialogue(sp, npcId, def.id(), lines, false, getDialogueStyle(def));
            return;
        }
    
        // 无任务：打招呼（用 NPC 样式）
        List<String> greetings = NpcRegistry.INSTANCE.get(npcId)
                .map(n -> n.greetings)
                .orElse(List.of("npc.api.default_greeting"));
        ResourceLocation npcStyle = getNpcStyleId();

        openDialogue(sp, npcId, null, greetings, true, npcStyle);
    }

    private boolean openDialogue(ServerPlayer sp, ResourceLocation npcId,
                                 @Nullable ResourceLocation questId,
                                 List<String> lines, boolean greeting,
                                 @Nullable ResourceLocation style) {
        var e = new QuestEvents.DialogueOpen(sp, questId, npcId, greeting);
        if (MinecraftForge.EVENT_BUS.post(e)) return false;
        MessageLoader.getApiLoader().sendToPlayer(new OpenDialoguePacket(npcId, questId, lines, greeting, style), sp);
        return true;
    }
    
    /**
     * 三级样式回退：任务 > NPC > 默认(null)
     */
    private ResourceLocation getDialogueStyle(QuestDefinition questDef) {
        // 1. 优先用任务的样式
        if (questDef.guiStyleId() != null) return questDef.guiStyleId();
        // 2. 其次用 NPC 的样式
        ResourceLocation npcStyle = getNpcStyleId();
        if (npcStyle != null) return npcStyle;
        // 3. 最后默认 null
        return null;
    }
    
    private ResourceLocation getNpcStyleId() {
        return NpcRegistry.INSTANCE.get(getNpcId())
                .map(n -> n.guiStyleId != null ? new ResourceLocation(n.guiStyleId) : null)
                .orElse(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("npc_id", getNpcId().toString());
        tag.putString("display_name_key", entityData.get(DISPLAY_NAME_KEY));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("npc_id"))
            setNpcId(new ResourceLocation(tag.getString("npc_id")));
        if (tag.contains("display_name_key"))
            setDisplayNameKey(tag.getString("display_name_key"));
    }

    @Override
    protected void registerGoals() {
        // NPC 只游荡、不主动攻击
//        this.goalSelector.addGoal(1,
//                new WaterAvoidingRandomStrollGoal(this,0.6));
        this.goalSelector.addGoal(2,
                new LookAtPlayerGoal(
                        this, Player.class, 6.0f));
        this.goalSelector.addGoal(3,
                new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(
            net.minecraft.world.damagesource.DamageSource s) { return null; }
    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() { return null; }
}