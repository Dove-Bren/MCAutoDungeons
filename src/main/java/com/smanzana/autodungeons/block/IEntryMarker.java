package com.smanzana.autodungeons.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/**
 * This block, when in a dungeon-room blueprint, stands for the entry of the room.
 */
public interface IEntryMarker {
	
	public default boolean isEntry(BlockState state) { return true; }

	public Direction getFacing(BlockState state);
	
}
