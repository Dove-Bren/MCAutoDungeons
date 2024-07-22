package com.smanzana.autodungeons.tile;

import com.smanzana.autodungeons.world.WorldKey;

public interface IWorldKeyHolder {

	public boolean hasWorldKey();
	
	public WorldKey getWorldKey();
	
	public void setWorldKey(WorldKey key);
	
}
