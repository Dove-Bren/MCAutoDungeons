package com.smanzana.autodungeons.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/**
 * This block, when in a dungeon-room blueprint, stands for one possible exit of the room
 */
public interface IExitMarker {
	
	public default boolean isExit(BlockState state) { return true; }

	public Direction getFacing(BlockState state);
	
}
