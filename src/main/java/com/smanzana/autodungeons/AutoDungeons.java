package com.smanzana.autodungeons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.autodungeons.proxy.ClientProxy;
import com.smanzana.autodungeons.proxy.CommonProxy;

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
//	private AutoDungeonsManager petCommandManager;
//	private MovementListener movementListener;
//	private TargetListener targetListener;
//
//	private final TargetManager serverTargetManager;
//	private final TargetManager clientTargetManager;

	public AutoDungeons() {
		instance = this;
		
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		
//		(new ModConfig()).register();
//		
//		serverTargetManager = new TargetManager();
//		clientTargetManager = new TargetManager();
//		
//		MinecraftForge.EVENT_BUS.register(this);
//		movementListener = new MovementListener();
//		targetListener = new TargetListener();
//		MinecraftForge.EVENT_BUS.addListener(BoundIronGolemEntity::EntityInteractListener);
	}
	
	public static CommonProxy GetProxy() {
		return instance.proxy;
	}
	
//	public static AutoDungeonsManager GetAutoDungeonsManager() {
//		if (instance.petCommandManager == null) {
//			if (instance.proxy.isServer()) {
//				throw new RuntimeException("Accessing AutoDungeonsManager before a world has been loaded!");
//			} else {
//				instance.petCommandManager = new AutoDungeonsManager();
//			}
//		}
//		return instance.petCommandManager;
//	}
//	
//	public static MovementListener GetMovementListener() {
//		return instance.movementListener;
//	}
//	
//	public static TargetListener GetTargetListener() {
//		return instance.targetListener;
//	}
//	
//	public static TargetManager GetServerTargetManager() {
//		return instance.serverTargetManager;
//	}
//	
//	public static TargetManager GetClientTargetManager() {
//		return instance.clientTargetManager;
//	}
	
//	private void initAutoDungeonsManager(Level world) {
//		petCommandManager = (AutoDungeonsManager) ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(AutoDungeonsManager::Load, AutoDungeonsManager::new,
//				AutoDungeonsManager.DATA_NAME);
//
//		// TODO I think this is automatic now?
////		if (petCommandManager == null) {
////			petCommandManager = new AutoDungeonsManager();
////			world.getMapStorage().setData(AutoDungeonsManager.DATA_NAME, petCommandManager);
////		}
//	}
//	
//	@SubscribeEvent
//	public void onWorldLoad(WorldEvent.Load event) {
//		if (!event.getWorld().isClientSide()) {
//			// force an exception here if this is wrong
//			ServerLevel world = (ServerLevel) event.getWorld();
//			
//			// Do the correct initialization for persisted data
//			//initPetSoulRegistry(world);
//			initAutoDungeonsManager(world);
//		}
//	}
	
}
