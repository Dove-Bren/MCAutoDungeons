package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.client.overlay.OverlayRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

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
	public @Nullable PlayerEntity getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}

	@Override
	public boolean hasIntegratedServer() {
		return Minecraft.getInstance().isIntegratedServerRunning();
	}
	
	public OverlayRenderer getOverlayRenderer() {
		return this.overlayRenderer;
	}
}
