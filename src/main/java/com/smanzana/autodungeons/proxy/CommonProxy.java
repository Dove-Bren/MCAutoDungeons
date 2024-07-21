package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;

public class CommonProxy {

	public CommonProxy() {
		
	}
	
	public boolean isServer() {
		return true;
	}
	
	public @Nullable Player getPlayer() {
		return null;
	}
	
}
