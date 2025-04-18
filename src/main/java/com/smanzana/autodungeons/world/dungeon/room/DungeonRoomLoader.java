package com.smanzana.autodungeons.world.dungeon.room;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.util.AutoReloadListener;
import com.smanzana.autodungeons.util.NBTReloadListener;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.Blueprint.INBTGenerator;
import com.smanzana.autodungeons.world.blueprints.Blueprint.LoadContext;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRegisterEvent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;

public class DungeonRoomLoader {
	
	private static DungeonRoomLoader instance = null;
	public static DungeonRoomLoader instance() {
		if (instance == null) {
			instance = new DungeonRoomLoader();
		}
		
		return instance;
	}
	
	private static final String ROOM_ROOT_ID = "root";
	private static final String ROOM_ROOT_NAME = ROOM_ROOT_ID + ".gat";
	private static final String ROOM_COMPRESSED_EXT = "gat";
	
	private final List<DungeonRoomEntry> loadedRooms;
	
	private DungeonRoomLoader() {
		
		this.roomSaveFolder = new File("./DungeonData/room_blueprint_captures/");
		this.roomLoadFolder = new File("./DungeonData/room_blueprint_captures/");
		this.loadedRooms = new ArrayList<>();
		
		MinecraftForge.EVENT_BUS.addListener(this::onRoomRegistration);
	}
	
	private static final class DungeonRoomEntry {
		public Blueprint blueprint;
		public final ResourceLocation id;
		public final String name;
		public final List<String> tags;
		public final int weight;
		public final int cost;
		
		public DungeonRoomEntry(Blueprint blueprint, ResourceLocation id, String name, List<String> tags, int weight, int cost) {
			this.blueprint = blueprint;
			this.id = id;
			this.name = name;
			this.tags = tags;
			this.weight = weight;
			this.cost = cost;
		}
	}
	
	private static final String NBT_BLUEPRINT = "blueprint";
	private static final String NBT_TAGS = "tags";
	private static final String NBT_WEIGHT = "weight";
	private static final String NBT_NAME = "name";
	private static final String NBT_COST = "cost";
	private static final String NBT_ID = "id";
	
	protected static final CompoundTag toNBT(CompoundTag blueprintTag, @Nullable ResourceLocation ID, String name, int weight, int cost, List<String> tags) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString(NBT_NAME, name);
		nbt.putInt(NBT_WEIGHT, weight);
		nbt.putInt(NBT_COST, cost);
		nbt.put(NBT_BLUEPRINT, blueprintTag);
		if (ID != null) {
			nbt.putString(NBT_ID, ID.toString());
		}
		
		ListTag list = new ListTag();
		for (String tag : tags) {
			list.add(StringTag.valueOf(tag));
		}
		nbt.put(NBT_TAGS, list);
		
		return nbt;
	}
	
	private final Blueprint loadBlueprintFromNBT(LoadContext context, CompoundTag nbt) {
		// Get and stash name for loading debug
		String name = nbt.getString(NBT_NAME);
		context.name = name;
		
		Blueprint blueprint = Blueprint.FromNBT(context, nbt.getCompound(NBT_BLUEPRINT));
		return blueprint;
	}
	
	public final DungeonRoomEntry loadEntryFromNBT(LoadContext context, @Nullable ResourceLocation idOverride, CompoundTag nbt) {
		Blueprint blueprint = loadBlueprintFromNBT(context, nbt);
		
		if (blueprint == null) {
			return null;
		}
		
		String name = nbt.getString(NBT_NAME);
		int weight = nbt.getInt(NBT_WEIGHT);
		int cost = nbt.contains(NBT_COST) ? nbt.getInt(NBT_COST) : 1;
		ResourceLocation id = nbt.contains(NBT_ID) ? new ResourceLocation(nbt.getString(NBT_ID)) : null;
		if (idOverride != null) {
			id = idOverride;
		}
		
		// TODO join to master if this is a piece?
		
		List<String> tags = new LinkedList<>();
		ListTag list = nbt.getList(NBT_TAGS, Tag.TAG_STRING);
		
		int tagCount = list.size();
		for (int i = 0; i < tagCount; i++) {
			tags.add(list.getString(i));
		}
		
//		// For version bumping
//		int unusedWarning;
//		if (!context.source.contains("sorcery_dungeon"))
//		{
//			AutoDungeons.LOGGER.debug("Scanning " + context.source);
//			blueprint.scanBlocks((BlockPos offset, BlueprintBlock block) -> {
//				if (block.getState() != null && block.getState().getBlock() == Blocks.COMPARATOR) {
//					BlockState state = BuiltinBlocks.entryBlock.getDefaultState();
//					state = state.with(EntryBlock.FACING, block.getFacing().getOpposite());
//					return BlueprintBlock.getBlueprintBlock(state, null);
//				}
//				
//				if (block.getState() != null && block.getState().getBlock() == Blocks.REPEATER) {
//					BlockState state = BuiltinBlocks.exitBlock.getDefaultState();
//					state = state.with(ExitBlock.FACING, block.getFacing().getOpposite());
//					return BlueprintBlock.getBlueprintBlock(state, null);
//				}
//				
//				return block;
//			});
//			
//			writeRoomAsFile(blueprint, name, weight, cost, tags);
//		}
		
		return new DungeonRoomEntry(blueprint, id, name, tags, weight, cost);
	}
	
	public final File roomLoadFolder;
	public final File roomSaveFolder;
	
	private final boolean writeRoomAsFileInternal(File saveFile, CompoundTag blueprintTag, String name, int weight, int cost, List<String> tags) {
		boolean success = true;
		
		try {
			NbtIo.writeCompressed(toNBT(blueprintTag, null, name, weight, cost, tags), new FileOutputStream(saveFile));
			//CompressedStreamTools.safeWrite(toNBT(blueprintTag, name, weight, tags), saveFile);
		} catch (IOException e) {
			e.printStackTrace();
			
			System.out.println("Failed to write out serialized file " + saveFile.getAbsolutePath());
			AutoDungeons.LOGGER.error("Failed to write room to " + saveFile.getAbsolutePath());
			success = false;
		}
		
		return success;
	}
	
	public final boolean writeRoomAsFile(Blueprint blueprint, String name, int weight, int cost, List<String> tags) {
		return writeRoomAsFile(new DungeonRoomEntry(blueprint, null, name, tags, weight, cost));
	}
	
	protected final boolean writeRoomAsFile(DungeonRoomEntry entry) {
		boolean success = true;
		String path = null;
		
		if (entry.blueprint.shouldSplit()) {
			File baseDir = new File(this.roomSaveFolder, entry.name);
			if (!baseDir.mkdirs()) {
				throw new RuntimeException("Failed to create directories for complex room: " + baseDir.getPath());
			}
			
			INBTGenerator gen = entry.blueprint.toNBTWithBreakdown();
			AutoDungeons.LOGGER.info("Writing complex room " + entry.name + " as " + gen.getTotal() + " pieces");
			
			for (int i = 0; gen.hasNext(); i++) {
				String fileName;
				CompoundTag nbt = gen.next();
				if (i == 0) {
					// Root room has extra info and needs to be identified
					fileName = ROOM_ROOT_NAME;
				} else {
					fileName = entry.name + "_" + i + "." + ROOM_COMPRESSED_EXT;
				}
				
				File outFile = new File(baseDir, fileName);
				success = writeRoomAsFileInternal(outFile, nbt, entry.name, entry.weight, entry.cost, entry.tags);
				path = outFile.getPath();
			}
		} else {
			File outFile = new File(this.roomSaveFolder, entry.name + "." + ROOM_COMPRESSED_EXT);
			success = writeRoomAsFileInternal(outFile,
					entry.blueprint.toNBT(),
					entry.name, entry.weight, entry.cost, entry.tags);
			path = outFile.getPath();
		}
		
		if (success) {
			AutoDungeons.LOGGER.info("Room written to " + path);
		}
		
		return success;
	}

	public void loadServerData(List<CompoundTag> roomData) {
		loadedRooms.clear();
		final LoadContext context = new LoadContext("Server DataPack");
		for (CompoundTag tag : roomData) {
			loadedRooms.add(loadEntryFromNBT(context, null, tag));
		}
		
		DungeonRoomRegistry.GetInstance().reload();
	}
	
	public List<CompoundTag> getServerData() {
		List<CompoundTag> data = new ArrayList<>(loadedRooms.size());
		for (DungeonRoomEntry room : loadedRooms) {
			data.add(toNBT(room.blueprint.toNBTIgnoringSize(), room.id, room.name, room.weight, room.cost, room.tags));
		}
		return data;
	}
	
	public final void onRoomRegistration(DungeonRoomRegisterEvent event) {
		DungeonRoomRegistry registry = event.getRegistry();
		
		// Blueprint Rooms
		for (DungeonRoomEntry entry : this.loadedRooms) {
			registry.register(entry.name, new BlueprintDungeonRoom(entry.id, entry.blueprint), entry.weight, entry.cost, entry.tags);;
		}
	}
	
	private static class RoomReloadListener extends NBTReloadListener {
		
		protected static RoomReloadListener lastInstance = null;
		
		protected final List<DungeonRoomEntry> loadedRooms;
		
		public RoomReloadListener(String folder) {
			super(folder, "gat", true);
			loadedRooms = new ArrayList<>();
			MinecraftForge.EVENT_BUS.register(this);
			
			if (lastInstance != null) {
				MinecraftForge.EVENT_BUS.unregister(lastInstance);
			}
			lastInstance = this;
		}
		
		@Override
		public void apply(Map<ResourceLocation, CompoundTag> data, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
			loadedRooms.clear();
			if (data != null) {
				AutoDungeons.LOGGER.info("Loading room blueprints from {} resources", data.size());
				long start;
				long now;
				final DungeonRoomLoader loader = DungeonRoomLoader.instance();
				
				for (Entry<ResourceLocation, CompoundTag> entry : data.entrySet()) {
					final LoadContext context = new LoadContext(entry.getKey().toString());
					
					start = System.currentTimeMillis();
					loadedRooms.add(loader.loadEntryFromNBT(context, entry.getKey(), entry.getValue()));
					now = System.currentTimeMillis();
					
					if (now - start > 100) {
						AutoDungeons.LOGGER.warn("Took " + (now-start) + "ms to read " + entry.getKey());
					}
				}
			}
		}
	}
	
	private static class RoomCompReloadListener extends AutoReloadListener<Map<ResourceLocation, Map<String, CompoundTag>>> {
		
		private static RoomCompReloadListener lastInstance = null;
		
		protected final List<DungeonRoomEntry> loadedRooms;
		
		public RoomCompReloadListener(String folder) {
			super(folder, "cmp");
			loadedRooms = new ArrayList<>();
			MinecraftForge.EVENT_BUS.register(this);
			
			if (lastInstance != null) {
				MinecraftForge.EVENT_BUS.unregister(lastInstance);
			}
			lastInstance = this;
		}
		
		@Override
		protected Map<ResourceLocation, Map<String, CompoundTag>> prepareResource(Map<ResourceLocation, Map<String, CompoundTag>> builder, ResourceLocation location, InputStream input) throws IOException, IllegalStateException {
			if (builder == null) {
				builder = new HashMap<>();
			}
			
			// Figure out the folder path
			String compPath = getCompPath(location.getPath());
			String subpath;
			if (!compPath.isEmpty()) {
				// Trim location down to comp path, and keep track of subpath
				subpath = location.getPath().substring(compPath.length() + 1);
				location = new ResourceLocation(location.getNamespace(), compPath);
			} else {
				subpath = location.getPath();
				location = new ResourceLocation(location.getNamespace(), "");
			}
			
			// Read NBT
			CompoundTag tag = NbtIo.readCompressed(input);
			
			// Add to map
			CompoundTag existing = builder.computeIfAbsent(location, p -> new HashMap<>())
				.put(subpath, tag);
			
			if (existing != null) {
				throw new IllegalStateException("Duplicate data file ignored with ID " + location + "/" + subpath);
			}
			
			return builder;
		}
		
		@Override
		protected Map<ResourceLocation, Map<String, CompoundTag>> checkPreparedData(Map<ResourceLocation, Map<String, CompoundTag>> data, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
			// Verify each path has a root
			if (data != null) {
				Iterator<Entry<ResourceLocation, Map<String, CompoundTag>>> it = data.entrySet().iterator();
				while (it.hasNext()) {
					Entry<ResourceLocation, Map<String, CompoundTag>> entry = it.next();
					final ResourceLocation compName = entry.getKey();
					final Map<String, CompoundTag> compData = entry.getValue();
					if (!compData.containsKey(ROOM_ROOT_ID)) {
						AutoDungeons.LOGGER.error("Failed to find root for room composition: " + compName);
						it.remove();
					}
				}
			}
			
			return data;
		}
		
		protected void loadComp(ResourceLocation comp, Map<String, CompoundTag> data, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
			long start;
			long now;
			final DungeonRoomLoader loader = DungeonRoomLoader.instance();
			
			// Verification above means we should always have a root. Load that directly as first room
			LoadContext context = new LoadContext(comp.toString(), ROOM_ROOT_ID);
			start = System.currentTimeMillis();
			DungeonRoomEntry root = loader.loadEntryFromNBT(context, comp, data.get(ROOM_ROOT_ID));
			now = System.currentTimeMillis();
			if ((now-start) > 100) {
				AutoDungeons.LOGGER.warn("Took " + (now-start) + "ms to load root for " + comp);
			}
			
			if (root == null) {
				AutoDungeons.LOGGER.error("Failed to load root for composite room " + comp);
			} else {
				for (Entry<String, CompoundTag> compRow : data.entrySet()) {
					if (compRow.getKey().equalsIgnoreCase(ROOM_ROOT_ID)) {
						continue; // handled outside of loop
					}
					
					context = new LoadContext(comp.toString(), compRow.getKey());
					
					start = System.currentTimeMillis();
					Blueprint piece = loader.loadBlueprintFromNBT(context, compRow.getValue());
					now = System.currentTimeMillis();
					
					if (now - start > 100) {
						AutoDungeons.LOGGER.warn("Took " + (now-start) + "ms to read " + compRow.getKey());
					}
					
					start = System.currentTimeMillis();
					root.blueprint = root.blueprint.join(piece);
					now = System.currentTimeMillis();
					if ((now-start) > 100) {
						AutoDungeons.LOGGER.warn("Took " + (now-start) + "ms to merge in " + comp + "/" + compRow.getKey());
					}
				}
				this.loadedRooms.add(root);
			}
		}

		@Override
		public void apply(Map<ResourceLocation, Map<String, CompoundTag>> data, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
			// For each comp grouping...
			loadedRooms.clear();
			if (data != null) {
				AutoDungeons.LOGGER.info("Loading {} room blueprint compositions", data.size());
				int pieceCount = 0;
				
				for (Entry<ResourceLocation, Map<String, CompoundTag>> entry : data.entrySet()) {
					final ResourceLocation comp = entry.getKey();
					final Map<String, CompoundTag> compMap = entry.getValue();
					pieceCount += compMap.size();
					loadComp(comp, compMap, resourceManagerIn, profilerIn);
				}
				
				AutoDungeons.LOGGER.info("Loaded {} room blueprint compositions from {} pieces", data.size(), pieceCount);
			}
		}
		
		/**
		 * Return the "composition" path. This is the whole path up to the actual filename.
		 * For example, "testcomp/comp1.cat" would return "testcomp".
		 * "mycomps/testcomp/comp1.cat" would return "mycomps/testcomp".
		 * This lets compositions still be organized by folder.
		 * @param path
		 * @return
		 */
		protected String getCompPath(String path) {
			path = path.replace('\\', '/');
			int idx = path.lastIndexOf('/');
			if (idx == -1) {
				return "";
			} else {
				return path.substring(0, idx);
			}
		}
	}
	
	private static final class ReloadListenerData {
		public Map<ResourceLocation, CompoundTag> roomData;
		public Map<ResourceLocation, Map<String, CompoundTag>> compData;
	}
	
	public static class BlueprintReloadListener extends SimplePreparableReloadListener<ReloadListenerData> {
		
		private final RoomReloadListener roomListener;
		private final RoomCompReloadListener compListener;
		
		public BlueprintReloadListener(String folder) {
			roomListener = new RoomReloadListener(folder);
			compListener = new RoomCompReloadListener(folder);
		}

		@Override
		protected ReloadListenerData prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
			final ReloadListenerData data = new ReloadListenerData();
			
			// Note: this whole class is here so that after applying, we can trigger a dungeon room reload.
			
			// This serializes these two operations instead of them happening in parallel :(
			data.roomData = this.roomListener.prepare(resourceManagerIn, profilerIn);
			data.compData = this.compListener.prepare(resourceManagerIn, profilerIn);
			
			return data;
		}

		@Override
		protected void apply(ReloadListenerData data, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
			this.roomListener.apply(data.roomData, resourceManagerIn, profilerIn);
			this.compListener.apply(data.compData, resourceManagerIn, profilerIn);
			
			DungeonRoomLoader.instance().loadedRooms.clear();
			DungeonRoomLoader.instance().loadedRooms.addAll(this.roomListener.loadedRooms);
			DungeonRoomLoader.instance().loadedRooms.addAll(this.compListener.loadedRooms);
			
			// This should not be here yet, but I can't find a good 'after data loading' event
			DungeonRoomRegistry.GetInstance().reload();
		}
	}
}
