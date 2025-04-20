package com.smanzana.autodungeons.world.dungeon;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * Registry for dungeon types (not individual instances of a dungeon in the world).
 */
@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DungeonRegistry {

	public static final ResourceKey<Registry<Dungeon>> KEY_REG_DUNGEON_TYPES = ResourceKey.createRegistryKey(new ResourceLocation(AutoDungeons.MODID, "dungeon_type"));
	
	private static Supplier<IForgeRegistry<Dungeon>> REGISTRY;
	
	@SubscribeEvent
	public static void createRegistry(NewRegistryEvent event) {
		REGISTRY = event.create(new RegistryBuilder<Dungeon>().setName(KEY_REG_DUNGEON_TYPES.location()).setType(Dungeon.class).setMaxID(Integer.MAX_VALUE - 1)
			.disableSaving());
	}
	
	public static final IForgeRegistry<Dungeon> GetForgeRegistry() {
		return REGISTRY.get();
	}
	
	public static final @Nullable Dungeon Get(ResourceLocation key) {
		return REGISTRY.get().getValue(key);
	}
	
}
