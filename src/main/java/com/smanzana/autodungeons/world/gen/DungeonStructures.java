package com.smanzana.autodungeons.world.gen;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.world.level.levelgen.feature.StructurePieceType;
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
		//event.getRegistry().register(NostrumDungeonStructure.DungeonPieceSerializer.instance);
		StructurePieceType.setPieceId(DungeonStructure.DungeonPieceSerializer.instance, DungeonStructure.DungeonPieceSerializer.PIECE_ID);
	}
}
