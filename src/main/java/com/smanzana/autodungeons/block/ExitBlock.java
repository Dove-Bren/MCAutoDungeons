package com.smanzana.autodungeons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public class ExitBlock extends DirectionalPlaceholderBlock implements IExitMarker {
	
	private static final VoxelShape AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2D, 16.0D);

	@Override
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return AABB;
	}

}
