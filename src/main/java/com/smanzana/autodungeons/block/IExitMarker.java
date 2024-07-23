package com.smanzana.autodungeons.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

/**
 * This block, when in a dungeon-room blueprint, stands for one possible exit of the room
 */
public interface IExitMarker {

	public Direction getFacing(BlockState state);
	
}
