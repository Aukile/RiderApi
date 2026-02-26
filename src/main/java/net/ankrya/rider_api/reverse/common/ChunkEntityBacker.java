package net.ankrya.rider_api.reverse.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.lang.reflect.InvocationTargetException;

public class ChunkEntityBacker implements IBacker {

	public final ChunkAccess chunk;
	public final Level level;
	public final Class<? extends Entity> entClass;
	private final EntityType<?> type;
	private final CompoundTag tag;
	
	public ChunkEntityBacker(ChunkAccess chunk, Level level, Entity ent) {
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
		Entity entity = EntityType.loadEntityRecursive(tag, level, (loadedEntity) -> loadedEntity);
		if (entity != null) {
			entity.absMoveTo(tag.getDouble("PosX"), tag.getDouble("PosY"), tag.getDouble("PosZ"));
			level.addFreshEntity(entity);
		} else {
			EntityType.create(tag, level).ifPresent(level::addFreshEntity);
		}
	}

}
