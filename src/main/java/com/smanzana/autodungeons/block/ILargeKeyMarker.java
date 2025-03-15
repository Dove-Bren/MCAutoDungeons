package com.smanzana.autodungeons.block;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;

/**
 * This block, when in a dungeon-room blueprint, is where a large WorldKey can be placed
 */
public interface ILargeKeyMarker {
	
	public default boolean isLargeKey(BlockState state) { return true; }

	public void setKey(LevelAccessor world, BlockState state, BlockPos pos, WorldKey key, DungeonRoomInstance room, @Nullable BoundingBox bounds);
	
}
