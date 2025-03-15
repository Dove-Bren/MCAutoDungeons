package com.smanzana.autodungeons.block;

import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Like HorizontalBlock but an interface
 * @author Skyler
 *
 */
public interface IDirectionalBlock {
	
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

}
