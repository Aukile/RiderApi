package net.ankrya.rider_api.client.shaber;

import net.ankrya.rider_api.RiderApi;
import net.ankrya.rider_api.client.shaber.base.CCShaderInstance;
import net.ankrya.rider_api.client.shaber.base.CCUniform;
import net.ankrya.rider_api.help.GJ;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**欸嘿~ 单纯研究一下看看*/
@Mod.EventBusSubscriber(modid = RiderApi.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModShaders {
    private static class RenderStateShardAccess extends RenderStateShard {
        private static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
        private static final RenderStateShard.LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        private static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }
    }

    public static final float[] COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float tick;
    public static float renderFrame;
    public static CCShaderInstance cosmicShader;
    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;
    public static final RenderType COSMIC_RENDER_TYPE = RenderType.create(RiderApi.MODID + ":cosmic", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader)).setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST).setLightmapState(RenderStateShardAccess.LIGHT_MAP).setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY).setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED).createCompositeState(true));

    public static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(RiderApi.MODID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader = (CCShaderInstance) e;
            cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
            cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
            cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
            cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
            cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
            cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
            cosmicTime.set((float) renderTime + renderFrame);
            cosmicShader.onApply(() -> cosmicTime.set((float) renderTime + renderFrame));
        });
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END) {
            ++renderTime;
            tick += 1F;
            if (tick >= 720.0f) {
                tick = 0.0F;
            }
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPre(final ScreenEvent.Render.Pre e) {
        ModShaders.inventoryRender = true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPost(final ScreenEvent.Render.Post e) {
        ModShaders.inventoryRender = false;
    }
}
