package com.smanzana.autodungeons.world.blueprints;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public interface IBlueprintBlockPlacer {
	
	/**
	 * Possible place a blueprint block manually instead of using built-in blueprint placing.
	 * @param context
	 * @param pos
	 * @param direction
	 * @param block
	 * @return whether the block has been handled manually. If false, blueprint placement happens like normal.
	 */
	public boolean spawnBlock(BlueprintSpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block);
	
	/**
	 * Called after block placement has happened (regardless of whether spawnBlock returned true or false)
	 * @param context
	 * @param pos
	 * @param placedState
	 * @param te 
	 * @param direction
	 * @param block
	 */
	public void finalizeBlock(BlueprintSpawnContext context, BlockPos pos, BlockState placedState, @Nullable BlockEntity te, Direction direction, BlueprintBlock block);
}