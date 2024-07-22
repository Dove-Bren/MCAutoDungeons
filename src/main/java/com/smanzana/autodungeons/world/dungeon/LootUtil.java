package com.smanzana.autodungeons.world.dungeon;

import java.util.Random;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public final class LootUtil {
	
	public static final Random rand = new Random();

	public static final void generateLoot(IWorld world, BlockPos pos, Direction facing, ResourceLocation loottable) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing), 2);
		
		ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
		
		if (chest == null) {
			world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState(), 2);
		} else {
			chest.setLootTable(loottable, rand.nextLong());
		}
	}
	
	/**
	 * Sets a block to be a chest with the given loot inside of it.
	 * loot should be an array exactly 27 long. Less is ok but more is ignored
	 * @param world
	 * @param pos
	 * @param facing
	 * @param loot
	 */
	public static final void createLoot(IWorld world, BlockPos pos, Direction facing,
			NonNullList<ItemStack> loot) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing), 2); // 2 here assumes world is generating and block updates shouldn't happen
		
		ChestTileEntity chest = (ChestTileEntity) world.getTileEntity(pos);
		int len = Math.min(27, loot.size());
		for (int i = 0; i < len; i++) {
			chest.setInventorySlotContents(i, loot.get(i));
		}
	}
	
}
