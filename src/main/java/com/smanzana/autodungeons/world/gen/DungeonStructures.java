package com.smanzana.autodungeons.world.gen;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DungeonStructures {

	@SubscribeEvent
	public static void registerStructures(RegistryEvent.Register<Structure<?>> event) {
		registerStructurePieceTypes();
	}
	
	//@SubscribeEvent Imagine.
	//public static void registerStructurePieceTypes(RegistryEvent.Register<IStructurePieceType> event) {
	protected static void registerStructurePieceTypes() {
		//event.getRegistry().register(NostrumDungeonStructure.DungeonPieceSerializer.instance);
		IStructurePieceType.setPieceId(DungeonStructure.DungeonPieceSerializer.instance, DungeonStructure.DungeonPieceSerializer.PIECE_ID);
	}
}
