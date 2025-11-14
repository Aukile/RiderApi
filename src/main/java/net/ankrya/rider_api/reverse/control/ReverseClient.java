package net.ankrya.rider_api.reverse.control;

import net.ankrya.rider_api.reverse.TMIMain;
import net.ankrya.rider_api.reverse.client.ClientHandle;
import net.ankrya.rider_api.reverse.common.ChunkBlockBacker;
import net.ankrya.rider_api.reverse.common.IBacker;
import net.ankrya.rider_api.reverse.common.ListBacker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

public class ReverseClient {

	public static final IChunkReverse BoxBacktracking = new IChunkReverse() {
		
		private ClientHandle handle;
		private ClientLevel level;
		
		private List<ListBacker> backers;
		
		public ClientHandle getHandle() {
			if (handle != TMIMain.clientHandle) {
				handle = TMIMain.clientHandle;
			}
			if (level != Minecraft.getInstance().level) {
				level = Minecraft.getInstance().level;
				if (level == null) {
					backers = null;
				} else {
					backers = new ArrayList<>();
				}
			}
			return handle;
		}
		
		@Override
		public void pre(Level level, ChunkAccess chunk) {
			ListBacker backer = getHandle().chunkBlockBacker((ClientLevel) level, chunk);
			backers.add(backer);
		}
		
		@Override
		public void post(Level level, ChunkAccess chunk) {
			backers.forEach((listBacker) -> {
				IBacker[] iBackers = listBacker.backers;
                for (IBacker iBacker : iBackers) {
                    if (iBacker instanceof ChunkBlockBacker blockBacker) {
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
