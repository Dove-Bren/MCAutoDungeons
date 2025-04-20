package com.smanzana.autodungeons.world.gen;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DungeonStructures {

	@SubscribeEvent
	public static void registerStructures(RegistryEvent.Register<StructureFeature<?>> event) {
		registerStructurePieceTypes();
	}
	
	//@SubscribeEvent Imagine.
	//public static void registerStructurePieceTypes(RegistryEvent.Register<IStructurePieceType> event) {
	protected static void registerStructurePieceTypes() {
		// IceAndFire and TwilightForest talk about this not being needed except for older worlds, and register a dummy.
		// But in new 1.18.2 worlds, not having this causes log spam. ?
		// Maybe the point they're trying to make is you can just register a dummy one because it doesn't actaully do anything, BUT
		// we need it even for 1.18.2 worlds?
		
		//event.getRegistry().register(NostrumDungeonStructure.DungeonPieceSerializer.instance);
		Registry.register(Registry.STRUCTURE_PIECE, DungeonStructure.DungeonPieceSerializer.PIECE_ID, DungeonStructure.DungeonPieceSerializer.instance);
		//StructurePieceType.setTemplatePieceId(DungeonStructure.DungeonPieceSerializer.instance, DungeonStructure.DungeonPieceSerializer.PIECE_ID);
	}
}
