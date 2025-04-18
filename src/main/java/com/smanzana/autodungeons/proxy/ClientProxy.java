package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.client.overlay.OverlayRenderer;
import com.smanzana.autodungeons.client.render.BlueprintRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy extends CommonProxy {
	private OverlayRenderer overlayRenderer;
	private BlueprintRenderer blueprintRenderer;
	
	public ClientProxy() {
		super();
		this.overlayRenderer = new OverlayRenderer();
		this.blueprintRenderer = new BlueprintRenderer();
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

	@Override
	public boolean hasIntegratedServer() {
		return Minecraft.getInstance().isLocalServer();
	}
	
	@Override
	public void syncDungeonDefinitions(Player player) {
		; // don't do anything for integrated or client
	}
	
	@Override
	public void syncWorldKeys(Player player) {
		;
	}
	
	public OverlayRenderer getOverlayRenderer() {
		return this.overlayRenderer;
	}
	
	public BlueprintRenderer getBlueprintRenderer() {
		return this.blueprintRenderer;
	}
}
