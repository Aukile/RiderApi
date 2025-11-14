package net.ankrya.rider_api.reverse.client;

import net.ankrya.rider_api.reverse.IHandle;
import net.ankrya.rider_api.reverse.common.ChunkBlockBacker;
import net.ankrya.rider_api.reverse.common.IBacker;
import net.ankrya.rider_api.reverse.common.ListBacker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClientHandle implements IHandle {

	public ListBacker chunkBlockBacker(ClientLevel sl, ChunkAccess chunk) {
		List<IBacker> list = new ArrayList<IBacker>();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
					BlockPos pos = new BlockPos(x + chunk.getPos().x * 16, y, z + chunk.getPos().z * 16);
					list.add(new ChunkBlockBacker(sl, chunk, pos));
				}	
			}
		}
		return new ListBacker(list.toArray(new IBacker[0]));
	}
	
	public ListBacker chunkentityBacker(ClientLevel level, ChunkAccess chunk) {
		List<IBacker> list = new ArrayList<IBacker>();
		list.add(new IBacker() {
			
			@Override
			public void back() {
				Iterator<Entity> entitys = level.entitiesForRendering().iterator();
				while (entitys.hasNext()) {
					Entity entity = entitys.next();
					if (entity.chunkPosition().equals(chunk.getPos())) {
						entity.setRemoved(RemovalReason.KILLED);
					}
				}
			}
			
		});
		return new ListBacker(list.toArray(new IBacker[0]));
	}
	
	@Override
	public void init() {
		
	}

}