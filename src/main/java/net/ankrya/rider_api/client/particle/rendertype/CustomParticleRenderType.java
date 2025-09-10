package net.ankrya.rider_api.client.particle.rendertype;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * 自定义渲染类型
 * @author Aistray
 */
@OnlyIn(Dist.CLIENT)
public interface CustomParticleRenderType {
    ParticleRenderType PARTICLE_SHEET_LIT_TRANSLUCENT = new ParticleRenderType() {
        public void begin(BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.depthMask(true);
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            GLSetTexture(TextureAtlas.LOCATION_PARTICLES);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tesselator) {
            tesselator.getBuilder().setQuadSorting(VertexSorting.byDistance(0.0F, 0.0F, 0.0F));
            tesselator.end();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        }

        @Override
        public String toString() {
            return "TRANSLUCENT_GLOWING";
        }
    };

    public static void GLSetTexture(ResourceLocation texture){
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstracttexture = texturemanager.getTexture(texture);
        RenderSystem.bindTexture(abstracttexture.getId());
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.setShaderTexture(0, abstracttexture.getId());
    }
}
