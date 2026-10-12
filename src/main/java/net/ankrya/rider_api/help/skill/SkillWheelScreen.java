package net.ankrya.rider_api.help.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.item.base.armor.BaseRiderArmorBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Matrix4f;

import java.util.List;

public class SkillWheelScreen extends Screen {

    private static final float INNER = 25f;
    private static final float OUTER = 60f;
    private static final float HOVER_EXPAND = 6f;
    private static final int SUB = 32;

    private List<Skill> skills;
    private int selected = -1;
    private float openAnim = 0f;
    private float[] segAnim = new float[0];

    public SkillWheelScreen() { super(Component.literal("Skill Wheel")); }

    public SkillWheelScreen(List<Skill> skills) {
        this();
        this.skills = skills;
    }

    @Override
    protected void init() {
        super.init();
        var mc = Minecraft.getInstance();
        if (skills == null || skills.isEmpty())
            skills = (mc.player == null || !(mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof BaseRiderArmorBase armorBase)) ? List.of() : armorBase.getSkillList(mc.player);
        if (skills.isEmpty()) this.removed();
        segAnim = new float[Math.max(skills.size(), 1)];

        int cur = Variables.getVariable(mc.player, ModVariable.SKILL_OPTION);
        for (int i = 0; i < skills.size(); i++) {
            if (skills.get(i).id() == cur && skills.get(i).available()) { selected = i; break; }
        }
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics gui) {
        gui.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float pt) {
        this.renderBackground(gui);

        openAnim = Math.min(1f, openAnim + pt * 0.15f);
        for (int i = 0; i < segAnim.length; i++) {
            float t = (i == selected) ? 1f : 0f;
            segAnim[i] += (t - segAnim[i]) * Math.min(1f, pt * 0.35f);
        }
        updateSelection(mouseX, mouseY);

        if (skills.isEmpty()) {
            drawCenterText(gui, Component.literal("无可用技能"), Component.literal("请装备相关物品"));
            return;
        }

        float cx = this.width / 2f;
        float cy = this.height / 2f;
        float scale = easeOutBack(openAnim);

        gui.pose().pushPose();
        gui.pose().translate(cx, cy, 0);
        gui.pose().scale(scale, scale, 1f);
        gui.pose().translate(-cx, -cy, 0);

        renderWheel(gui);
        renderIcons(gui);

        gui.pose().popPose();

        renderTexts(gui, scale);
        super.render(gui, mouseX, mouseY, pt);
    }

    private void updateSelection(double mx, double my) {
        int n = skills.size();
        if (n == 0) { selected = -1; return; }
        double dx = mx - this.width / 2.0;
        double dy = my - this.height / 2.0;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d < INNER * 0.6) { selected = -1; return; }

        double ang = Math.atan2(dx, -dy);
        if (ang < 0) ang += Math.PI * 2;
        double seg = (Math.PI * 2) / n;
        int idx = (int) Math.floor((ang + seg / 2) / seg) % n;

        if (!skills.get(idx).available()) { selected = -1; return; }
        selected = idx;
    }

    private void renderWheel(GuiGraphics gui) {
        int n = skills.size();
        float cx = this.width / 2f, cy = this.height / 2f;
        Matrix4f mat = gui.pose().last().pose();

        // ==========================================
        // 【核心修复：彻底关闭背面剔除和深度测试】
        // ==========================================
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();       // 关键！防止三角形因为方向不对被显卡吃掉
        RenderSystem.disableDepthTest();  // 关键！防止和背景打架
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        double segAng = (Math.PI * 2) / n;
        for (int i = 0; i < n; i++) {
            Skill s = skills.get(i);
            float hover = segAnim[i];
            float outer = OUTER + HOVER_EXPAND * hover;
            float inner = INNER;
            double base = i * segAng - segAng / 2 - Math.PI / 2;

            float r, g, b;
            if (s.tintColor() != 0) {
                int c = s.tintColor();
                r = ((c >> 16) & 0xFF) / 255f;
                g = ((c >> 8)  & 0xFF) / 255f;
                b = ( c        & 0xFF) / 255f;
            } else {
                r = 0.1f + 0.3f * hover;
                g = 0.4f + 0.4f * hover;
                b = 0.8f + 0.2f * hover;
            }
            if (!s.available()) { r *= 0.4f; g *= 0.4f; b *= 0.4f; }

            // 扇区内部填充：非常明显的可见度！
            float aInner = 0.25f + 0.25f * hover; // 越靠近圆心越透明 (25% -> 50%)
            float aOuter = 0.60f + 0.40f * hover; // 外圈非常浓郁 (60% -> 100%)

            for (int k = 0; k < SUB; k++) {
                double a1 = base + segAng * k / SUB;
                double a2 = base + segAng * (k + 1) / SUB;

                float x1o = cx + (float)Math.cos(a1) * outer;
                float y1o = cy + (float)Math.sin(a1) * outer;
                float x2o = cx + (float)Math.cos(a2) * outer;
                float y2o = cy + (float)Math.sin(a2) * outer;

                float x1i = cx + (float)Math.cos(a1) * inner;
                float y1i = cy + (float)Math.sin(a1) * inner;
                float x2i = cx + (float)Math.cos(a2) * inner;
                float y2i = cy + (float)Math.sin(a2) * inner;

                // 绘制内部填充三角形
                bb.vertex(mat, x1i, y1i, 0).color(r, g, b, aInner).endVertex();
                bb.vertex(mat, x1o, y1o, 0).color(r, g, b, aOuter).endVertex();
                bb.vertex(mat, x2o, y2o, 0).color(r, g, b, aOuter).endVertex();

                bb.vertex(mat, x1i, y1i, 0).color(r, g, b, aInner).endVertex();
                bb.vertex(mat, x2o, y2o, 0).color(r, g, b, aOuter).endVertex();
                bb.vertex(mat, x2i, y2i, 0).color(r, g, b, aInner).endVertex();
            }

            // 所有的边框线：恢复为半透明白色，不再参与颜色渐变
            float lineAlpha = 0.3f + 0.5f * hover;

            for (int k = 0; k < SUB; k++) {
                double a1 = base + segAng * k / SUB;
                double a2 = base + segAng * (k + 1) / SUB;
                float x1 = cx + (float)Math.cos(a1) * outer;
                float y1 = cy + (float)Math.sin(a1) * outer;
                float x2 = cx + (float)Math.cos(a2) * outer;
                float y2 = cy + (float)Math.sin(a2) * outer;
                quadLine(bb, mat, x1, y1, x2, y2, 1.0f, 1f, 1f, 1f, lineAlpha);
            }

            float x1_in = cx + (float)Math.cos(base) * inner;
            float y1_in = cy + (float)Math.sin(base) * inner;
            float x1_out = cx + (float)Math.cos(base) * outer;
            float y1_out = cy + (float)Math.sin(base) * outer;
            quadLine(bb, mat, x1_in, y1_in, x1_out, y1_out, 1.0f, 1f, 1f, 1f, lineAlpha);

            float x2_in = cx + (float)Math.cos(base + segAng) * inner;
            float y2_in = cy + (float)Math.sin(base + segAng) * inner;
            float x2_out = cx + (float)Math.cos(base + segAng) * outer;
            float y2_out = cy + (float)Math.sin(base + segAng) * outer;
            quadLine(bb, mat, x2_in, y2_in, x2_out, y2_out, 1.0f, 1f, 1f, 1f, lineAlpha);
        }

        BufferUploader.drawWithShader(bb.end());

        // 恢复渲染状态，防止影响其他原版界面
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void quadLine(BufferBuilder bb, Matrix4f mat,
                          float x1, float y1, float x2, float y2,
                          float w, float r, float g, float b, float a) {
        float dx = x2 - x1, dy = y2 - y1;
        float L = (float)Math.sqrt(dx*dx + dy*dy);
        if (L < 1e-4f) return;
        float nx = -dy / L * w * 0.5f, ny = dx / L * w * 0.5f;
        bb.vertex(mat, x1-nx, y1-ny, 0).color(r,g,b,a).endVertex();
        bb.vertex(mat, x1+nx, y1+ny, 0).color(r,g,b,a).endVertex();
        bb.vertex(mat, x2+nx, y2+ny, 0).color(r,g,b,a).endVertex();
        bb.vertex(mat, x1-nx, y1-ny, 0).color(r,g,b,a).endVertex();
        bb.vertex(mat, x2+nx, y2+ny, 0).color(r,g,b,a).endVertex();
        bb.vertex(mat, x2-nx, y2-ny, 0).color(r,g,b,a).endVertex();
    }

    private void renderIcons(GuiGraphics gui) {
        int n = skills.size();
        float cx = this.width / 2f, cy = this.height / 2f;
        double segAng = (Math.PI * 2) / n;
        float midR = (INNER + OUTER) / 2f;

        for (int i = 0; i < n; i++) {
            Skill s = skills.get(i);
            float hover = segAnim[i];
            double a = i * segAng - Math.PI / 2;
            float r = midR + HOVER_EXPAND * 0.5f * hover;

            int x = Math.round(cx + (float)Math.cos(a) * r);
            int y = Math.round(cy + (float)Math.sin(a) * r);

            if (s.icon() != null) {
                RenderSystem.enableBlend();
                float alpha = s.available() ? (0.8f + 0.2f * hover) : 0.4f;
                gui.setColor(1f, 1f, 1f, alpha);
                gui.blit(s.icon(), x - 8, y - 8, 0, 0, 16, 16, 16, 16);
                gui.setColor(1f, 1f, 1f, 1f);
            }
        }
    }

    private void renderTexts(GuiGraphics gui, float scale) {
        float cx = this.width / 2f;
        float cy = this.height / 2f;

        if (selected >= 0 && selected < skills.size()) {
            Skill s = skills.get(selected);

            int nameWidth = this.font.width(s.name());
            gui.drawString(this.font, s.name(), (int)cx - nameWidth / 2, (int)cy - 4, 0xFFFFFFFF, true);

            Component outerText = s.description();
            if (outerText == null || outerText.getString().trim().isEmpty()) {
                outerText = s.name();
            }

            int textColor = (s.tintColor() != 0) ? (s.tintColor() | 0xFF000000) : 0xFFFFAA00;
            if (!s.available()) textColor = 0xFFAAAAAA;

            int n = skills.size();
            double segAng = (Math.PI * 2) / n;
            double a = selected * segAng - Math.PI / 2;

            float textRadius = (OUTER + HOVER_EXPAND + 12f) * scale;
            int tx = Math.round(cx + (float)Math.cos(a) * textRadius);
            int ty = Math.round(cy + (float)Math.sin(a) * textRadius);

            int maxTextWidth = 120;
            List<FormattedCharSequence> lines = this.font.split(outerText, maxTextWidth);
            int totalHeight = lines.size() * this.font.lineHeight;
            int startY = ty - totalHeight / 2;

            float cosA = (float)Math.cos(a);

            if (cosA > 0.1f) {
                int startX = tx + 6;
                for (int j = 0; j < lines.size(); j++) {
                    gui.drawString(this.font, lines.get(j), startX, startY + j * this.font.lineHeight, textColor, true);
                }
            } else if (cosA < -0.1f) {
                for (int j = 0; j < lines.size(); j++) {
                    int lineWidth = this.font.width(lines.get(j));
                    gui.drawString(this.font, lines.get(j), tx - 6 - lineWidth, startY + j * this.font.lineHeight, textColor, true);
                }
            } else {
                for (int j = 0; j < lines.size(); j++) {
                    int lineWidth = this.font.width(lines.get(j));
                    gui.drawString(this.font, lines.get(j), tx - lineWidth / 2, startY + j * this.font.lineHeight, textColor, true);
                }
            }
        } else {
            Component title = Component.translatable("skill_wheel.api.select_skill");
            int w = this.font.width(title);
            gui.drawString(this.font, title, (int)cx - w / 2, (int)cy - 4, 0xFF888888, true);
        }
    }

    private void drawCenterText(GuiGraphics gui, Component a, Component b) {
        int cx = this.width / 2, cy = this.height / 2;
        int w1 = this.font.width(a);
        gui.drawString(this.font, a, cx - w1 / 2, cy - 8, 0xFFFFFFFF, true);
        if (!b.getString().isEmpty()) {
            int w2 = this.font.width(b);
            gui.drawString(this.font, b, cx - w2 / 2, cy + 4, 0xFFCCCCCC, true);
        }
    }

    private float easeOutBack(float x) {
        float c1 = 1.70158f, c3 = c1 + 1f;
        return 1f + c3 * (float)Math.pow(x - 1, 3) + c1 * (float)Math.pow(x - 1, 2);
    }

    @Override
    public void onClose() {
        if (selected >= 0 && selected < skills.size()) {
            Skill s = skills.get(selected);
            if (s.available()) Variables.setVariable(Minecraft.getInstance().player, ModVariable.SKILL_OPTION, s.id());
        }
        super.onClose();
    }

    @Override
    public boolean keyReleased(int key, int scan, int mods) {
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL
                || key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL) {
            this.onClose(); return true;
        }
        return super.keyReleased(key, scan, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 && selected >= 0) { this.onClose(); return true; }
        return super.mouseClicked(mx, my, btn);
    }
}