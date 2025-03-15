package com.smanzana.autodungeons.block;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(AutoDungeons.MODID)
public class BuiltinBlocks {
	
	private static final String ID_ENTRY_BLOCK = "entry_block";
	private static final String ID_EXIT_BLOCK = "exit_block";
	
	@ObjectHolder(ID_ENTRY_BLOCK) public static EntryBlock entryBlock;
	@ObjectHolder(ID_EXIT_BLOCK) public static ExitBlock exitBlock;
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, Item.Properties builder, IForgeRegistry<Item> registry) {
		BlockItem item = new BlockItem(block, builder);
    	item.setRegistryName(registryName);
    	registry.register(item);
	}
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, IForgeRegistry<Item> registry) {
		registerBlockItem(block, registryName, new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS), registry);
	}
	
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	registerBlockItem(entryBlock, entryBlock.getRegistryName(), registry);
    	registerBlockItem(exitBlock, exitBlock.getRegistryName(), registry);
    }
    
    private static void registerBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
    	block.setRegistryName(registryName);
    	registry.register(block);
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
    	final IForgeRegistry<Block> registry = event.getRegistry();
    	
    	registerBlock(new EntryBlock(), ID_ENTRY_BLOCK, registry);
    	registerBlock(new ExitBlock(), ID_EXIT_BLOCK, registry);
    }
    
}
