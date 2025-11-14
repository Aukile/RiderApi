package net.ankrya.rider_api.reverse.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Objects;

public class ChunkBlockBacker implements IBacker {

	public final ChunkAccess chunk;
	public final Level level;
	public final BlockPos pos;
	public final Block block;
	private final DataComponentMap tag;
	
	public ChunkBlockBacker(Level serverLevel, ChunkAccess chunk, BlockPos pos) {
		this.level = serverLevel;
		this.chunk = chunk;
		this.pos = pos;
		BlockState state = serverLevel.getBlockState(pos);
		this.block = state.getBlock();
		if (state.hasBlockEntity()) {
			BlockEntity blockEntity = chunk.getBlockEntity(pos);
            tag = blockEntity != null ? blockEntity.collectComponents() : DataComponentMap.EMPTY;
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
