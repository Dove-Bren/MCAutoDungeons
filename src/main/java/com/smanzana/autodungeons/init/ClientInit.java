package com.smanzana.autodungeons.init;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.block.BuiltinBlocks;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = AutoDungeons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		registerBlockRenderLayer();
		
    	//ClientProxy proxy = (ClientProxy) AutoDungeons.instance.proxy;
		//proxy.initKeybinds();
	}
	
	private static final void registerBlockRenderLayer() {
		RenderTypeLookup.setRenderLayer(BuiltinBlocks.entryBlock, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BuiltinBlocks.exitBlock, RenderType.cutout());
	}
}
