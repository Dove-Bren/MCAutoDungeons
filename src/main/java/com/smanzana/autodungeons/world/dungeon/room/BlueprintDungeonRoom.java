package com.smanzana.autodungeons.world.dungeon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.block.IEntryMarker;
import com.smanzana.autodungeons.block.IExitMarker;
import com.smanzana.autodungeons.block.ILargeDoorMarker;
import com.smanzana.autodungeons.block.ILargeKeyMarker;
import com.smanzana.autodungeons.tile.IUniqueBlueprintTileEntity;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintBlock;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.blueprints.BlueprintSpawnContext;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.autodungeons.world.blueprints.IBlueprintBlockPlacer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

/**
 * Room where the room structure is a blueprint.
 * @author Skyler
 *
 */
public class BlueprintDungeonRoom implements IDungeonRoom, IDungeonLobbyRoom {
	
	protected static class BlueprintDungeonRoomPlacer implements IBlueprintBlockPlacer {

		private final UUID dungeonID;
		private final UUID roomID;
		
		public BlueprintDungeonRoomPlacer(UUID dungeonID, UUID roomID) {
			this.dungeonID = dungeonID;
			this.roomID = roomID;
		}

		@Override
		public boolean spawnBlock(BlueprintSpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block) {
			return false; // do regular BP spawning
		}
		
		@Override
		public void finalizeBlock(BlueprintSpawnContext context, BlockPos pos, BlockState placedState, @Nullable TileEntity te, Direction direction, BlueprintBlock block) {
			if (te != null) {
				if (te instanceof IUniqueBlueprintTileEntity) {
					((IUniqueBlueprintTileEntity) te).onRoomBlueprintSpawn(dungeonID, roomID, context.isWorldGen);
				}
			}
		}
	}
	
	private final ResourceLocation id;
	private final IBlueprint blueprint;
	private final Set<BlueprintLocation> doors;
	private final List<BlueprintLocation> largeKeySpots;
	private @Nullable BlueprintLocation largeKeyDoor;
	private final List<BlueprintLocation> chestsRelative;
	
	public BlueprintDungeonRoom(ResourceLocation id, Blueprint blueprint) {
		this.id = id;
		this.blueprint = blueprint;
		this.doors = new HashSet<>();
		this.largeKeySpots = new ArrayList<>();
		this.largeKeyDoor = null;
		this.chestsRelative = new ArrayList<>();
		
		blueprint.scanBlocks(this::parseRoom);
	}
	
	public static boolean IsDoorIndicator(BlockState state) {
		return state != null
				&& state.getBlock() instanceof IExitMarker
				&& ((IExitMarker) state.getBlock()).isExit(state);
	}
	
	public static boolean IsEntry(BlockState state) {
		return state != null
				&& state.getBlock() instanceof IEntryMarker
				&& ((IEntryMarker) state.getBlock()).isEntry(state);
	}
	
	public static boolean IsLargeKeySpot(BlockState state) {
		return state != null
				&& state.getBlock() instanceof ILargeKeyMarker
				&& ((ILargeKeyMarker) state.getBlock()).isLargeKey(state);
	}
	
	public static boolean IsLargeKeyDoor(BlockState state) {
		return state != null
				&& state.getBlock() instanceof ILargeDoorMarker
				&& ((ILargeDoorMarker) state.getBlock()).isLargeDoor(state);
	}
	
	public static boolean IsChest(BlockState state) {
		return state != null && state.getBlock() == Blocks.CHEST;
	}
	
	private static boolean debugConnections = false;

	protected BlueprintBlock parseRoom(BlockPos offset, BlueprintBlock block) {
		if (IsChest(block.getState())) {
			chestsRelative.add(new BlueprintLocation(offset, block.getFacing()));
		} else if (IsDoorIndicator(block.getState())) {
			doors.add(new BlueprintLocation(offset, block.getFacing().getOpposite()));
			if (!debugConnections) {
				block = BlueprintBlock.Air; // Make block an air one
			}
		} else if (IsEntry(block.getState())) {
			if (!debugConnections && offset.equals(BlockPos.ZERO)) {
				// Clear out any comparator that's there from capturing still
				block = BlueprintBlock.Air;
			}
		} else if (IsLargeKeySpot(block.getState())) {
			largeKeySpots.add(new BlueprintLocation(offset, block.getFacing()));
			if (!debugConnections) {
				block = BlueprintBlock.Air; // Make block into an air one. Could make it a chest...
			}
		} else if (IsLargeKeyDoor(block.getState())) {
			if (this.largeKeyDoor != null) {
				AutoDungeons.LOGGER.error("Found multiple large dungeon doors in room while parsing blueprint!");
			}
			this.largeKeyDoor = new BlueprintLocation(offset, block.getFacing());
		}
		
		return block;
	}
	
	protected IBlueprint getBlueprint() {
		return blueprint;
	}
	
	@Override
	public boolean canSpawnAt(IWorld world, BlueprintLocation start) {
		BlockPos dims = getBlueprint().getAdjustedDimensions(start.getFacing());
		BlockPos offset = getBlueprint().getAdjustedOffset(start.getFacing());
		
		int minX = start.getPos().getX() - offset.getX();
		int minY = start.getPos().getY() - offset.getY();
		int minZ = start.getPos().getZ() - offset.getZ();
		int maxX = minX + dims.getX();
		int maxY = minY + dims.getY();
		int maxZ = minZ + dims.getZ();
		for (int i = minX; i <= maxX; i++)
		for (int j = minY; j <= maxY; j++)
		for (int k = minZ; k <= maxZ; k++) {
			BlockPos pos = new BlockPos(i, j, k);
			BlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getDestroySpeed(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(IWorld world, BlueprintLocation start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		final BlueprintDungeonRoomPlacer placer = new BlueprintDungeonRoomPlacer(UUID.randomUUID(), dungeonID);
		getBlueprint().spawn(world, start.getPos(), start.getFacing(), bounds, placer);
	}

	@Override
	public int getNumExits() {
		return doors.size();
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		Collection<BlueprintLocation> exits = doors;
		
		// Dungeon notion of direction is backwards to blueprints:
		// Dungeon wants facing to be you looking back through the door
		// Blueprint wants your facing as you go in the door. That's there the 'opposite' comes from.
		
		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
		//final Direction modDir = IBlueprint.GetModDir(blueprint.getEntry().getFacing(), start.getFacing());
		// Door offset and final rotation is what's in exits rotated modDir times
		
		List<BlueprintLocation> ret;
		if (exits != null) {
			ret = new ArrayList<>(exits.size());
			for (BlueprintLocation door : exits) {
				ret.add(BlueprintToRoom(door, getBlueprint().getEntry(), start));
			}
		} else {
			ret = new LinkedList<>();
		}
		return ret;
	}

	@Override
	public int getDifficulty() {
		return 1;
	}

	@Override
	public boolean supportsDoor() {
		return largeKeyDoor != null;
	}
	
	@Override
	public BlueprintLocation getDoorLocation(BlueprintLocation start) {
		return BlueprintToRoom(largeKeyDoor, blueprint.getEntry(), start);
	}

	@Override
	public boolean supportsKey() {
		return !largeKeySpots.isEmpty();
	}

	@Override
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		BlueprintLocation orig = largeKeySpots.get(0);
		return BlueprintToRoom(orig, blueprint.getEntry(), start);
	}
	
	@Override
	public boolean supportsTreasure() {
		return !chestsRelative.isEmpty();
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
//		// See note about dungeon vs blueprint facing in @getExits
		List<BlueprintLocation> ret = new ArrayList<>();
		for (BlueprintLocation orig : chestsRelative) {
			ret.add(BlueprintToRoom(orig, blueprint.getEntry(), start));
		}
		return ret;
	}
	
	@Override
	public MutableBoundingBox getBounds(BlueprintLocation entry) {
		final IBlueprint blueprint = getBlueprint();
		BlockPos dims = blueprint.getAdjustedDimensions(entry.getFacing());
		BlockPos offset = blueprint.getAdjustedOffset(entry.getFacing());
		
		int minX = entry.getPos().getX() - offset.getX();
		int minY = entry.getPos().getY() - offset.getY();
		int minZ = entry.getPos().getZ() - offset.getZ();
		int maxX = minX + (dims.getX()-(int) Math.signum(dims.getX()));
		int maxY = minY + (dims.getY()-(int) Math.signum(dims.getY()));
		int maxZ = minZ + (dims.getZ()-(int) Math.signum(dims.getZ()));
		
		// Have to figure out real min/max ourselves
		return new MutableBoundingBox(
				Math.min(minX, maxX),
				Math.min(minY, maxY),
				Math.min(minZ, maxZ),
				Math.max(maxX, minX),
				Math.max(maxY, minY),
				Math.max(maxZ, minZ));
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return true;
	}
	
	@Override
	public ResourceLocation getRoomID() {
		return id;
	}
	
	protected static final BlueprintLocation BlueprintToRoom(BlueprintLocation blueprintPoint, BlueprintLocation blueprintEntry, BlueprintLocation start) {
		BlueprintLocation orig = blueprintPoint;
		
		// Dungeon notion of direction is backwards to blueprints:
		// Dungeon wants facing to be you looking back through the door
		// Blueprint wants your facing as you go in the door. That's there the 'opposite' comes from.
		
		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
		final Direction modDir = IBlueprint.GetModDir(blueprintEntry.getFacing(), start.getFacing());
		// Door offset and final rotation is what's in exits rotated modDir times
				
		Direction doorDir = orig.getFacing();
		int times = (modDir.get2DDataValue() + 2) % 4;
		while (times-- > 0) {
			doorDir = doorDir.getClockWise();
		}
		final BlueprintLocation fromEntry = new BlueprintLocation(
				IBlueprint.ApplyRotation(orig.getPos(), modDir),
				doorDir
				);
		return new BlueprintLocation(start.getPos().offset(fromEntry.getPos()), fromEntry.getFacing()); 
	}

	// This is a bit hacky, but all loaded rooms can be 'lobby' rooms in that the entry should be where the stairs go.
	@Override
	public Vector3i getStairOffset() {
		return Vector3i.ZERO;
	}
}
