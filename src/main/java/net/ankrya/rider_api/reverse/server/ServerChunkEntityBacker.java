package net.ankrya.rider_api.reverse.server;

import net.ankrya.rider_api.reverse.common.IBacker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.lang.reflect.InvocationTargetException;

public class ServerChunkEntityBacker implements IBacker {

	public final ChunkAccess chunk;
	public final ServerLevel level;
	public final Class<? extends Entity> entClass;
	private final EntityType type;
	private final CompoundTag tag;
	
	public ServerChunkEntityBacker(ChunkAccess chunk, ServerLevel level, Entity ent) {
		this.chunk = chunk;
		this.level = level;
		this.type = ent.getType();
		this.entClass = ent.getClass();
		CompoundTag ret = new CompoundTag();
		String id = ent.getEncodeId();
		if (id != null) ret.putString("id", ent.getEncodeId());
		this.tag = ent.saveWithoutId(ret);
	}
	
	@Override
	public void back() {
		try {
			Entity entity = entClass.getConstructor(EntityType.class, Level.class).newInstance(type, level);
			int id = entity.getId();
			entity.load(tag);
			entity.setId(id);
			level.addFreshEntity(entity);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

}
