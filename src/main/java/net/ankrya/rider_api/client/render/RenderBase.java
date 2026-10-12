package net.ankrya.rider_api.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class RenderBase {
    private static final Function<ResourceLocation, RenderType> GLOWING_RENDER_TYPE;

    static {
        GLOWING_RENDER_TYPE = Util.memoize(Shards::glowing);
    }

    public static RenderType glowing(ResourceLocation location) {
        return GLOWING_RENDER_TYPE.apply(location);
    }

    /** 1.20 中 RenderStateShard 的这些常量是 protected，借子类访问（不需要额外的 AT） */
    private static abstract class Shards extends RenderType {
        private Shards() {
            super(null, null, null, 0, false, false, null, null);
        }

        private static RenderType glowing(ResourceLocation location) {
            RenderStateShard.TextureStateShard texturestateshard = new RenderStateShard.TextureStateShard(location, false, false);
            return RenderType.create(
                    "glowing_effect",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
                            .setTextureState(texturestateshard)
                            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                            .setCullState(RenderType.NO_CULL)
                            .setLightmapState(RenderType.LIGHTMAP)
                            .setOverlayState(RenderType.OVERLAY)
                            .createCompositeState(false));
        }
    }
}
