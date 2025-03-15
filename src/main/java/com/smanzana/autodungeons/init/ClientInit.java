package com.smanzana.autodungeons.init;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.block.BuiltinBlocks;
import com.smanzana.autodungeons.proxy.ClientProxy;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
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
		
    	ClientProxy proxy = (ClientProxy) AutoDungeons.GetProxy();
    	OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "AutoDungeons::keyOverlay", proxy.getOverlayRenderer());
    	
		//proxy.initKeybinds();
	}
	
	private static final void registerBlockRenderLayer() {
		ItemBlockRenderTypes.setRenderLayer(BuiltinBlocks.entryBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(BuiltinBlocks.exitBlock, RenderType.cutout());
	}
}
