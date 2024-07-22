package com.smanzana.autodungeons.world;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.smanzana.autodungeons.util.NetUtils;

import net.minecraft.nbt.CompoundNBT;

public class WorldKey {
	
	private static final String NBT_ID = "key_id";
	;//private static final String NBT_COLOR = "color";
	
	private final UUID id;
	;//private final int color;
	
	public WorldKey(@Nonnull UUID id) {;//, int colorARGB) {
		this.id = id;
		;//this.color = colorARGB;
	}
	
	public WorldKey() {
		this(UUID.randomUUID());
	}
	
	/**
	 * Takes another UUID and creates a new, unique key based on this key and the
	 * other ID passed in.
	 * This is intended to be deterministic such that two WorldKeys with the same underlying
	 * ID can be mutated with the same second id and produce equal new keys.
	 * @param id
	 * @return
	 */
	public WorldKey mutateWithID(UUID id) {
		return new WorldKey(NetUtils.CombineUUIDs(this.id, id));
	}
	
	public WorldKey mutateWithKey(WorldKey other) {
		return mutateWithID(other.id);
	}
	
	public CompoundNBT asNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putUniqueId(NBT_ID, id);
		;//nbt.putInt(NBT_COLOR, color);
		return nbt;
	}
	
	public static WorldKey fromNBT(CompoundNBT nbt) {
		UUID id = nbt.getUniqueId(NBT_ID);
		;//int color = nbt.getInt(NBT_COLOR);
		return new WorldKey(id);//, color);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof WorldKey) {
			WorldKey other = (WorldKey) o;
			if (other.id.equals(this.id)) {;// && other.color == this.color) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();// * 37 + Integer.hashCode(color);
	}
	
	@Override
	public String toString() {
		return this.id.toString();// + " - " + this.color;
	}
}