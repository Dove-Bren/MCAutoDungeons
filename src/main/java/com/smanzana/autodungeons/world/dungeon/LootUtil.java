package com.smanzana.autodungeons.world.dungeon;

import java.util.Random;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public final class LootUtil {
	
	public static final Random rand = new Random();

	public static final void generateLoot(LevelAccessor world, BlockPos pos, Direction facing, ResourceLocation loottable) {
		world.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
		
		ChestBlockEntity chest = (ChestBlockEntity) world.getBlockEntity(pos);
		
		if (chest == null) {
			world.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
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
	public static final void createLoot(LevelAccessor world, BlockPos pos, Direction facing,
			NonNullList<ItemStack> loot) {
		world.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2); // 2 here assumes world is generating and block updates shouldn't happen
		
		ChestBlockEntity chest = (ChestBlockEntity) world.getBlockEntity(pos);
		int len = Math.min(27, loot.size());
		for (int i = 0; i < len; i++) {
			chest.setItem(i, loot.get(i));
		}
	}
	
}
