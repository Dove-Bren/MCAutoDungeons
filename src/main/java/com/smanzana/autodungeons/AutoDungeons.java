package com.smanzana.autodungeons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.autodungeons.listener.DungeonTracker;
import com.smanzana.autodungeons.proxy.ClientProxy;
import com.smanzana.autodungeons.proxy.CommonProxy;
import com.smanzana.autodungeons.world.WorldKeyRegistry;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AutoDungeons.MODID)
public class AutoDungeons
{

	public static final String MODID = "autodungeons";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	private static AutoDungeons instance;
	
	private CommonProxy proxy;
	private DungeonTracker dungeonTracker;

	private static WorldKeyRegistry worldKeys;
//
//	private final TargetManager serverTargetManager;
//	private final TargetManager clientTargetManager;

	public AutoDungeons() {
		instance = this;
		
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		dungeonTracker = DistExecutor.safeRunForDist(() -> DungeonTracker.Client::new, () -> DungeonTracker::new);;

		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static CommonProxy GetProxy() {
		return instance.proxy;
	}
	
	public static WorldKeyRegistry GetWorldKeys() {
		if (worldKeys == null) {
			if (instance.proxy.isServer()) {
				throw new RuntimeException("Accessing WorldKeys before a world has been loaded!");
			} else {
				worldKeys = new WorldKeyRegistry();
			}
		}
		return worldKeys;
	}

	public static DungeonTracker GetDungeonTracker() {
		return instance.dungeonTracker;
	}
	
	private void initWorldKeys(Level world) {
		worldKeys = (WorldKeyRegistry) ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(WorldKeyRegistry::load, WorldKeyRegistry::new,
				WorldKeyRegistry.DATA_NAME);
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isClientSide()) {
			// force an exception here if this is wrong
			ServerLevel world = (ServerLevel) event.getWorld();
			
			// Do the correct initialization for persisted data
			initWorldKeys(world);
		}
	}
	
}
