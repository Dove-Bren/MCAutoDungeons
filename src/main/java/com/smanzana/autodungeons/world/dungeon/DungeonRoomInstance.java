package com.smanzana.autodungeons.world.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.util.NetUtils;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DungeonRoomInstance {
	private final BlueprintLocation entry;
	private final IDungeonRoom template;
	private final boolean hasLargeKey; // whether the key should be in this room
	private final boolean hasLargeDoor; // Whether a large key door is in this room and should et stamped to be dungeon key
	private final DungeonInstance dungeonInstance;
	private final UUID roomID;
	private final Dungeon dungeonTemplate;
	
	// Puzzle mechanics that can be turned on after construction
	private final List<BlueprintLocation> smallDoors; // What (if any) exits should have small doors
	private boolean hasSmallKey;
	
	public DungeonRoomInstance(BlueprintLocation entry, IDungeonRoom template, boolean hasKey, boolean hasLargeDoor, DungeonInstance dungeonInstance, @Nonnull UUID roomID) {
		this.entry = entry;
		this.template = template;
		this.hasLargeKey = hasKey;
		this.hasLargeDoor = hasLargeDoor;
		this.dungeonInstance = dungeonInstance;
		this.roomID = roomID;
		this.smallDoors = new ArrayList<>(1);
		this.hasSmallKey = false;
		
		this.dungeonTemplate = DungeonRegistry.Get(dungeonInstance.getDungeonID());
	}
	
	protected void addSmallDoor(BlueprintLocation exit) {
		smallDoors.add(exit);
	}
	
	protected void addSmallKey() {
		this.hasSmallKey = true;
	}

	public BoundingBox getBounds() {
		if (this.template == null) {
			System.out.println("null");
		}
		return template.getBounds(this.entry);
	}
	
	public UUID getRoomID() {
		return roomID;
	}
	
	public DungeonInstance getDungeonInstance() {
		return this.dungeonInstance;
	}
	
	public BlueprintLocation getEntry() {
		return this.entry;
	}
	
	public IDungeonRoom getRoomTemplate() {
		return this.template;
	}
	
	public void spawn(LevelAccessor world) {
		spawn(world, null);
	}
	
	public void spawn(LevelAccessor world, BoundingBox bounds) {
		// Spawn room template
		template.spawn(world, this.entry, bounds, this.roomID);
		
		// If we have a key, do special key placement
		if (this.hasLargeKey) {
			BlueprintLocation keyLoc = template.getKeyLocation(this.entry);
			if (bounds == null || bounds.isInside(keyLoc.getPos())) {
				dungeonTemplate.spawnLargeKey(this, world, keyLoc);
			}
		}
		if (this.hasLargeDoor) {
			BlueprintLocation doorLoc = template.getDoorLocation(this.entry);
			if (bounds == null || bounds.isInside(doorLoc.getPos())) {
				dungeonTemplate.spawnLargeDoor(this, world, doorLoc);
			}
		}
		if (this.hasSmallKey) {
			if (!this.template.supportsTreasure()) {
				AutoDungeons.LOGGER.fatal("Room is meant to have a small key, but has no treasure locations");
			} else {
				// pick small key location based on something deterministic so that it'll be the same
				// even if we can't spawn it in this call
				Random rand = new Random(this.roomID.getLeastSignificantBits() ^ this.roomID.getMostSignificantBits());
				List<BlueprintLocation> treasureSpots = this.template.getTreasureLocations(this.entry);
				BlueprintLocation spot = treasureSpots.get((int) (rand.nextFloat() * treasureSpots.size()));
				if (bounds == null || bounds.isInside(spot.getPos())) {
					dungeonTemplate.spawnSmallKey(this, world, spot);
				}
			}
		}
		for (BlueprintLocation smallDoor : this.smallDoors) {
			if (bounds == null || bounds.isInside(smallDoor.getPos())) {
				dungeonTemplate.spawnSmallDoor(this, world, smallDoor, bounds);
			}
		}

		if (this.template.supportsTreasure()) {
			for (BlueprintLocation lootSpot : this.template.getTreasureLocations(this.entry)) {
				if (bounds != null && !bounds.isInside(lootSpot.getPos())) {
					continue; // Will come back for you later <3
				}
				
				if (world.getBlockState(lootSpot.getPos()).getBlock() instanceof ChestBlock) {
					LootUtil.generateLoot(world, lootSpot.getPos(), lootSpot.getFacing(), this.dungeonTemplate.getLootTableForRoom(this));
				}
			}
		}
	}

	@Override
	public String toString() {
		return "[" + this.entry.getPos() + "] " + this.template.getRoomID() + ": " + this.getBounds();
	}
	
	private static final String NBT_ENTRY = "entry";
	private static final String NBT_TEMPLATE = "template";
	private static final String NBT_HASKEY = "hasKey";
	private static final String NBT_HASDOOR = "hasLargeDoor";
	private static final String NBT_DUNGEON_INSTANCE = "dungeonInstance";
	private static final String NBT_ROOM_ID = "roomID";
	private static final String NBT_HASSMALLKEY = "hasSmallKey";
	private static final String NBT_SMALL_DOORS = "smallDoors";
	
	public @Nonnull CompoundTag toNBT(@Nullable CompoundTag tag) {
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		tag.put(NBT_ENTRY, this.entry.toNBT());
		tag.putString(NBT_TEMPLATE, this.template.getRoomID().toString());
		tag.putBoolean(NBT_HASKEY, this.hasLargeKey);
		tag.put(NBT_DUNGEON_INSTANCE, this.dungeonInstance.toNBT());
		tag.putUUID(NBT_ROOM_ID, roomID);
		tag.putBoolean(NBT_HASSMALLKEY, this.hasSmallKey);
		tag.putBoolean(NBT_HASDOOR, this.hasLargeDoor);
		tag.put(NBT_SMALL_DOORS, NetUtils.ToNBT(this.smallDoors, e -> e.toNBT()));
		
		return tag;
	}
	
	public static DungeonRoomInstance fromNBT(CompoundTag tag) {
		final BlueprintLocation entry = BlueprintLocation.fromNBT(tag.getCompound(NBT_ENTRY));
		final ResourceLocation templateID = new ResourceLocation(tag.getString(NBT_TEMPLATE));
		final DungeonRoomRecord record = DungeonRoomRegistry.GetInstance().getRegisteredRoom(templateID);
		final boolean hasKey = tag.getBoolean(NBT_HASKEY);
		final boolean hasLargeDoor = tag.getBoolean(NBT_HASDOOR);
		final DungeonInstance instance = DungeonInstance.FromNBT(tag.get(NBT_DUNGEON_INSTANCE));
		final UUID roomID = tag.getUUID(NBT_ROOM_ID);
		
		if (record == null) {
			AutoDungeons.LOGGER.error("Failed to find dungeon room instance by id " + templateID);
		}
		
		final DungeonRoomInstance ret = new DungeonRoomInstance(entry, record.room, hasKey, hasLargeDoor, instance, roomID);
		
		ret.hasSmallKey = tag.getBoolean(NBT_HASSMALLKEY);
		ret.smallDoors.clear();
		if (tag.contains(NBT_SMALL_DOORS, Tag.TAG_LIST)) { // mostly just legacy support?
			NetUtils.FromNBT(ret.smallDoors, (ListTag) tag.get(NBT_SMALL_DOORS), nbt -> BlueprintLocation.fromNBT((CompoundTag) nbt));
		}
		
		return ret;
	}
}