package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.network.NetworkHandler;
import com.smanzana.autodungeons.network.message.DungeonRoomDataSyncMessage;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomLoader;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

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
	
	public void syncDungeonDefinitions(PlayerEntity player) {
		NetworkHandler.sendTo(new DungeonRoomDataSyncMessage(DungeonRoomLoader.instance()), (ServerPlayerEntity) player);
	}
	
}
