package com.smanzana.autodungeons.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

/**
 * This block, when in a dungeon-room blueprint, stands for the entry of the room.
 */
public interface IEntryMarker {

	public Direction getFacing(BlockState state);
	
}
