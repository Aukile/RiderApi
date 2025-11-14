package net.ankrya.rider_api.reverse.server;

import net.ankrya.rider_api.reverse.common.IBacker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ServerChunkBlockBacker implements IBacker {

	public final ChunkAccess chunk;
	public final ServerLevel level;
	public final BlockPos pos;
	public final Block block;
	private final DataComponentMap tag;
	
	public ServerChunkBlockBacker(ServerLevel sl, ChunkAccess chunk, BlockPos pos) {
		this.level = sl;
		this.chunk = chunk;
		this.pos = pos;
		BlockState state = sl.getBlockState(pos);
		this.block = state.getBlock();
		if (state.hasBlockEntity()) {
			BlockEntity be = chunk.getBlockEntity(pos);
			tag = be != null ? be.components() : DataComponentMap.EMPTY;
		} else {
			tag = null;
		}
	}
	
	@Override
	public void back() {
		level.setBlock(pos, block.defaultBlockState(), 4);
		if (tag != null) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity != null) {
				blockEntity.setComponents(tag);
			}
		}
	}

}
