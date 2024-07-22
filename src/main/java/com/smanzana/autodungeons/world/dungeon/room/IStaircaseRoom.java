package com.smanzana.autodungeons.world.dungeon.room;

import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.NostrumDungeon.IWorldHeightReader;

public interface IStaircaseRoom extends IDungeonRoom {

	public BlueprintLocation getEntryStart(IWorldHeightReader world, BlueprintLocation start);
	
}
