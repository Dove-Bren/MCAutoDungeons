package com.smanzana.autodungeons.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

/**
 * A near clone of part of JsonReloadListener; takes a folder and extension and then lets
 * child classes implement the actual parsing instead of the resource iteration.
 * @author Skyler
 *
 */
public abstract class AutoReloadListener<T> extends SimplePreparableReloadListener<T> {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final String folder;
	private final String extension;
	
	public AutoReloadListener(String folder, String extension) {
		this.folder = folder;
		this.extension = extension == null ? "" : (extension.contains(".") ? extension : ("." + extension));
	}

	@Override
	public T prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
		T builder = null;
		
		for (ResourceLocation rawLocation : resourceManagerIn.listResources(folder, this::checkPath)) {
			final ResourceLocation dataLocation = makeSubLocation(rawLocation);
			
			try (
					final Resource resource = resourceManagerIn.getResource(rawLocation);
					final InputStream input = resource.getInputStream();
			) {
				builder = this.prepareResource(builder, dataLocation, input);
			} catch (IOException|IllegalStateException e) {
				LOGGER.error("Couldn't parse data file {} from {}", dataLocation, rawLocation, e);
			}
		}
		
		builder = checkPreparedData(builder, resourceManagerIn, profilerIn);
		
		return builder;
	}
	
	protected boolean checkPath(String path) {
		return path.endsWith(this.extension);
	}
	
	protected ResourceLocation makeSubLocation(ResourceLocation location) {
		final int folderLen = folder.length();
		final int extLen = extension.length();
		final String path = location.getPath();
		return new ResourceLocation(location.getNamespace(),
				path.substring(folderLen + 1, path.length() - extLen)
				);
	}
	
	protected abstract T prepareResource(T builder, ResourceLocation location, InputStream input) throws IOException, IllegalStateException;
	
	protected T checkPreparedData(T data, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
		return data;
	}
	
	@Override
	public abstract void apply(T objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn);
}
