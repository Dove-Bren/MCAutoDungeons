package com.smanzana.autodungeons.world.blueprints;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;

public interface IBlueprint {

	public default void spawn(LevelAccessor world, BlockPos at) {
		this.spawn(world, at, Direction.NORTH);
	}
	
	public default void spawn(LevelAccessor world, BlockPos at, Direction direction) {
		this.spawn(world, at, direction, (BoundingBox) null, null);
	}
	
	public void spawn(LevelAccessor world, BlockPos at, Direction direction, @Nullable BoundingBox bounds, @Nullable IBlueprintBlockPlacer spawner);
	
	public BlueprintLocation getEntry();

	/**
	 * Returns a preview of the blueprint centered around the blueprint entry point.
	 * Note that the preview is un-rotated. You must rotate yourself if you want that.
	 * Note that air blocks and blocks outside the template are null in the arrays.
	 * @return
	 */
	public BlueprintBlock[][][] getPreview();
	
	public void scanBlocks(IBlueprintScanner scanner);

	public BlockPos getDimensions();
	
	/**
	 * Returns total space dimensions of the blueprint, if it were rotated to the desired facing.
	 * @param facing The desired facing for the entry way, if there is one.
	 * @return
	 */
	public default BlockPos getAdjustedDimensions(Direction facing) {
		final BlockPos dimensions = this.getDimensions();
		final BlueprintLocation entry = this.getEntry();
		
		Direction mod = GetModDir(entry == null ? Direction.NORTH : entry.getFacing(), facing);
		int width = dimensions.getX();
		int height = dimensions.getY();
		int length = dimensions.getZ();
		
		switch (mod) {
		case DOWN:
		case UP:
		case NORTH:
			break;
		case EAST:
			// Single rotation: (-z, x)
			width = -dimensions.getZ();
			length = dimensions.getX();
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			width = -dimensions.getX();
			length = -dimensions.getZ();
			break;
		case WEST:
			// Triple: (z, -x)
			width = dimensions.getZ();
			length = -dimensions.getX();
			break;
		}
		
		return new BlockPos(width, height, length);
	}
	
	/**
	 * Returns entry point offsets when blueprint is rotated to the desired facing.
	 * @param facing
	 * @return
	 */
	public default BlockPos getAdjustedOffset(Direction facing) {
		final BlueprintLocation entry = this.getEntry();
		BlockPos offset = entry == null ? new BlockPos(0,0,0) : entry.getPos().immutable();
		Direction mod = IBlueprint.GetModDir(entry == null ? Direction.NORTH : entry.getFacing(), facing);
		
		int x = offset.getX();
		int y = offset.getY();
		int z = offset.getZ();
		
		switch (mod) {
		case DOWN:
		case UP:
		case NORTH:
			break;
		case EAST:
			// Single rotation: (-z, x)
			x = -offset.getZ();
			z = offset.getX();
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			x = -offset.getX();
			z = -offset.getZ();
			break;
		case WEST:
			// Triple: (z, -x)
			x = offset.getZ();
			z = -offset.getX();
			break;
		}
		
		return new BlockPos(x, y, z);
	}
	
	static BlockPos ApplyRotation(BlockPos input, Direction modDir) {
		int x = 0;
		int z = 0;
		final int dx = input.getX();
		final int dz = input.getZ();
		switch (modDir) {
		case DOWN:
		case UP:
		case NORTH:
			 x = dx;
			 z = dz;
			break;
		case EAST:
			// Single rotation: (-z, x)
			x = -dz;
			z = dx;
			break;
		case SOUTH:
			// Double rotation: (-x, -z)
			x = -dx;
			z = -dz;
			break;
		case WEST:
			// Triple: (z, -x)
			x = dz;
			z = -dx;
			break;
		}
		return new BlockPos(x, input.getY(), z);
	}

	public static Direction GetModDir(Direction original, Direction newFacing) {
		Direction out = Direction.NORTH;
		int rotCount = (4 + newFacing.get2DDataValue() - original.get2DDataValue()) % 4;
		while (rotCount-- > 0) {
			out = out.getClockWise();
		}
		return out;
	}
	
}
