package net.ankrya.rider_api.help.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 一个轮盘选项。
 * @param id           技能ID
 * @param name         显示名称
 * @param description  描述（中心显示）
 * @param icon         图标贴图，可为 null
 * @param tintColor    无图标时的扇区染色 ARGB，可为 0
 * @param available    是否可选（灰显但仍显示）
 */
public record Skill(int id, Component name, Component description, ResourceLocation icon, int tintColor, boolean available) {
    public static Builder of(int id) { return new Builder(id); }

    public static class Builder {
        private final int id;
        private Component name = Component.empty();
        private Component desc = Component.empty();
        private ResourceLocation icon = null;
        private int tint = 0;
        private boolean available = true;

        public Builder(int id) { this.id = id; }
        public Builder name(Component c) { this.name = c; return this; }
        public Builder name(String s) { this.name = Component.literal(s); return this; }
        public Builder desc(Component c) { this.desc = c; return this; }
        public Builder desc(String s) { this.desc = Component.literal(s); return this; }
        public Builder icon(ResourceLocation r) { this.icon = r; return this; }
        public Builder tint(int argb) { this.tint = argb; return this; }
        public Builder available(boolean b) { this.available = b; return this; }
        public Skill build() { return new Skill(id, name, desc, icon, tint, available); }
    }
}