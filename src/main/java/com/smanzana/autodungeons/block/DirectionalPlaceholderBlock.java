package com.smanzana.autodungeons.block;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class DirectionalPlaceholderBlock extends HorizontalDirectionalBlock {

	public static DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public DirectionalPlaceholderBlock() {
		super(Block.Properties.of(Material.ICE)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.noOcclusion()
				);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

}
