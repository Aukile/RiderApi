package net.ankrya.rider_api.quest.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ankrya.rider_api.quest.editor.translation.TranslationManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class QuestEditorScreen extends Screen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final QuestEditorData data = new QuestEditorData();

    private TabManager tabManager;
    private TabNavigationBar tabBar;

    public QuestEditorScreen() {
        super(Component.translatable("gui.api.editor.title"));
    }

    @Override
    protected void init() {
        tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

        GridLayoutTab tab1 = new BasicInfoTab(this, data);
        GridLayoutTab tab2 = new ObjectivesRewardsTab(this, data);
        GridLayoutTab tab3 = new DialogueStyleTab(this, data);

        tabBar = TabNavigationBar.builder(tabManager, width)
                .addTabs(tab1, tab2, tab3)
                .build();
        addRenderableWidget(tabBar);

        tabBar.selectTab(0, false);
        tabBar.arrangeElements();

        // 内容区从标签栏底部开始，到底部按钮上方结束
        int top = tabBar.getRectangle().bottom();
        tabManager.setTabArea(new ScreenRectangle(0, top, width, height - top - 40));

        addRenderableWidget(Button.builder(
                Component.translatable("gui.api.editor.export"),
                b -> exportJson()
        ).bounds(width / 2 - 100, height - 54, 96, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.api.editor.import"),
                b -> minecraft.setScreen(new QuestImportScreen(this, data))
        ).bounds(width / 2 + 4, height - 54, 96, 20).build());
    }

    private void exportJson() {
        try {
            var root = data.toJson();
            File jsonFile = new File("quest_export_" + System.currentTimeMillis() + ".json");
            try (FileWriter w = new FileWriter(jsonFile)) { GSON.toJson(root, w); }

            // 收集用到的翻译键
            Set<String> usedKeys = new HashSet<>();
            usedKeys.add(data.title);
            usedKeys.add(data.description);
            data.objectives.forEach(o -> usedKeys.add(o.label));
            data.waves.forEach(w -> {
                if (!w.announce.isEmpty()) usedKeys.add(w.announce);
                if (!w.bossBarName.isEmpty()) usedKeys.add(w.bossBarName);
                w.mobs.forEach(m -> { if (!m.customName.isEmpty()) usedKeys.add(m.customName); });
            });
            usedKeys.addAll(data.dialogues);
            if (data.acceptMessageKey != null) usedKeys.add(data.acceptMessageKey);
            if (data.completeMessageKey != null) usedKeys.add(data.completeMessageKey);
            usedKeys.removeIf(String::isEmpty);

            // 只导出 en_us
            File enFile = new File("quest_export_en_us_" + System.currentTimeMillis() + ".json");
            TranslationManager.INSTANCE.exportFiltered(usedKeys, enFile);

            minecraft.player.sendSystemMessage(
                    Component.translatable("gui.api.editor.export_success_with_lang",
                            jsonFile.getName(), enFile.getName()));
            onClose();
        } catch (Exception ex) {
            minecraft.player.sendSystemMessage(
                    Component.translatable("gui.api.editor.export_failed", ex.getMessage()));
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}