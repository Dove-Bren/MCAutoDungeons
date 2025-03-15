package com.smanzana.autodungeons.world.dungeon;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import com.smanzana.autodungeons.util.NetUtils;
import com.smanzana.autodungeons.world.WorldKey;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;

public class DungeonInstance {
	private final ResourceLocation dungeonID;
	private final UUID instanceID;
	private final WorldKey smallKey;
	private final WorldKey largeKey;
	
	private static final String NBT_DUNGEON_ID = "dungeonID";
	private static final String NBT_INSTANCE_ID = "instanceID";
	private static final String NBT_SMALL_KEY = "smallKey";
	private static final String NBT_LARGE_KEY = "largeKey";
	
	public DungeonInstance(ResourceLocation dungeonID, UUID instanceID, WorldKey smallKey, WorldKey largeKey) {
		this.dungeonID = dungeonID;
		this.instanceID = instanceID;
		this.smallKey = smallKey;
		this.largeKey = largeKey;
	}
	
	protected DungeonInstance(Dungeon dungeon, UUID instanceID, UUID keyBaseID) {
		this(dungeon.getRegistryName(), instanceID,
				new WorldKey(instanceID),
				new WorldKey(NetUtils.CombineUUIDs(instanceID, keyBaseID)));
	}
	
	protected DungeonInstance(Dungeon dungeon, UUID instanceID, Random rand) {
		this(dungeon, instanceID, NetUtils.CombineUUIDs(instanceID, NetUtils.RandomUUID(rand)));
	}
	
	public ResourceLocation getDungeonID() {
		return this.dungeonID;
	}
	
	public UUID getInstanceID() {
		return this.instanceID;
	}
	
	public WorldKey getSmallKey() {
		return smallKey;
	}

	public WorldKey getLargeKey() {
		return largeKey;
	}

	public static DungeonInstance Random(Dungeon dungeon) {
		return new DungeonInstance(dungeon, UUID.randomUUID(), UUID.randomUUID());
	}
	
	public static DungeonInstance Random(Dungeon dungeon, Random rand) {
		return new DungeonInstance(dungeon, NetUtils.RandomUUID(rand), rand);
	}
	
	public INBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putString(NBT_DUNGEON_ID, this.dungeonID.toString());
		tag.putUUID(NBT_INSTANCE_ID, instanceID);
		tag.put(NBT_SMALL_KEY, this.smallKey.asNBT());
		tag.put(NBT_LARGE_KEY, this.largeKey.asNBT());
		return tag;
	}
	
	public static DungeonInstance FromNBT(INBT nbt) {
		CompoundNBT tag = (CompoundNBT) nbt;
		ResourceLocation loc = new ResourceLocation(tag.getString(NBT_DUNGEON_ID));
		UUID id = tag.getUUID(NBT_INSTANCE_ID);
		WorldKey smallKey = WorldKey.fromNBT(tag.getCompound(NBT_SMALL_KEY));
		WorldKey largeKey = WorldKey.fromNBT(tag.getCompound(NBT_LARGE_KEY));
		return new DungeonInstance(loc, id, smallKey, largeKey);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(dungeonID, instanceID, smallKey, largeKey);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DungeonInstance) {
			DungeonInstance other = (DungeonInstance) o;
			return other.dungeonID.equals(this.dungeonID)
					&& other.instanceID.equals(this.instanceID)
					&& other.smallKey.equals(this.smallKey)
					&& other.largeKey.equals(this.largeKey);
		}
		return false;
	}
}