package com.smanzana.autodungeons.world.blueprints;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class BlueprintLocation {
	private final Direction facing;
	private final BlockPos pos;
	
	public BlueprintLocation(BlockPos pos, Direction facing) {
		this.pos = pos;
		this.facing = facing;
	}

	public Direction getFacing() {
		return facing;
	}

	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public String toString() {
		return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")[" + facing.name() + "]";
	}
	
	private static final String NBT_POS = "pos";
	private static final String NBT_DIR = "facing";
	
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		tag.put(NBT_POS, NbtUtils.writeBlockPos(pos));
		tag.putByte(NBT_DIR, (byte) facing.get2DDataValue());
		return tag;
	}
	
	public static BlueprintLocation fromNBT(CompoundTag nbt) {
		final BlockPos pos;
		
		pos = NbtUtils.readBlockPos(nbt.getCompound(NBT_POS));
		
		Direction facing = Direction.from2DDataValue(nbt.getByte(NBT_DIR));
		return new BlueprintLocation(pos, facing);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BlueprintLocation) {
			BlueprintLocation other = (BlueprintLocation) o;
			return other.facing == this.facing && other.pos.equals(this.pos);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.facing.hashCode() * 91 + this.pos.hashCode();
	}
}