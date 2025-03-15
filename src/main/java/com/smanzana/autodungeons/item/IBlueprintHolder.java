package com.smanzana.autodungeons.item;

import javax.annotation.Nonnull;

import com.smanzana.autodungeons.world.blueprints.IBlueprint;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

public interface IBlueprintHolder {

	public boolean hasBlueprint(Player player, ItemStack stack);
	public boolean shouldDisplayBlueprint(Player player, ItemStack stack, BlockPos pos);
	public @Nonnull IBlueprint getBlueprint(Player player, ItemStack stack, BlockPos pos);
	
}
