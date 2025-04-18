package com.smanzana.autodungeons.world.dungeon;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * Registry for dungeon types (not individual instances of a dungeon in the world).
 */
@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DungeonRegistry {

	public static final ResourceKey<Registry<Dungeon>> KEY_REG_DUNGEON_TYPES = ResourceKey.createRegistryKey(new ResourceLocation(AutoDungeons.MODID, "dungeon_type"));
	
	private static IForgeRegistry<Dungeon> REGISTRY;
	
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<Dungeon>().setName(KEY_REG_DUNGEON_TYPES.location()).setType(Dungeon.class).setMaxID(Integer.MAX_VALUE - 1)
			.disableSaving().create();
	}
	
	public static final IForgeRegistry<Dungeon> GetForgeRegistry() {
		return REGISTRY;
	}
	
	public static final @Nullable Dungeon Get(ResourceLocation key) {
		return REGISTRY.getValue(key);
	}
	
}
