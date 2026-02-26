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
	public final BlockState blockState;
	private final CompoundTag tag;
	
	public ChunkBlockBacker(Level serverLevel, ChunkAccess chunk, BlockPos pos) {
		this.level = serverLevel;
		this.chunk = chunk;
		this.pos = pos;
		this.blockState = serverLevel.getBlockState(pos);
		if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = chunk.getBlockEntity(pos);
			if (blockEntity != null) {
				CompoundTag savedTag = blockEntity.saveCustomAndMetadata(serverLevel.registryAccess());
				this.tag = savedTag.isEmpty() ? null : savedTag;
			} else {
				this.tag = null;
			}
		} else {
			this.tag = null;
		}
	}
	
	@Override
	public void back() {
		level.setBlock(pos, blockState, 4);
		if (tag != null) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntity.loadWithComponents(tag, level.registryAccess());
            }
        }
	}

}
