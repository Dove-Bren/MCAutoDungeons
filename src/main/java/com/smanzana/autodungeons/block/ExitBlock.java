package com.smanzana.autodungeons.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class ExitBlock extends DirectionalPlaceholderBlock implements IExitMarker {
	
	private static final VoxelShape AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2D, 16.0D);

	@Override
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return AABB;
	}

}
