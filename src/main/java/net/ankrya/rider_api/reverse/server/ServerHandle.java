package net.ankrya.rider_api.reverse.server;

import net.ankrya.rider_api.reverse.IHandle;
import net.ankrya.rider_api.reverse.common.ChunkEntityBacker;
import net.ankrya.rider_api.reverse.common.IBacker;
import net.ankrya.rider_api.reverse.common.ListBacker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

public record ServerHandle(MinecraftServer server) implements IHandle {

	public ListBacker chunkBlockBacker(ServerLevel sl, ChunkAccess chunk) {
		List<IBacker> list = new ArrayList<>();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
					BlockPos pos = new BlockPos(x + chunk.getPos().x * 16, y, z + chunk.getPos().z * 16);
					list.add(new ServerChunkBlockBacker(sl, chunk, pos));
				}
			}
		}
		return new ListBacker(list.toArray(new IBacker[0]));
	}

	public ListBacker chunkEntityBacker(ServerLevel level, ChunkAccess chunk) {
		List<IBacker> list = new ArrayList<>();
		list.add(() -> {
            for (Entity entity : level.getAllEntities()) {
                if (entity.chunkPosition().equals(chunk.getPos())) {
                    entity.setRemoved(RemovalReason.KILLED);
                }
            }
        });
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Player) && entity.chunkPosition().equals(chunk.getPos())) {
                list.add(new ChunkEntityBacker(chunk, level, entity));
            }
        }
		return new ListBacker(list.toArray(new IBacker[0]));
	}

	@Override
	public void init() {
	}

}
