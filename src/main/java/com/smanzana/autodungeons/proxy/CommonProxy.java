package com.smanzana.autodungeons.proxy;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.network.NetworkHandler;
import com.smanzana.autodungeons.network.message.DungeonRoomDataSyncMessage;
import com.smanzana.autodungeons.network.message.WorldKeySyncMessage;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomLoader;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

public class CommonProxy {

	public CommonProxy() {
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
	}
	
	public boolean isServer() {
		return true;
	}
	
	public @Nullable Player getPlayer() {
		return null;
	}

	public boolean hasIntegratedServer() {
		return false;
	}
	
	public void syncDungeonDefinitions(Player player) {
		NetworkHandler.sendTo(new DungeonRoomDataSyncMessage(DungeonRoomLoader.instance()), (ServerPlayer) player);
	}
	
	public void syncWorldKeys(Player player) {
		NetworkHandler.sendTo(new WorldKeySyncMessage(AutoDungeons.GetWorldKeys()), (ServerPlayer) player);
	}
	
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (!this.hasIntegratedServer()) {
			this.syncWorldKeys(event.getPlayer());
		}
	}
	
}
