package com.smanzana.autodungeons.world.dungeon.room;

import net.minecraft.util.math.vector.Vector3i;

public interface IDungeonLobbyRoom extends IDungeonRoom {

	public Vector3i getStairOffset();
	
}