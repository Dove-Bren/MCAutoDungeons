package com.smanzana.autodungeons.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;

public abstract class DirectionalPlaceholderBlock extends HorizontalBlock {

	public static DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public DirectionalPlaceholderBlock() {
		super(Block.Properties.create(Material.ICE)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.notSolid()
				);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(FACING);
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

}
