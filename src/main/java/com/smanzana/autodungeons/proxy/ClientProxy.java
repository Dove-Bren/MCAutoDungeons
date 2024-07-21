package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.client.overlay.OverlayRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy extends CommonProxy {
	private OverlayRenderer overlayRenderer;
	
	public ClientProxy() {
		super();
		this.overlayRenderer = new OverlayRenderer();
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public @Nullable Player getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	public OverlayRenderer getOverlayRenderer() {
		return this.overlayRenderer;
	}
}
