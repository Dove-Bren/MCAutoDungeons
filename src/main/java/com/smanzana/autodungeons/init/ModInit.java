package com.smanzana.autodungeons.init;

import com.mojang.brigadier.CommandDispatcher;
import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.command.CommandReadRoom;
import com.smanzana.autodungeons.command.CommandSpawnDungeon;
import com.smanzana.autodungeons.command.CommandWriteRoom;
import com.smanzana.autodungeons.network.NetworkHandler;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomLoader.BlueprintReloadListener;

import net.minecraft.command.CommandSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Common (client and server) handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModInit {

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		
		// EARLY phase:
		////////////////////////////////////////////
//		registerShapes(); Should be here, but end up driving what items are created and have to be called super early!
//    	registerTriggers();
		
    	// NOTE: These registering methods are on the regular gameplay BUS,
    	// because they depend on data and re-fire when data is reloaded?
		MinecraftForge.EVENT_BUS.addListener(ModInit::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(ModInit::registerDataLoaders);
		MinecraftForge.EVENT_BUS.addListener(ModInit::syncDataEvent);
		
		NetworkHandler.getInstance();
	}
	
	public static final void registerCommands(RegisterCommandsEvent event) {
		// Note: not in ModInit because it's not a MOD bus event. Commands get registered when data is reloaded.
		final CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		
		CommandSpawnDungeon.register(dispatcher);
		CommandWriteRoom.register(dispatcher);
		CommandReadRoom.register(dispatcher);
	}
	
	public static final void registerDataLoaders(AddReloadListenerEvent event) {
		// This event is weird because it's for registering listeners of another event
		
		// Register data listener for dungeon rooms
		event.addListener(new BlueprintReloadListener("rooms"));
	}
	
	public static final void syncDataEvent(OnDatapackSyncEvent event) {
		AutoDungeons.GetProxy().syncDungeonDefinitions(event.getPlayer());
	}
}
