package net.ankrya.rider_api.message.ex_message;

import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.message.INMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class MoveMessage implements INMessage {
    private final int id;
    private final double vx;
    private final double vy;
    private final double vz;

    public MoveMessage(int id, double vx, double vy, double vz) {
        this.id = id;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public MoveMessage(Player player, double vx, double vy, double vz) {
        this(player.getId(), vx, vy, vz);
    }

    public MoveMessage(Player player, Vec3 move) {
        this(player, move.x, move.y, move.z);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        INMessage.autoWriteAll(buf, id, vx, vy, vz);
    }

    @Override
    public void run(NetworkEvent.Context ctx) {
        if (ctx.getDirection().getReceptionSide().isClient()){
            ctx.enqueueWork(this::move);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void move() {
        Level level = Minecraft.getInstance().level;
        if (level != null){
            Entity entity = level.getEntity(id);
            if (entity != null) {
                entity.setDeltaMovement(vx, vy, vz);
            }
        }
    }
}
