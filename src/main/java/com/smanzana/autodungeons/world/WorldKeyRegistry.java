package com.smanzana.autodungeons.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.network.NetworkHandler;
import com.smanzana.autodungeons.network.message.WorldKeySyncMessage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Allows blocks/items to grant 'keys' that can be used on other doors/chests to unlock them,
 * even when those things may not be loaded right now.
 * This is used both as a world-wide key-to-object mapping where keys are UUIDs, and something
 * an RPG-like system where you have small keys and you consume a small key to open a door.
 * In a way, this is the same as the trigger mechanic on switch blocks etc. except the thing it's triggering
 * doesn't have to be loaded and respond right then.
 * Many things that hold keys are able to mutate them deterministically (like in templates) so that each time
 * a template is spawned, it refers to a different key set.
 * @author Skyler
 *
 */
public class WorldKeyRegistry extends WorldSavedData {
	
	public static final String DATA_NAME = AutoDungeons.MODID + "_world_keys";
	private static final String NBT_LIST = "key_map";
	private static final String NBT_KEY = "key";
	private static final String NBT_COUNT = "count";
	
	private final Map<WorldKey, Integer> keys;
	
	public WorldKeyRegistry() {
		this(DATA_NAME);
	}

	public WorldKeyRegistry(String name) {
		super(name);
		
		this.keys = new HashMap<>();
	}

	@Override
	public void load(CompoundNBT nbt) {
		keys.clear();
		
		ListNBT list = nbt.getList(NBT_LIST, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompound(i);
			CompoundNBT keyTag = tag.getCompound(NBT_KEY);
			final int count = tag.getInt(NBT_COUNT);
			if (count > 0) {
				keys.put(WorldKey.fromNBT(keyTag), count);
			}
		}
		
		AutoDungeons.LOGGER.info("Loaded " + keys.size() + " world keys");
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		ListNBT list = new ListNBT();
		for (Entry<WorldKey, Integer> entry : keys.entrySet()) {
			if (entry.getValue() == null || entry.getValue() <= 0) {
				continue;
			}
			
			CompoundNBT tag = new CompoundNBT();
			tag.put(NBT_KEY, entry.getKey().asNBT());
			tag.putInt(NBT_COUNT, entry.getValue());
			list.add(tag);
		}
		compound.put(NBT_LIST, list);
		
		AutoDungeons.LOGGER.info("Saved " + keys.size() + " world keys");
		return compound;
	}
	
	protected void broadcast() {
		NetworkHandler.sendToAll(new WorldKeySyncMessage(this));
	}
	
	public WorldKey addKey(WorldKey key) {
		keys.merge(key, 1, Integer::sum);
		this.setDirty();
		broadcast();
		return key;
	}
	
	public WorldKey addKey() {
		return addKey(new WorldKey());
	}
	
	public int getKeyCount(WorldKey key) {
		return keys.getOrDefault(key, 0);
	}
	
	public boolean hasKey(WorldKey key) {
		return getKeyCount(key) > 0;
	}
	
	public boolean consumeKey(WorldKey key) {
		final int count = getKeyCount(key);
		if (count > 0) {
			if (count == 1) {
				keys.remove(key);
			} else {
				keys.put(key, count - 1);
			}
			setDirty();
			broadcast();
			return true;
		}
		return false;
	}
	
	public void clearKeys() {
		keys.clear();
		setDirty();
		broadcast();
	}
}
