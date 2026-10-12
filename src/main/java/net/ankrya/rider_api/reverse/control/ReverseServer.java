package net.ankrya.rider_api.reverse.control;

import net.ankrya.rider_api.reverse.TMIMain;
import net.ankrya.rider_api.reverse.common.IBacker;
import net.ankrya.rider_api.reverse.common.ListBacker;
import net.ankrya.rider_api.reverse.server.ServerChunkBlockBacker;
import net.ankrya.rider_api.reverse.server.ServerChunkEntityBacker;
import net.ankrya.rider_api.reverse.server.ServerHandle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

public class ReverseServer {
	
	public static final IEntityReverse EntityBacktracking = new IEntityReverse() {
		
		private ServerHandle handle;
		
		private List<ListBacker> backers;
		
		public ServerHandle getHandle() {
			if (handle != TMIMain.serverHandle) {
				handle = TMIMain.serverHandle;
				if (handle == null) {
					backers = null;
				} else {
					backers = new ArrayList<>();
				}
			}
			return handle;
		}

		@Override
		public void pre(Level level, ChunkAccess chunk) {
			ListBacker backer = getHandle().chunkEntityBacker((ServerLevel) level, chunk);
			backers.add(backer);
		}
		
		@Override
		public void post(Level l, ChunkAccess chunk) {
			ServerLevel level = (ServerLevel) l;
            for (Entity entity : level.getAllEntities()) {
                if (entity != null && entity.getType() != EntityType.PLAYER) {
                    if (entity.chunkPosition().equals(chunk.getPos())) {
                        entity.setRemoved(RemovalReason.KILLED);
                    }
                }
            }
			
			backers.forEach((backer) -> {
				IBacker[] iBackers = backer.backers;
                for (IBacker iBacker : iBackers) {
                    if (iBacker instanceof ServerChunkEntityBacker entityBacker) {
//						if (entityBacker.chunk.equals(chunk)) {
                        entityBacker.back();
//						}
                    }
                }
			});
		}
		
		@Override
		public void clear() {
			backers.clear();
		}

	};
	
	public static final IChunkReverse BoxBacktracking = new IChunkReverse() {
		
		private ServerHandle handle;
		
		private List<ListBacker> backers;
		
		public ServerHandle getHandle() {
			if (handle != TMIMain.serverHandle) {
				handle = TMIMain.serverHandle;
				if (handle == null) {
					backers = null;
				} else {
					backers = new ArrayList<>();
				}
			}
			return handle;
		}
		
		@Override
		public void pre(Level level, ChunkAccess chunk) {
			ListBacker backer = getHandle().chunkBlockBacker((ServerLevel) level, chunk);
			backers.add(backer);
		}
		
		@Override
		public void post(Level level, ChunkAccess chunk) {
			backers.forEach((listBacker) -> {
				IBacker[] iBackers = listBacker.backers;
                for (IBacker iBacker : iBackers) {
                    if (iBacker instanceof ServerChunkBlockBacker blockBacker) {
//						if (blockBacker.chunk.equals(chunk)) {
                        blockBacker.back();
//						}
                    }
                }
			});
		}

		@Override
		public void clear() {
			backers.clear();
		}

	};
	
}
