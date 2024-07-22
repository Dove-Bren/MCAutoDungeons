package com.smanzana.autodungeons.tile;

import com.smanzana.autodungeons.world.NostrumWorldKey;

public interface IWorldKeyHolder {

	public boolean hasWorldKey();
	
	public NostrumWorldKey getWorldKey();
	
	public void setWorldKey(NostrumWorldKey key);
	
}
