package com.smanzana.autodungeons.init;

import com.smanzana.autodungeons.AutoDungeons;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
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
		//ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.LockedChestEntityType, TileEntityLockedChestRenderer::new);
		//ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.LockedDoorType, TileEntityLockedDoorRenderer::new);
		//ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.DungeonKeyChestTileEntityType, TileEntityDungeonKeyChestRenderer::new);
		//ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.DungeonDoorTileEntityType, TileEntityDungeonDoorRenderer::new);
		
		registerBlockRenderLayer();
		
    	//ClientProxy proxy = (ClientProxy) AutoDungeons.instance.proxy;
		//proxy.initKeybinds();
	}
	
	private static final void registerBlockRenderLayer() {
		//RenderTypeLookup.setRenderLayer(NostrumBlocks.lockedChest, RenderType.getSolid());
//		RenderTypeLookup.setRenderLayer(NostrumBlocks.smallDungeonKeyChest, RenderType.getCutout());
//		RenderTypeLookup.setRenderLayer(NostrumBlocks.largeDungeonKeyChest, RenderType.getCutout());
//		RenderTypeLookup.setRenderLayer(NostrumBlocks.smallDungeonDoor, RenderType.getCutout());
//		RenderTypeLookup.setRenderLayer(NostrumBlocks.largeDungeonDoor, RenderType.getCutout());
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		if(event.getMap().getTextureLocation() != AtlasTexture.LOCATION_BLOCKS_TEXTURE) {
			return;
		}
		
		// We have to request loading textures that aren't explicitly loaded by any of the normal registered models.
		// That means entity OBJ models, or textures we load on the fly, etc.
		event.addSprite(new ResourceLocation(
				AutoDungeons.MODID, "models/block/chain_link"));
		event.addSprite(new ResourceLocation(
				AutoDungeons.MODID, "models/block/lock_plate"));
		event.addSprite(new ResourceLocation(
				AutoDungeons.MODID, "block/key_cage"));
    }
}
