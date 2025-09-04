package net.ankrya.rider_api.client.particle;

import net.ankrya.rider_api.init.ApiRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ParticleUtil {
    public static void spawnSlashParticles(Player player, Level level) {
        if (level.isClientSide) {
            // 获取玩家位置和朝向
            double px = player.getX();
            double py = player.getY() + player.getEyeHeight() * 0.8;
            double pz = player.getZ();
            float playerYaw = player.getYRot();
            double radians = Math.toRadians(playerYaw + 90);

            int particleCount = 20;
            Random random = new Random();

            for (int i = 0; i < particleCount; i++) {
                double t = (double)i / particleCount;

                // 刀光弧线计算公式
                double r = 1.5 + 0.5 * Math.sin(2 * Math.PI * t);
                double angle = Math.PI * (0.25 + 0.5 * t);
                double curve = 0.3 * Math.sin(2 * Math.PI * t);

                // 计算基础位置
                double baseX = r * Math.cos(radians + angle);
                double baseZ = r * Math.sin(radians + angle);

                // 添加弯曲效果
                double offsetX = curve * Math.cos(radians + angle + Math.PI/2);
                double offsetZ = curve * Math.sin(radians + angle + Math.PI/2);

                // 最终位置
                double x = px + baseX + offsetX;
                double y = py + 1.2 + 0.4 * Math.sin(2 * Math.PI * t);
                double z = pz + baseZ + offsetZ;

                // 添加一些随机性使效果更自然
                x += (random.nextDouble() - 0.5) * 0.2;
                y += (random.nextDouble() - 0.5) * 0.1;
                z += (random.nextDouble() - 0.5) * 0.2;

                // 生成粒子
                level.addParticle((ParticleOptions) ApiRegister.getRegisterObject("katana_slash", ParticleType.class).get(), x, y, z, 0, 0, 0);
            }
        }
    }
}
