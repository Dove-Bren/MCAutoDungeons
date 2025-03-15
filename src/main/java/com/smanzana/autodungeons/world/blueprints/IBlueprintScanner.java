package com.smanzana.autodungeons.world.blueprints;

import net.minecraft.core.BlockPos;

public interface IBlueprintScanner {
	public BlueprintBlock scan(BlockPos offset, BlueprintBlock block);
}