package com.smanzana.autodungeons.world.blueprints;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;

public class BlueprintSpawnContext {
	
	public final LevelAccessor world;
	public final BlockPos at;
	public final Direction direction;
	public final boolean isWorldGen; // Whether this is currently operating during worldgen (which has implications for TE placing, etc.)
	public final @Nullable BoundingBox bounds;
	public final @Nullable IBlueprintBlockPlacer placer; // Overriding block spawner interface
	
	public BlueprintSpawnContext(LevelAccessor world, BlockPos pos, Direction direction, boolean isWorldGen, @Nullable BoundingBox bounds, @Nullable IBlueprintBlockPlacer placer) {
		this.world = world;
		this.at = pos;
		this.direction = direction;
		this.bounds = bounds;
		this.placer = placer;
		this.isWorldGen = isWorldGen;
	}
	
	public BlueprintSpawnContext(LevelAccessor world, BlockPos pos, Direction direction, boolean isWorldGen, @Nullable BoundingBox bounds) {
		this(world, pos, direction, isWorldGen, bounds, null);
	}
}