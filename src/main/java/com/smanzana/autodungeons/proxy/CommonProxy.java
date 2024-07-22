package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;

public class CommonProxy {

	public CommonProxy() {
		
	}
	
	public boolean isServer() {
		return true;
	}
	
	public @Nullable PlayerEntity getPlayer() {
		return null;
	}

	public boolean hasIntegratedServer() {
		return false;
	}
	
}
