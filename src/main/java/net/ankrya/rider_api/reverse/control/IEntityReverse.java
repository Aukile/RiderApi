package net.ankrya.rider_api.reverse.control;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface IEntityReverse {
	
	void pre(Level level, ChunkAccess chunk);
	
	void post(Level level, ChunkAccess chunk);
	
	void clear();

}
