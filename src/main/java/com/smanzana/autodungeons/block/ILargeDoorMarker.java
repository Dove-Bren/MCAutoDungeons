package com.smanzana.autodungeons.block;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

/**
 * This block, when in a dungeon-room blueprint, is where a large WorldKey door can be placed
 */
public interface ILargeDoorMarker {
	
	public default boolean isLargeDoor(BlockState state) { return true; }

	public void setKey(IWorld world, BlockState state, BlockPos pos, WorldKey key, DungeonRoomInstance room, @Nullable MutableBoundingBox bounds);
	
}
