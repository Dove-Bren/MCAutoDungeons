package com.smanzana.autodungeons.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;

/**
 * A near clone of JsonReloadListener except with NBT
 * @author Skyler
 *
 */
public abstract class NBTReloadListener extends AutoReloadListener<Map<ResourceLocation, CompoundTag>> {
	
		private final boolean compressed;
	
	public NBTReloadListener(String folder, String extension, boolean compressed) {
		super(folder, extension);
		this.compressed = compressed;
	}
	
	@Override
	protected Map<ResourceLocation, CompoundTag> prepareResource(Map<ResourceLocation, CompoundTag> builder, ResourceLocation location, InputStream input) throws IOException, IllegalStateException {
		if (builder == null) {
			builder = new HashMap<>();
		}
		
		final CompoundTag existing;
		if (compressed) {
			existing = builder.put(location, NbtIo.readCompressed(input));
		} else {
			existing = builder.put(location, NbtIo.read(new DataInputStream(input)));
		}
		
		if (existing != null) {
			throw new IllegalStateException("Duplicate data file ignored with ID " + location);
		}
		
		return builder;
	}
}
